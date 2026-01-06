package org.landm.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Item {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, name="name")
    private String name;
    @Column(nullable = false, name="price")
    private BigDecimal price;
    @Column(name="days")
    private int days;
    @Column(name="description")
    private String description;
    @ManyToOne
    @JoinColumn(name="owner_id")
    private User owner;

    public Item(String name, BigDecimal price, int days, String description, long userId){

    }

    public Item(String name, BigDecimal price, int days, String description, User owner) {
        this.name = name;
        this.price = price;
        this.days = days;
        this.description = description;
        this.owner = owner;
    }

    public Item() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long itemId) {
        this.id = itemId;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String itemName) {
        this.name = itemName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal itemPrice) {
        this.price = itemPrice;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
