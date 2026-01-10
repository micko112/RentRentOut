package org.landm.dto;

import org.landm.entity.Item;

import java.util.List;

public class CategoryDto {
    private long id;
    private String name;
    private Long parentId;
    public CategoryDto() {
    }

    public CategoryDto(long id, String name, long parentId) {
        this.id = id;
        this.name = name;
this.parentId = parentId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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
