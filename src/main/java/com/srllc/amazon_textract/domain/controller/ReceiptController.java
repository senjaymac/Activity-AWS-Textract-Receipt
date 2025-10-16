package com.srllc.amazon_textract.domain.controller;

import com.srllc.amazon_textract.domain.entity.Receipt;
import com.srllc.amazon_textract.domain.repository.ReceiptRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/receipts")
@Tag(name = "Receipt Management", description = "Operations for managing receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptRepository receiptRepository;

    @GetMapping
    @Operation(summary = "Get all receipts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receipts retrieved successfully")
    })
    public ResponseEntity<List<Receipt>> getAllReceipts() {
        return ResponseEntity.ok(receiptRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get receipt by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receipt found"),
            @ApiResponse(responseCode = "404", description = "Receipt not found")
    })
    public ResponseEntity<Receipt> getReceiptById(@PathVariable Long id) {
        return receiptRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}