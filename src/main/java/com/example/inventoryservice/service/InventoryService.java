package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.ReserveInventoryCommand;
import com.example.inventoryservice.entity.InventoryReservation;
import com.example.inventoryservice.entity.InventoryReservationItem;
import com.example.inventoryservice.event.InventoryReservedEvent;
import com.example.inventoryservice.kafka.InventoryTopics;
import com.example.inventoryservice.repository.InventoryReservationRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryReservationRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryService(InventoryReservationRepository repository,
                            KafkaTemplate<String, Object> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void reserve(ReserveInventoryCommand command) {
        // Идемпотентность: если для этой саги уже есть резерв — не создаём второй,
        // но событие переотправляем (at-least-once семантику оркестратор должен уметь).
        InventoryReservation reservation = repository.findBySagaId(command.sagaId())
                .orElseGet(() -> createNew(command));

        InventoryReservedEvent event = new InventoryReservedEvent(
                reservation.getSagaId(),
                reservation.getOrderId(),
                reservation.getId().toString(),
                reservation.getItems().stream()
                        .map(i -> new InventoryReservedEvent.ReservedItem(i.getProductId(), i.getQuantity()))
                        .toList()
        );

        kafkaTemplate.send(InventoryTopics.INVENTORY_RESERVED_EVENT, reservation.getSagaId(), event);
    }

    private InventoryReservation createNew(ReserveInventoryCommand command) {
        List<InventoryReservationItem> items = command.items().stream()
                .map(i -> new InventoryReservationItem(i.productId(), i.quantity()))
                .toList();

        InventoryReservation reservation =
                new InventoryReservation(command.sagaId(), command.orderId(), items);

        return repository.save(reservation);
    }
}