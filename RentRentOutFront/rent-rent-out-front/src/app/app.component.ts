import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {NavbarComponent} from './core/layout/navbar/navbar.component';
import {getInstrumentationExcludedPaths} from '@angular-devkit/build-angular/src/tools/webpack/utils/helpers';
import {FooterComponent} from './core/layout/footer/footer.component';
import {HeaderComponent} from './core/layout/header/header.component';
import {AdCardComponent} from './features/ads/components/ad-card/ad-card.component';
import {AdListComponent} from './features/ads/pages/ad-list/ad-list.component';
import {ToastComponent} from './shared/components/toast/toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent, HeaderComponent, AdCardComponent, AdListComponent, ToastComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})

export class AppComponent {
  title = 'rent-rent-out-front';
}
