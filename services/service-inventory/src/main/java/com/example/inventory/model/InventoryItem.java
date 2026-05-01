package com.example.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long productId;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String sku;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    @Builder.Default
    private Integer quantityOnHand = 0;

    @Builder.Default
    private Integer reorderLevel = 10;

    private LocalDateTime lastRestocked;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
