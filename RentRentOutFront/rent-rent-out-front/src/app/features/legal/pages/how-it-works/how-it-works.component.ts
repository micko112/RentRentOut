import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SeoService } from '../../../../core/services/seo.service';

@Component({
  selector: 'app-how-it-works',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './how-it-works.component.html',
  styleUrl: './how-it-works.component.css'
})
export class HowItWorksComponent implements OnInit {
  constructor(private seoService: SeoService) {}

  ngOnInit(): void {
    this.seoService.setStaticPage(
      'Kako funkcioniše',
      'Saznaj kako da iznajmiš ili iznajmljuješ opremu, alate i tehničke uređaje na Izdajem Iznajmljujem.',
      '/how-it-works'
    );
  }
}
