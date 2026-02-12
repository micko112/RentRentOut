import {Component, OnInit} from '@angular/core';
import {CommonModule, NgForOf, NgIf} from '@angular/common';
import {RentalContract} from '../../../../shared/models/rental-contract.model';
import {Observable} from 'rxjs';
import {UserService} from '../../services/user.service';
import {ContractCardComponent} from '../../components/contract-card/contract-card.component';

@Component({
  selector: 'app-contracts',
  imports: [
    NgForOf,
    NgIf,
    CommonModule,
    ContractCardComponent
  ],
  templateUrl: './contracts.component.html',
  styleUrl: './contracts.component.css'
})
export class ContractsComponent implements OnInit {
  contracts$!: Observable<RentalContract[] | null>;
  constructor(private userService: UserService,
              ) {
  }
  ngOnInit() {
    this.contracts$ = this.userService.getAllContract();
    console.log(this.contracts$);
  }
}
