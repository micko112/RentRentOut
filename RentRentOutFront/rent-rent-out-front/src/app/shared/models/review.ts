import {User} from './user.model';
import {RentalContract} from './rental-contract.model';


export interface Review {
  id: number,
  contractId: number;
  reviewerId: number;
  revieweeId: number;
  reviewerUsername: string;
  adTitle: string,
  paymentOk: string,
  communicationOk: string,
  agreementOk: string,
  reviewType: string,
  revieweeRole: 'LESSOR' | 'LESSEE';
  comment: string,
  createdAt: string,

}
