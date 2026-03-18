import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-placeholder-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './placeholder-page.component.html',
  styleUrl: './placeholder-page.component.css'
})
export class PlaceholderPageComponent implements OnInit {
  title = 'Stranica u pripremi';
  description = 'Ova sekcija će uskoro biti dostupna.';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    const data = this.route.snapshot.data || {};
    if (data['title']) {
      this.title = data['title'];
    }
    if (data['description']) {
      this.description = data['description'];
    }
  }
}
