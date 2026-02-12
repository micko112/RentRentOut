import {Ad} from './ad.model';
import {ContractStatus} from './ContractStatus';
import {User} from './user.model';

export interface RentalContract {
  id: number;
  adDto: Ad;
  lesseeDto: User;
  startDate: string;
  endDate:  string;
  agreedPrice:  number;
  contractStatus: ContractStatus;
}
