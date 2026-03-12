import {User} from './user.model';
import {RentalContract} from './rental-contract.model';
import {UserShort} from './userShort';


export interface Review {
  id: number,
  contractId: number;
  reviewer: UserShort;
  reviewee: UserShort;
  adTitle: string,
  paymentOk: string,
  communicationOk: string,
  agreementOk: string,
  reviewType: string,
  revieweeRole: 'LESSOR' | 'LESSEE';
  comment: string,
  createdAt: string,

}
