package com.example.inventoryservice.dto;

import java.util.UUID;

public record InventoryReservationFailedEvent(
        UUID eventId,
        UUID sagaId,
        UUID orderId,
        String errorMessage
) {}