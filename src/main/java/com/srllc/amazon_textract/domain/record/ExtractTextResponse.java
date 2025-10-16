package com.srllc.amazon_textract.domain.record;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ExtractTextResponse(
    StoreInfo store,
    List<ItemInfo> items,
    TransactionInfo transaction,
    List<String> rawText
) {
    public record StoreInfo(
        String name,
        String branch,
        String manager,
        Integer cashier_number
    ) {}
    
    public record ItemInfo(
        String product,
        Integer quantity,
        BigDecimal price
    ) {}
    
    public record TransactionInfo(
        BigDecimal subtotal,
        BigDecimal cash,
        BigDecimal change
    ) {}
}
