export interface AppNotification {
  id: number;
  type: string;
  title: string;
  message: string;
  isRead: boolean;
  relatedEntityId?: number;
  relatedEntityType?: string;
  actorName?: string;
  createdAt: string;
}
