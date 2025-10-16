package com.srllc.amazon_textract.domain.service.impl;

import com.srllc.amazon_textract.domain.entity.Receipt;
import com.srllc.amazon_textract.domain.entity.ReceiptItem;
import com.srllc.amazon_textract.domain.record.ExtractTextResponse;
import com.srllc.amazon_textract.domain.repository.ReceiptRepository;
import com.srllc.amazon_textract.domain.service.TextractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class TextractServiceImpl implements TextractService {

    private final TextractClient textractClient;
    private final ReceiptRepository receiptRepository;

    @Override
    public ExtractTextResponse extractTextFromImage(MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();

            var request = DetectDocumentTextRequest.builder()
                    .document(Document.builder()
                            .bytes(SdkBytes.fromByteArray(imageBytes))
                            .build())
                    .build();

            var response = textractClient.detectDocumentText(request);

            List<String> lines = response.blocks().stream()
                    .filter(block -> block.blockType() == BlockType.LINE)
                    .map(Block::text)
                    .toList();

            Receipt receipt = parseAndSaveReceipt(lines);

            return new ExtractTextResponse(
                new ExtractTextResponse.StoreInfo(
                    receipt.getMerchantName(),
                    receipt.getBranch(),
                    receipt.getManagerName(),
                    receipt.getCashierNumber() != null ? Integer.parseInt(receipt.getCashierNumber()) : 0
                ),
                receipt.getItems().stream()
                    .map(item -> new ExtractTextResponse.ItemInfo(
                        item.getProduct(),
                        item.getQuantity(),
                        item.getPrice()))
                    .toList(),
                new ExtractTextResponse.TransactionInfo(
                    receipt.getTotalAmount(),
                    receipt.getCash(),
                    receipt.getChangeAmount()
                ),
                lines
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        } catch (TextractException e) {
            throw new RuntimeException("Textract Failed: " + e.getMessage(), e);
        }
    }

    private Receipt parseAndSaveReceipt(List<String> lines) {
        String merchantName = extractMerchantName(lines);
        String branch = extractBranch(lines);
        String managerName = extractManagerName(lines);
        String cashierNumber = extractCashierNumber(lines);
        BigDecimal subtotal = extractSubtotal(lines);
        BigDecimal cash = extractCash(lines);
        BigDecimal changeAmount = extractChangeAmount(lines);
        List<ReceiptItem> items = extractItems(lines);

        Receipt receipt = Receipt.builder()
                .merchantName(merchantName)
                .branch(branch)
                .managerName(managerName)
                .cashierNumber(cashierNumber)
                .totalAmount(subtotal)
                .cash(cash)
                .changeAmount(changeAmount)
                .receiptDate(LocalDateTime.now())
                .build();

        receipt = receiptRepository.save(receipt);

        for (ReceiptItem item : items) {
            item.setReceipt(receipt);
        }
        receipt.setItems(items);

        return receiptRepository.save(receipt);
    }

    private String extractMerchantName(List<String> lines) {
        for (String line : lines) {
            if (line.toUpperCase().contains("HYPERMARKET") || line.toUpperCase().contains("STORE") || line.toUpperCase().contains("MART")) {
                return line.trim();
            }
        }
        return lines.isEmpty() ? "Unknown Store" : lines.get(0);
    }

    private BigDecimal extractSubtotal(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).toLowerCase().contains("sub total")) {
                if (i + 1 < lines.size()) {
                    String amount = lines.get(i + 1).replace("$", "").trim();
                    try {
                        return new BigDecimal(amount);
                    } catch (NumberFormatException e) {
                        // Continue searching
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private List<ReceiptItem> extractItems(List<String> lines) {
        List<ReceiptItem> items = new ArrayList<>();
        boolean inItemSection = false;
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            
            // Start capturing items after "Name" header
            if (line.toLowerCase().equals("name")) {
                inItemSection = true;
                continue;
            }
            
            // Stop capturing items when we reach totals
            if (line.toLowerCase().contains("sub total") || line.toLowerCase().contains("total")) {
                break;
            }
            
            if (inItemSection && i + 2 < lines.size()) {
                String itemName = line;
                String qtyStr = lines.get(i + 1);
                String priceStr = lines.get(i + 2);
                
                // Skip header rows
                if (itemName.toLowerCase().equals("qty") || itemName.toLowerCase().equals("price")) {
                    continue;
                }
                
                try {
                    int quantity = Integer.parseInt(qtyStr);
                    BigDecimal price = new BigDecimal(priceStr.replace("$", ""));
                    
                    ReceiptItem item = ReceiptItem.builder()
                            .product(itemName)
                            .quantity(quantity)
                            .price(price)
                            .build();
                    items.add(item);
                    
                    i += 2; // Skip the next two lines as they are quantity and price
                } catch (NumberFormatException e) {
                    // Not a valid item, continue
                }
            }
        }
        return items;
    }

    private String extractBranch(List<String> lines) {
        // Look for city names or branch indicators
        for (String line : lines) {
            if (line.toLowerCase().contains("city") && !line.toLowerCase().contains("index")) {
                return line.trim();
            }
        }
        return null;
    }

    private String extractManagerName(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).toLowerCase().contains("manager")) {
                if (i + 1 < lines.size()) {
                    return lines.get(i + 1).trim();
                }
            }
        }
        return null;
    }

    private String extractCashierNumber(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).toLowerCase().contains("cashier")) {
                if (i + 1 < lines.size() && lines.get(i + 1).startsWith("#")) {
                    return lines.get(i + 1).replace("#", "").trim();
                }
            }
        }
        return null;
    }

    private BigDecimal extractCash(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).toLowerCase().equals("cash")) {
                if (i + 1 < lines.size()) {
                    String amount = lines.get(i + 1).replace("$", "").trim();
                    try {
                        return new BigDecimal(amount);
                    } catch (NumberFormatException e) {
                        // Continue searching
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal extractChangeAmount(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).toLowerCase().equals("change")) {
                if (i + 1 < lines.size()) {
                    String amount = lines.get(i + 1).replace("$", "").trim();
                    try {
                        return new BigDecimal(amount);
                    } catch (NumberFormatException e) {
                        // Continue searching
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private String extractField(List<String> lines, Pattern pattern) {
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }
        return null;
    }

    private BigDecimal extractAmount(List<String> lines, Pattern pattern) {
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                return new BigDecimal(matcher.group(1));
            }
        }
        return BigDecimal.ZERO;
    }
}
