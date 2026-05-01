package com.example.inventory.service;

import com.example.inventory.dto.InventoryRequest;
import com.example.inventory.dto.StockAdjustmentRequest;
import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;
import com.example.shared.exception.BusinessException;
import com.example.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void create_WhenSkuExists_ShouldThrowException() {
        InventoryRequest request = new InventoryRequest();
        request.setSku("SKU-001");

        when(inventoryRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> inventoryService.create(request));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void create_WhenValid_ShouldSaveItem() {
        InventoryRequest request = new InventoryRequest();
        request.setProductId(1L);
        request.setSku("SKU-001");
        request.setQuantityOnHand(100);
        request.setReorderLevel(20);

        when(inventoryRepository.existsBySku(anyString())).thenReturn(false);
        InventoryItem savedItem = InventoryItem.builder()
                .id(1L)
                .productId(1L)
                .sku("SKU-001")
                .quantityOnHand(100)
                .reorderLevel(20)
                .build();
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(savedItem);

        InventoryItem result = inventoryService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("SKU-001", result.getSku());
        assertEquals(100, result.getQuantityOnHand());
        verify(inventoryRepository).save(any(InventoryItem.class));
    }

    @Test
    void adjustStock_WhenInsufficientStock_ShouldThrowException() {
        InventoryItem item = InventoryItem.builder()
                .id(1L)
                .sku("SKU-001")
                .quantityOnHand(5)
                .build();
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantityChange(-10);

        assertThrows(BusinessException.class, () -> inventoryService.adjustStock(1L, request));
    }

    @Test
    void adjustStock_WhenValid_ShouldAdjust() {
        InventoryItem item = InventoryItem.builder()
                .id(1L)
                .sku("SKU-001")
                .quantityOnHand(50)
                .build();
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantityChange(25);
        request.setReason("Restock");

        InventoryItem result = inventoryService.adjustStock(1L, request);

        assertEquals(75, result.getQuantityOnHand());
        assertNotNull(result.getLastRestocked());
    }

    @Test
    void findById_WhenNotFound_ShouldThrowException() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.findById(1L));
    }
}
