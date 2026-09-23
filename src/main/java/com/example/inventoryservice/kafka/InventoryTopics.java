package com.example.inventoryservice.kafka;

public final class InventoryTopics {
    public static final String RESERVE_INVENTORY_COMMAND = "reserve-inventory-command";
    public static final String INVENTORY_RESERVED_EVENT   = "inventory-reserved-event";
    public static final String INVENTORY_RESERVATION_FAILED_EVENT = "inventory-reservation-failed-events";

    private InventoryTopics() {}
}