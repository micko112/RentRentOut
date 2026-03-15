package org.landm.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequestDto {

    @NotNull
    private Long adId; // Ako je nova konverzacija, treba nam ovo
    @NotNull
    private Long receiverId; // Kome šaljemo
    @NotBlank
    private String content;
}
