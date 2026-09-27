package com.example.inventoryservice.event;

import java.util.List;
import java.util.UUID;

public record InventoryReservedEvent(
        UUID eventId,
        String sagaId,
        String orderId,
        String reservationId,
        List<ReservedItem> items
) {
    public record ReservedItem(String productId, int quantity) {}
}