import {Component, OnInit, TemplateRef} from '@angular/core';
import {CommonModule, NgForOf, NgIf, NgIfContext} from '@angular/common';
import {RentalContract} from '../../../../shared/models/rental-contract.model';
import {Observable} from 'rxjs';
import {UserService} from '../../services/user.service';
import {ContractCardComponent} from '../../components/contract-card/contract-card.component';
import {AuthService} from '../../../auth/services/auth.service';
import {ActivatedRoute} from '@angular/router';

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

  private scrollToContractId: string | null = null;

  constructor(private userService: UserService,
              private authService: AuthService,
              private route: ActivatedRoute) {}

  ngOnInit() {
    this.scrollToContractId = this.route.snapshot.queryParamMap.get('contractId');
    this.loadContracts();
  }

  private loadContracts() {
    const currentUser = this.authService.currentUserValue;
    if(!currentUser) return;

    this.userService.getAllContract().subscribe(allContracts => {
      this.outgoingRequests = allContracts.filter(c=> c.lesseeDto.id === currentUser.id);
      this.incomingRequests = allContracts.filter(c=> c.adDto.owner.id === currentUser.id);

      if (this.scrollToContractId) {
        const targetId = this.scrollToContractId;
        setTimeout(() => {
          const el = document.getElementById('contract-' + targetId);
          if (el) {
            el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            el.classList.add('contract-highlighted');
            setTimeout(() => el.classList.remove('contract-highlighted'), 2000);
          }
        }, 150);
      }
    });
  }

  refreshData(){
    this.loadContracts();
  }
}

