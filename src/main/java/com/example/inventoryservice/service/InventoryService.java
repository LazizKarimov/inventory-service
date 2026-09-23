package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.InventoryReservationFailedEvent;
import com.example.inventoryservice.dto.ReserveInventoryCommand;
import com.example.inventoryservice.entity.InventoryReservation;
import com.example.inventoryservice.entity.InventoryReservationItem;
import com.example.inventoryservice.event.InventoryReservedEvent;
import com.example.inventoryservice.kafka.InventoryTopics;
import com.example.inventoryservice.repository.InventoryReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
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
        // 1. Проверка на намеренный failure
        String failedProductId = findFailedProduct(command);
        if (failedProductId != null) {
            log.warn("Намеренный failure: товар {} не может быть зарезервирован, сага {}",
                    failedProductId, command.sagaId());
            publishInventoryReservationFailed(command, "Товар " + failedProductId + " не может быть зарезервирован");
            return;
        }

        // 2. Обычная логика — как было
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

    private String findFailedProduct(ReserveInventoryCommand command) {
        return command.items().stream()
                .map(ReserveInventoryCommand.ReserveItem::productId)
                .filter(productId -> productId != null && productId.startsWith("FAIL-"))
                .findFirst()
                .orElse(null);
    }

    private void publishInventoryReservationFailed(ReserveInventoryCommand command, String errorMessage) {
        InventoryReservationFailedEvent failedEvent = new InventoryReservationFailedEvent(
                UUID.fromString(command.sagaId()),
                UUID.fromString(command.orderId()),
                errorMessage
        );

        kafkaTemplate.send(
                InventoryTopics.INVENTORY_RESERVATION_FAILED_EVENT,
                command.sagaId(),
                failedEvent
        );
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