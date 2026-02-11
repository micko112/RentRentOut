import {Ad} from './ad.model';
import {ContractStatus} from './ContractStatus';
import {User} from './user.model';

export interface RentalContract {
  id: number;
  ad: Ad;
  lessee: User;
  startDate: string;
  endDate:  string;
  agreedPrice:  number;
  contractStatus: ContractStatus;
}
