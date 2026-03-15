package org.landm.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateCategoryRequestDto {
   @NotBlank
    private String name;

   private Long parentId;

}
