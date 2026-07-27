import { Component, OnInit } from '@angular/core';
import { SeoService } from '../../../../core/services/seo.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-privacy-policy',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: './privacy-policy.component.html',
  styleUrl: './privacy-policy.component.css'
})
export class PrivacyPolicyComponent implements OnInit {
  constructor(private seoService: SeoService) {}

  ngOnInit(): void {
    this.seoService.setStaticPage(
      'Politika privatnosti',
      'Politika privatnosti platforme Izdajem Iznajmljujem — kako štitimo tvoje podatke u skladu sa ZZPL/GDPR propisima.',
      '/privacy-policy'
    );
  }
}
