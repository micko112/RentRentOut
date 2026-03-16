import {UserShort} from './userShort';


export interface ConversationPreview {
  id: number;
  adId: number;
  adTitle: string;
  adThumbnail: string | null;
  otherParticipant: UserShort;
  lastMessagePreview: string;
  updatedAt: string;
  unreadCount: number;
}
