export interface Message {
  id: number;
  conversationId: number;
  senderId: number;
  content: string;
  read: boolean;
  messageType: 'REGULAR' | 'SYSTEM' | 'CONTRACT_REQUEST' | 'IMAGE' | 'LOCATION';
  relatedContractId?: number;
  createdAt: string; // ISO datum
  _temp?: boolean;

  // Populated only for CONTRACT_REQUEST messages
  contractAdTitle?: string;
  contractStartDate?: string;  // ISO date string e.g. "2026-03-20"
  contractEndDate?: string;
  contractTotalPrice?: number;
  contractCurrency?: string;

  // IMAGE
  imageUrl?: string;

  // LOCATION
  locationLat?: number;
  locationLng?: number;
  locationLabel?: string;
}
export interface MessageGroup {
  dateLabel: string; // Npr. "Danas", "Juče", "15.03.2026."
  messages: Message[]; // Poruke koje pripadaju tom danu
}
