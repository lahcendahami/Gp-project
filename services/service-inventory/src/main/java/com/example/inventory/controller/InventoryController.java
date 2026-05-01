package com.example.inventory.controller;

import com.example.inventory.dto.InventoryRequest;
import com.example.inventory.dto.StockAdjustmentRequest;
import com.example.inventory.model.InventoryItem;
import com.example.inventory.service.InventoryService;
import com.example.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.findById(id)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.findLowStock()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryItem>> create(@Valid @RequestBody InventoryRequest request) {
        InventoryItem created = inventoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Inventory item created successfully inv", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> update(@PathVariable Long id,
            @Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory item updated", inventoryService.update(id, request)));
    }

    @PatchMapping("/{id}/adjust")
    public ResponseEntity<ApiResponse<InventoryItem>> adjustStock(@PathVariable Long id,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Stock adjusted", inventoryService.adjustStock(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        inventoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Inventory item deleted", null));
    }
}
