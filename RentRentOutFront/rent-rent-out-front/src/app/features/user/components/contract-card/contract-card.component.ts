import {Component, EventEmitter, Input, Output} from '@angular/core';
import {RentalContract} from '../../../../shared/models/rental-contract.model';
import {CommonModule} from '@angular/common';
import {UserService} from '../../services/user.service';
import {ContractService} from '../../../contracts/services/contract.service';
import {Router, RouterLink} from '@angular/router';
import {ToastService} from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-contract-card',
  imports: [CommonModule, RouterLink],
  templateUrl: './contract-card.component.html',
  styleUrl: './contract-card.component.css'
})
export class ContractCardComponent {
    @Input() contract!: RentalContract;
    @Input() isOwnerView: boolean = false;
    @Output() statusUpdated = new EventEmitter<void>();

    constructor(private userService: UserService,
                private contractService: ContractService,
                private router: Router,
                private toastService: ToastService) {}
  updateStatus(newStatus: string) {
      this.contractService.updateStatus(this.contract.id, newStatus).subscribe({
        next: () => {
          if (newStatus === 'ACCEPTED') this.toastService.showSuccess('Uspešno ste prihvatili zahtev.');
          else if (newStatus === 'REJECTED') this.toastService.showSuccess('Zahtev je odbijen.');
          else if (newStatus === 'ACTIVE') this.toastService.showSuccess('Predmet je uspešno predat!');
          else if (newStatus === 'FINISHED') this.toastService.showSuccess('Iznajmljivanje je završeno.');
          else if (newStatus === 'CANCELLED') this.toastService.showSuccess('Ugovor je otkazan.');
          else if (newStatus === 'CANCELLED_AFTER_ACCEPT') this.toastService.showSuccess('Ugovor je otkazan. Obe strane mogu ostaviti ocenu.');
          this.statusUpdated.emit();
        },
        error: (err) => {
          this.toastService.showError('Greška: ' + (err.error?.message || 'Pokušajte ponovo.'));
        }})
  }

  get isExpiredRequest(): boolean {
    if (!this.contract || this.contract.contractStatus !== 'REQUESTED') return false;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return new Date(this.contract.endDate) < today;
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      REQUESTED: 'Na čekanju',
      ACCEPTED: 'Prihvaćen',
      ACTIVE: 'Aktivan',
      FINISHED: 'Završen',
      CANCELLED: 'Otkazan',
      REJECTED: 'Odbijen',
      EXPIRED: 'Istekao',
      CANCELLED_AFTER_ACCEPT: 'Otkazan (prihvaćen)',
      AD_DELETED: 'Oglas obrisan',
      DELETED: 'Obrisan',
    };
    return labels[status] ?? status;
  }
}
