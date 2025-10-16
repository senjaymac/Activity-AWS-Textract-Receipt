package com.srllc.amazon_textract.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "receipt_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product")
    private String product;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "price")
    private BigDecimal price;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    @JsonBackReference
    private Receipt receipt;
}