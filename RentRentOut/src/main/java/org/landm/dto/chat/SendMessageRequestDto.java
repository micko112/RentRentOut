package org.landm.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 5000)
    private String content;
}
