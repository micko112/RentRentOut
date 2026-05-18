package org.landm.dto.chat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SendMessageRequestDto {

    @NotNull
    private Long adId;
    @NotNull
    private Long receiverId;

    @Size(max = 5000)
    private String content;

    /** REGULAR | IMAGE | LOCATION (default REGULAR ako je null) */
    private String messageType;

    @Size(max = 500)
    private String imageUrl;

    private BigDecimal locationLat;
    private BigDecimal locationLng;

    @Size(max = 255)
    private String locationLabel;
}
