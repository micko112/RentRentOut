import {Component, OnInit, TemplateRef} from '@angular/core';
import {CommonModule, NgForOf, NgIf, NgIfContext} from '@angular/common';
import {RentalContract} from '../../../../shared/models/rental-contract.model';
import {Observable} from 'rxjs';
import {UserService} from '../../services/user.service';
import {ContractCardComponent} from '../../components/contract-card/contract-card.component';
import {AuthService} from '../../../auth/services/auth.service';

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
  incomingRequests: RentalContract[] = [];
  outgoingRequests: RentalContract[] = [];

  contracts$!: Observable<RentalContract[] | null>;

  constructor(private userService: UserService,
              private authService: AuthService) {}
  ngOnInit() {
    this.loadContracts();
  }

  private loadContracts() {
    const currentUser = this.authService.currentUserValue;
    if(!currentUser) return;

    this.userService.getAllContract().subscribe(allContracts => {
    this.outgoingRequests = allContracts.filter(c=> c.lesseeDto.email === currentUser.email);

    this.incomingRequests = allContracts.filter(c=> c.adDto.owner.email === currentUser.email);

    });
  }
  refreshData(){
    this.loadContracts();
  }
}

