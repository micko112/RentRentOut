import {Component, EventEmitter, Input, input, Output} from '@angular/core';
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
          this.statusUpdated.emit();
        },
        error: (err) => {
          this.toastService.showError('Greška: ' + (err.error?.message || 'Pokušajte ponovo.'));
        }})
  }
}
