import {Ad} from './ad.model';

export interface CreateRentalContractRequest {
  adId: number;
  startDate: string;
  endDate:  string;
  agreedPrice:  number;
  amount: number;
}
