package com.srllc.amazon_textract.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "receipts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "branch")
    private String branch = "Main Branch";

    @Column(name = "manager_name")
    private String managerName = "Store Manager";

    @Column(name = "cashier_number")
    private String cashierNumber = "1";
    
    @Column(name = "subtotal")
    private BigDecimal subtotal = new BigDecimal("107.60");

    @Column(name = "cash")
    private BigDecimal cash = new BigDecimal("200.00");

    @Column(name = "change_amount")
    private BigDecimal changeAmount = new BigDecimal("92.40");
    
    @Column(name = "receipt_date")
    private LocalDateTime receiptDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReceiptItem> items;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}