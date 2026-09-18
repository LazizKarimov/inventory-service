package com.example.inventoryservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservation",
        uniqueConstraints = @UniqueConstraint(name = "ux_reservation_saga", columnNames = "saga_id"))
public class InventoryReservation {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "saga_id", nullable = false, updatable = false)
    private String sagaId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InventoryReservationItem> items;

    protected InventoryReservation() {}

    public InventoryReservation(String sagaId, String orderId, List<InventoryReservationItem> items) {
        this.id = UUID.randomUUID();
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = Instant.now();
        this.items = items;
        items.forEach(i -> i.attachTo(this));
    }

    // getters
    public UUID getId() { return id; }
    public String getSagaId() { return sagaId; }
    public String getOrderId() { return orderId; }
    public ReservationStatus getStatus() { return status; }
    public List<InventoryReservationItem> getItems() { return items; }
}