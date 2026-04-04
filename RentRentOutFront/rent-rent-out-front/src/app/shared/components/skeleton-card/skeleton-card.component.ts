import { ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-skeleton-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="skeleton-card" [class.list-view]="listView">
      <div class="skeleton-img shimmer"></div>
      <div class="skeleton-body">
        <div class="skeleton-line long shimmer"></div>
        <div class="skeleton-line medium shimmer"></div>
        <div class="skeleton-line short shimmer"></div>
      </div>
    </div>
  `,
  styleUrl: './skeleton-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SkeletonCardComponent {
  @Input() listView = false;
}
