export interface SendMessageRequest {
  adId: number;
  receiverId: number;
  content?: string;
  messageType?: 'REGULAR' | 'IMAGE' | 'LOCATION';
  imageUrl?: string;
  locationLat?: number;
  locationLng?: number;
  locationLabel?: string;
}
