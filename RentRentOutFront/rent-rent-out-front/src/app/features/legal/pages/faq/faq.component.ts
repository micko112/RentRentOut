import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface FaqItem {
  q: string;
  a: string;
  open?: boolean;
}

interface FaqGroup {
  icon: string;
  title: string;
  items: FaqItem[];
}

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './faq.component.html',
  styleUrl: './faq.component.css'
})
export class FaqComponent {
  groups: FaqGroup[] = [
    {
      icon: 'info',
      title: 'Opšta pitanja',
      items: [
        {
          q: 'Šta je IzdajemIznajmljujem?',
          a: 'IzdajemIznajmljujem je srpska platforma za iznajmljivanje stvari između privatnih lica. Možete iznajmiti ili dati u zakup alate, opremu, vozila, sportske rekvizite, muzičke instrumente i mnoge druge predmete — direktno, bez posrednika.'
        },
        {
          q: 'Ko može da koristi platformu?',
          a: 'Svako lice starije od 18 godina sa validnom email adresom. Registracija je besplatna i traje manje od jednog minuta. Neregistrovani posetioci mogu da pregledaju oglase, ali ne mogu da postavljaju oglase niti da šalju zahteve za iznajmljivanje.'
        },
        {
          q: 'Da li je platforma besplatna?',
          a: 'Registracija i osnovno korišćenje su potpuno besplatni — možete postavljati neograničen broj oglasa, slati zahteve i komunicirati sa korisnicima. Postoji opcija plaćenih promocija oglasa (Istaknuti, Prioritetni, Označeni paket) ukoliko želite veću vidljivost, ali to nije obavezno.'
        },
        {
          q: 'U kojim gradovima je platforma dostupna?',
          a: 'Platforma je dostupna na celoj teritoriji Srbije. Oglasi se mogu pretraživati po lokaciji — gradu ili opštini. Pri postavljanju oglasa navodite grad/lokaciju, a zakupci mogu filtrirati rezultate po blizini.'
        },
        {
          q: 'Da li postoji mobilna aplikacija?',
          a: 'Mobilna aplikacija je u planu razvoja. Trenutno je sajt potpuno prilagođen mobilnim uređajima (responsive dizajn), pa ga možete koristiti sa smartfona ili tableta jednako kao i sa računara.'
        }
      ]
    },
    {
      icon: 'sell',
      title: 'Postavljanje oglasa',
      items: [
        {
          q: 'Kako postavim oglas?',
          a: 'Nakon registracije i prijave, kliknite na "Postavi oglas" u navigaciji ili sidebar-u. Čarobnjak za kreiranje oglasa vodi vas kroz dva koraka: (1) kategorija, naslov i opis, (2) slike, cena, lokacija i raspoloživa količina. Oglas je aktivan 30 dana od postavljanja.'
        },
        {
          q: 'Koliko oglasa mogu da objavim?',
          a: 'Nema ograničenja broja aktivnih oglasa. Možete objaviti onoliko oglasa koliko imate predmeta za iznajmljivanje.'
        },
        {
          q: 'Šta se dešava kad oglas istekne (posle 30 dana)?',
          a: 'Oglas automatski dobija status "Arhivirano" i više nije vidljiv u pretragama. Dobićete email obaveštenje 2–3 dana pre isteka. Iz sekcije "Moji oglasi" možete obnoviti oglas jednim klikom — oglas se vraća u aktivan status sa novim rokom od 30 dana.'
        },
        {
          q: 'Koje slike mogu da dodam na oglas?',
          a: 'Možete dodati do 10 slika po oglasu. Svaka slika može biti do 10 MB (JPG, PNG, WEBP). Preporučujemo jasne, dobro osvetljene fotografije predmeta iz više uglova — oglasi sa kvalitetnim slikama dobijaju znatno više upita.'
        },
        {
          q: 'Mogu li da izmenim oglas nakon objavljivanja?',
          a: 'Da, oglas možete izmeniti u bilo kom trenutku dok je aktivan. Na stranici "Moji oglasi" kliknite "Izmeni" pored željenog oglasa. Sve izmene su trenutno vidljive posetiocima.'
        },
        {
          q: 'Kako funkcioniše kalendar dostupnosti?',
          a: 'Na oglasu postoji interni kalendar koji prikazuje zauzete termine. Kalendar se automatski ažurira kada prihvatite zahtev za određeni period. Pored toga, kao vlasnik oglasa možete ručno blokirati termine (npr. oglas je nedostupan za određeni period).'
        }
      ]
    },
    {
      icon: 'search',
      title: 'Iznajmljivanje i zahtevi',
      items: [
        {
          q: 'Kako pronađem predmet koji tražim?',
          a: 'Na početnoj stranici vidite najnovije oglase po kategorijama. Za preciznu pretragu koristite traku za pretragu ili filtere: kategorija, lokacija, cena (min/max), period dostupnosti. Rezultati se mogu sortirati po datumu, ceni ili relevantnosti.'
        },
        {
          q: 'Kako pošaljem zahtev za iznajmljivanje?',
          a: 'Na stranici oglasa odaberite periode na kalendaru i kliknite "Pošalji zahtev". Zahtev ide iznajmljivaču koji ga prihvata ili odbija. Dok zahtev čeka odluku, možete razmeniti poruke sa iznajmljivačem putem ugrađenog četa.'
        },
        {
          q: 'Šta ako iznajmljivač ne odgovori?',
          a: 'Iznajmljivač dobija email obaveštenje i notifikaciju u aplikaciji. Ukoliko ne odgovori u razumnom roku, možete mu poslati direktnu poruku kroz chat, ili potražiti drugi oglas. Preporučujemo kontakt sa iznajmljivačima koji imaju dobre ocene i visoku stopu odgovora.'
        },
        {
          q: 'Mogu li da iznajmim predmet na duže vreme?',
          a: 'Da. Trajanje iznajmljivanja nije ograničeno platformom — sve zavisi od dogovora između vas i iznajmljivača. Možete se dogovoriti za iznajmljivanje na dan, nedelju, mesec ili duže.'
        }
      ]
    },
    {
      icon: 'description',
      title: 'Ugovori i ocene',
      items: [
        {
          q: 'Šta je ugovor o iznajmljivanju na platformi?',
          a: 'Ugovor je digitalni zapis dogovora između iznajmljivača i zakupca: predmet, period i status. Zakupac šalje zahtev — to je predlog ugovora. Iznajmljivač ga prihvata ili odbija. Prihvaćeni ugovor se čuva i dostupan je obema stranama u sekciji "Moji ugovori". Ugovor je evidencioni alat, ne zamenjuje pisani ugovor koji strane mogu zaključiti posebno.'
        },
        {
          q: 'Kako funkcioniše sistem ocenjivanja?',
          a: 'Nakon završetka iznajmljivanja (status ugovora "Završeno"), svaka strana može ostaviti ocenu drugoj strani — od 1 do 5 zvezdica sa kratkim komentarom. Ocenjivanje je dostupno 30 dana od završetka ugovora. Ocene su javno vidljive na profilu korisnika i pomažu zajednici da identifikuje pouzdane iznajmljivače i zakupce.'
        },
        {
          q: 'Može li iznajmljivač odbiti zahtev bez objašnjenja?',
          a: 'Da, iznajmljivač ima pravo da odbije svaki zahtev bez navođenja razloga. Ako dobijete odbijanje, možete pokušati sa drugim oglasom ili kontaktirati iznajmljivača radi pojašnjenja putem chata.'
        },
        {
          q: 'Šta da radim ako korisnik nije ispunio dogovor?',
          a: 'Platforma nema mehanizam za rešavanje sporova niti posreduje u transakcijama. Preporučujemo: (1) pokušajte da se dogovorite direktno, (2) ostavite iskrenu ocenu kako biste upozorili zajednicu, (3) prijavite oglas ako postoji prevara ili prevara. Za ozbiljne slučajeve, platforma je samo evidencioni alat — pravni put ostaje između strana.'
        }
      ]
    },
    {
      icon: 'payments',
      title: 'Plaćanje i kredit',
      items: [
        {
          q: 'Kako se vrši plaćanje za iznajmljivanje?',
          a: 'Platforma ne obrađuje plaćanja između korisnika. Iznajmljivač i zakupac se dogovaraju direktno o načinu plaćanja — gotovinom pri preuzimanju, bankovnim prenosom, ili drugim dogovorom. Cena na oglasu je informativna i može biti predmet dogovora.'
        },
        {
          q: 'Šta je platni kredit i čemu služi?',
          a: 'Kredit je interni saldo koji se koristi isključivo za plaćanje promocija vaših oglasa. Ne može se koristiti za plaćanje iznajmljivanja niti se može isplatiti. Kredit dodajete ručnim putem (kontakt sa podrškom) ili ga možete dobiti kao bonus. Stanje kredita vidite na stranici "Kredit".'
        },
        {
          q: 'Koji su paketi promocija i koliko koštaju?',
          a: 'Postoje tri paketa:\n• Istaknuti (FEATURED) — 500 RSD, 7 dana, oglas se pojavljuje na vrhu pretrage\n• Prioritetni (PRIORITY) — 250 RSD, 3 dana, oglas je ispred standardnih oglasa\n• Označeni (HIGHLIGHTED) — 100 RSD, 30 dana, vizuelno istaknuta kartica oglasa\nPromociju aktivirate iz "Moji oglasi" → dugme "Promoviši".'
        },
        {
          q: 'Da li se kredit vraća ako povučem oglas?',
          a: 'Ne. Kredit potrošen na promociju se ne vraća ako se oglas obriše ili arhivira pre isteka promocije. Budite sigurni da je oglas aktivan pre aktiviranja promocije.'
        }
      ]
    },
    {
      icon: 'manage_accounts',
      title: 'Nalog i bezbednost',
      items: [
        {
          q: 'Kako promenim lozinku?',
          a: 'Ukoliko znate trenutnu lozinku: idite na "Moj nalog" (sidebar → Moj Nalog) i izmenite lozinku u sekciji za podešavanja. Ukoliko ste zaboravili lozinku: na stranici za prijavu kliknite "Zaboravili ste lozinku?" i pratite uputstvo koje stigne na email.'
        },
        {
          q: 'Kako mogu da izmenim email adresu ili telefon?',
          a: 'Email adresa se trenutno ne može menjati nakon registracije radi sigurnosti. Broj telefona i ostale lične podatke možete izmeniti na stranici "Moj nalog". Broj telefona je enkriptovan i vidljiv samo zainteresovanim korisnicima koji kliknu "Prikaži broj" na oglasu.'
        },
        {
          q: 'Da li su moji podaci sigurni?',
          a: 'Da. Platforma koristi HTTPS enkripciju za sav saobraćaj, lozinke se čuvaju kao bcrypt hash (nikada plain text), telefonski brojevi su enkriptovani AES-256, a JWT sesijski tokoni su HttpOnly kolačići (zaštićeni od XSS napada). Više detalja u Politici privatnosti.'
        },
        {
          q: 'Šta je verifikacija identiteta?',
          a: 'Verifikacija identiteta je opcioni proces kojim potvrđujete da ste stvarna osoba. Verifikovani korisnici dobijaju oznaku na profilu, što povećava poverenje iznajmljivača i zakupaca. Verifikacija se vrši putem zvanične dokumentacije. Idite na "Verifikacija" u sidebar-u.'
        },
        {
          q: 'Kako mogu da obrišem nalog?',
          a: 'Za brisanje naloga pošaljite zahtev na email izdajemiznajmljujem.rs@gmail.com sa predmetom "Zahtev za brisanje naloga". U roku od 30 dana svi vaši podaci biće trajno obrisani, u skladu sa GDPR/ZZPL regulativom. Napomena: aktivni oglasi, ugovori i ocene biće anonimizovani, ne obrisani, kako bi se sačuvao integritet evidencije.'
        },
        {
          q: 'Kako da prijavim sumnjivog korisnika ili oglas?',
          a: 'Za prijavu oglasa: koristite dugme "Prijavi oglas" direktno na stranici tog oglasa — navedite razlog i opis problema. Za prijavu korisnika: pišite nam na izdajemiznajmljujem.rs@gmail.com. Sve prijave pregleda naš tim, obično u roku od 1–2 radna dana.'
        }
      ]
    }
  ];

  toggle(group: FaqGroup, item: FaqItem): void {
    item.open = !item.open;
  }
}
