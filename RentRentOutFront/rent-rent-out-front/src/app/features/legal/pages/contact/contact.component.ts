import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SeoService } from '../../../../core/services/seo.service';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.css'
})
export class ContactComponent implements OnInit {
  constructor(private seoService: SeoService) {}

  ngOnInit(): void {
    this.seoService.setStaticPage(
      'Kontakt',
      'Kontaktiraj tim Izdajem Iznajmljujem za podršku, pitanja ili prijavu problema.',
      '/contact'
    );
  }
}
