import { Component, OnInit } from '@angular/core';
import { SeoService } from '../../../../core/services/seo.service';

@Component({
  selector: 'app-terms-of-service',
  standalone: true,
  templateUrl: './terms-of-service.component.html',
  styleUrl: './terms-of-service.component.css'
})
export class TermsOfServiceComponent implements OnInit {
  constructor(private seoService: SeoService) {}

  ngOnInit(): void {
    this.seoService.setStaticPage(
      'Uslovi korišćenja',
      'Pročitaj uslove korišćenja platforme Izdajem Iznajmljujem — pravila za iznajmljivanje, odgovornosti i pakete promocija.',
      '/terms-of-service'
    );
  }
}
