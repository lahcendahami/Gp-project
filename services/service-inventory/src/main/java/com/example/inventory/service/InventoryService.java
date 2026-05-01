package com.example.inventory.service;

import com.example.inventory.dto.InventoryRequest;
import com.example.inventory.dto.StockAdjustmentRequest;
import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;
import com.example.shared.exception.BusinessException;
import com.example.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public List<InventoryItem> findAll() {
        return inventoryRepository.findAll();
    }

    public InventoryItem findById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", id));
    }

    public List<InventoryItem> findLowStock() {
        // Find items where quantity on hand is at or below their reorder level
        // We use a default threshold of 10 for simple query, service logic filters further
        List<InventoryItem> allItems = inventoryRepository.findAll();
        return allItems.stream()
                .filter(item -> item.getQuantityOnHand() <= item.getReorderLevel())
                .toList();
    }

    @Transactional
    public InventoryItem create(InventoryRequest request) {
        if (inventoryRepository.existsBySku(request.getSku())) {
            throw new BusinessException("SKU already exists: " + request.getSku());
        }
        InventoryItem item = InventoryItem.builder()
                .productId(request.getProductId())
                .sku(request.getSku())
                .quantityOnHand(request.getQuantityOnHand())
                .reorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : 10)
                .build();
        InventoryItem saved = inventoryRepository.save(item);
        log.info("Created inventory item id={} sku={}", saved.getId(), saved.getSku());
        return saved;
    }

    @Transactional
    public InventoryItem update(Long id, InventoryRequest request) {
        InventoryItem item = findById(id);
        item.setProductId(request.getProductId());
        item.setSku(request.getSku());
        item.setQuantityOnHand(request.getQuantityOnHand());
        if (request.getReorderLevel() != null) {
            item.setReorderLevel(request.getReorderLevel());
        }
        return inventoryRepository.save(item);
    }

    @Transactional
    public InventoryItem adjustStock(Long id, StockAdjustmentRequest request) {
        InventoryItem item = findById(id);
        int newQuantity = item.getQuantityOnHand() + request.getQuantityChange();
        if (newQuantity < 0) {
            throw new BusinessException("Insufficient stock. Current: " + item.getQuantityOnHand()
                    + ", adjustment: " + request.getQuantityChange());
        }
        item.setQuantityOnHand(newQuantity);
        if (request.getQuantityChange() > 0) {
            item.setLastRestocked(LocalDateTime.now());
        }
        log.info("Adjusted stock for item id={} sku={} by {} (reason: {})",
                id, item.getSku(), request.getQuantityChange(), request.getReason());
        return inventoryRepository.save(item);
    }

    @Transactional
    public void delete(Long id) {
        InventoryItem item = findById(id);
        inventoryRepository.delete(item);
        log.info("Deleted inventory item id={}", id);
    }
}
