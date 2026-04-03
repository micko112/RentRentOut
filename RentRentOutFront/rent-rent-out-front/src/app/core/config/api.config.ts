import { environment } from '../../../environments/environment';

export const API_BASE_URL = environment.apiUrl;
export const WS_BASE_URL = environment.wsUrl;

// VAPID public key - mora da se poklapa sa app.vapid.public-key u application.properties
export const VAPID_PUBLIC_KEY = 'BEl62iUYgUivxIkv69yViEuiBIa-Ib9-SkvMeAtA3LFgDzkrxZJjSgSnfckjBJuBkr3qBUYIHBQFLXYp5Nksh8U';
