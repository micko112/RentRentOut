export interface Message {
  id: number;
  conversationId: number;
  senderId: number;
  content: string;
  read: boolean;
  createdAt: string; // ISO datum
}
