package com.example.inventoryservice.dto;

import java.util.UUID;

public record InventoryReservationFailedEvent(
        UUID sagaId,
        UUID orderId,
        String errorMessage
) {}