package org.landm.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateCategoryRequestDto {
   @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
