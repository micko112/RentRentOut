package org.landm.entity;


import jakarta.persistence.*; 
import org.landm.entity.User;

@Entity
public class Item {

    @Id
    @Column(name="item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long itemId;
    @Column(name="item_name")
    private String itemName;
    @Column(name="item_price")
    private float itemPrice;
    @Column(name="days")
    private int days;
//    private User owner;

    public Item(){

    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

//    public User getOwner() {
//        return owner;
//    }

//    public void setOwner(User owner) {
//        this.owner = owner;
//    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public float getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(float itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
