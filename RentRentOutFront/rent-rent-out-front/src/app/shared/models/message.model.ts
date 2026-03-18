export interface Message {
  id: number;
  conversationId: number;
  senderId: number;
  content: string;
  read: boolean;
  createdAt: string; // ISO datum
  _temp?: boolean;
}
export interface MessageGroup {
  dateLabel: string; // Npr. "Danas", "Juče", "15.03.2026."
  messages: Message[]; // Poruke koje pripadaju tom danu
}
