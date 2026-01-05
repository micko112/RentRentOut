package org.landm.dto;

public class ItemDto {

    private String itemName;
    private float itemPrice;
    private int days;

    public ItemDto() {
    }

    public ItemDto(String itemName, float itemPrice, int days) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.days = days;
    }

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
