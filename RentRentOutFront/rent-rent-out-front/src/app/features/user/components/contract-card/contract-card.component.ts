import {Component, EventEmitter, Input, input, Output} from '@angular/core';
import {RentalContract} from '../../../../shared/models/rental-contract.model';
import {CommonModule} from '@angular/common';
import {UserService} from '../../services/user.service';
import {ContractService} from '../../../contracts/services/contract.service';

@Component({
  selector: 'app-contract-card',
  imports: [CommonModule],
  templateUrl: './contract-card.component.html',
  styleUrl: './contract-card.component.css'
})
export class ContractCardComponent {
    @Input() contract!: RentalContract;
    @Input() isOwnerView: boolean = false;
    @Output() statusUpdated = new EventEmitter<void>();

    constructor(private userService: UserService,
                private contractService: ContractService) {}

  updateStatus(newStatus: string) {
      this.contractService.updateStatus(this.contract.id, newStatus).subscribe({
        next: () => {
          this.statusUpdated.emit();
        },
        error: () => console.log('Error while updating status: ' + newStatus)
      })

  }

}
