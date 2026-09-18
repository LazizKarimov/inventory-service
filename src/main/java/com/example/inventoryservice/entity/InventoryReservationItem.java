package com.example.inventoryservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_reservation_item")
public class InventoryReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private InventoryReservation reservation;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    protected InventoryReservationItem() {}

    public InventoryReservationItem(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    void attachTo(InventoryReservation reservation) { this.reservation = reservation; }

    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
}