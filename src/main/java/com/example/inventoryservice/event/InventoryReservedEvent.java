package com.example.inventoryservice.event;

import java.util.List;

public record InventoryReservedEvent(
        String sagaId,
        String orderId,
        String reservationId,
        List<ReservedItem> items
) {
    public record ReservedItem(String productId, int quantity) {}
}