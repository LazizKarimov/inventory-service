package com.example.inventoryservice.kafka;

import com.example.inventoryservice.dto.ReserveInventoryCommand;
import com.example.inventoryservice.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReserveInventoryCommandListener {

    private final InventoryService inventoryService;

    public ReserveInventoryCommandListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = InventoryTopics.RESERVE_INVENTORY_COMMAND,
            groupId = "${inventory.kafka.consumer-group:inventory-service}"
    )
    public void on(ReserveInventoryCommand command) {
        inventoryService.reserve(command);
    }
}