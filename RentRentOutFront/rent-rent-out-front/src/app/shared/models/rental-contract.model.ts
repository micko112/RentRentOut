import {Ad} from './ad.model';
import {ContractStatus} from './ContractStatus';

export interface ContractParticipant {
  id: number;
  firstname: string;
  lastname: string;
  avatarUrl: string;
}

export interface RentalContract {
  id: number;
  adDto: Ad;
  lesseeDto: ContractParticipant;
  startDate: string;
  endDate:  string;
  agreedPrice:  number;
  currency: string;
  contractStatus: ContractStatus;
}
