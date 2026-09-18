package com.example.inventoryservice.dto;

import java.util.List;

public record ReserveInventoryCommand(
        String sagaId,
        String orderId,
        List<ReserveItem> items
) {
    public record ReserveItem(String productId, int quantity) {}
}