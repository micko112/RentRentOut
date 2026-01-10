package org.landm.dto;

import org.landm.entity.Item;

import java.util.List;

public class CategoryDto {
    private long id;
    private String name;
    private List<Item> items;
    public CategoryDto() {
    }

    public CategoryDto(long id, String name, List<Item> items) {
        this.id = id;
        this.name = name;
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
