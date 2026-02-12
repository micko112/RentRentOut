import {Component, Input, input} from '@angular/core';
import {RentalContract} from '../../../../shared/models/rental-contract.model';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-contract-card',
  imports: [CommonModule],
  templateUrl: './contract-card.component.html',
  styleUrl: './contract-card.component.css'
})
export class ContractCardComponent {
    @Input() contract!: RentalContract;

}
