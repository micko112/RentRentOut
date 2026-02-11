import { Injectable } from '@angular/core';
import { RentalContract} from '../../../shared/models/rental-contract.model';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {API_BASE_URL} from '../../../core/config/api.config';
import {CreateRentalContractRequest} from '../../../shared/models/create-rental-contract-request';

@Injectable({
  providedIn: 'root'
})
export class ContractService {
  private url = `${API_BASE_URL}/rental-contract`;
  constructor(private http: HttpClient,) { }

  createRentalContract(request: CreateRentalContractRequest): Observable<RentalContract> {
    return this.http.post<RentalContract>(this.url, request);
  }
}
