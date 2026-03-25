import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {RentalContract} from '../../../../shared/models/rental-contract.model';
import {UserService} from '../../services/user.service';
import {ContractCardComponent} from '../../components/contract-card/contract-card.component';
import {AuthService} from '../../../auth/services/auth.service';
import {ActivatedRoute} from '@angular/router';
import {ToastService} from '../../../../shared/services/toast.service';
import {Subject, takeUntil} from 'rxjs';

@Component({
  selector: 'app-contracts',
  imports: [
    CommonModule,
    FormsModule,
    ContractCardComponent
  ],
  templateUrl: './contracts.component.html',
  styleUrl: './contracts.component.css'
})
export class ContractsComponent implements OnInit, OnDestroy {
  incomingRequests: RentalContract[] = [];
  outgoingRequests: RentalContract[] = [];
  searchQuery = '';
  private destroy$ = new Subject<void>();

  get filteredIncoming(): RentalContract[] {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) return this.incomingRequests;
    return this.incomingRequests.filter(c =>
      c.adDto?.title?.toLowerCase().includes(q)
    );
  }

  get filteredOutgoing(): RentalContract[] {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) return this.outgoingRequests;
    return this.outgoingRequests.filter(c =>
      c.adDto?.title?.toLowerCase().includes(q)
    );
  }

  private scrollToContractId: string | null = null;

  constructor(private userService: UserService,
              private authService: AuthService,
              private route: ActivatedRoute,
              private toastService: ToastService) {}

  ngOnInit() {
    this.scrollToContractId = this.route.snapshot.queryParamMap.get('contractId');
    this.loadContracts();
  }

  private loadContracts() {
    const currentUser = this.authService.currentUserValue;
    if(!currentUser) return;

    this.userService.getAllContract().pipe(takeUntil(this.destroy$)).subscribe({
      next: allContracts => {
        this.outgoingRequests = allContracts.filter(c => c.lesseeDto?.id === currentUser.id);
        this.incomingRequests = allContracts.filter(c => c.adDto?.owner?.id === currentUser.id);

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
      },
      error: () => {
        this.toastService.showError('Greška pri učitavanju ugovora.');
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  refreshData(){
    this.loadContracts();
  }
}

