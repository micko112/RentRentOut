package org.landm.dto.chat;

import lombok.Getter;
import lombok.Setter;
import org.landm.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Setter
@Getter
public class ConversationPreviewDto {

    private Long id;

    private Long adId;
    private String adTitle;
    private String adThumbnail;
    private UserShortDto otherParticipant;

    private String lastMessagePreview;

    private LocalDateTime updatedAt;
    private int unreadCount;

}
