import { Injectable, inject } from '@angular/core';
import { Title, Meta } from '@angular/platform-browser';
import { DOCUMENT } from '@angular/common';
import { Ad } from '../../shared/models/ad.model';
import { UserProfile } from '../../shared/models/userProfile';

const BASE_URL = 'https://izdajemiznajmljujem.com';
const DEFAULT_TITLE = 'Izdajem Iznajmljujem';
const DEFAULT_DESC = 'Iznajmi sve što ti treba — alati, tehnika, oprema. Besplatno objavi oglas.';
const DEFAULT_IMAGE = `${BASE_URL}/assets/images/placeholder.png`;

@Injectable({ providedIn: 'root' })
export class SeoService {
  private titleSvc = inject(Title);
  private meta    = inject(Meta);
  private doc     = inject(DOCUMENT);

  setPage(params: {
    title: string;
    description: string;
    canonicalPath?: string;
    ogImage?: string;
    ogType?: string;
  }): void {
    const { title, description, canonicalPath = '', ogImage = DEFAULT_IMAGE, ogType = 'website' } = params;
    const fullTitle = title === DEFAULT_TITLE ? title : `${title} | Izdajem Iznajmljujem`;
    const url = `${BASE_URL}${canonicalPath}`;

    this.titleSvc.setTitle(fullTitle);
    this.meta.updateTag({ name: 'description',           content: description });
    this.meta.updateTag({ property: 'og:title',          content: fullTitle   });
    this.meta.updateTag({ property: 'og:description',    content: description });
    this.meta.updateTag({ property: 'og:image',          content: ogImage     });
    this.meta.updateTag({ property: 'og:url',            content: url         });
    this.meta.updateTag({ property: 'og:type',           content: ogType      });
    this.meta.updateTag({ name: 'twitter:title',         content: fullTitle   });
    this.meta.updateTag({ name: 'twitter:description',   content: description });
    this.meta.updateTag({ name: 'twitter:image',         content: ogImage     });
    this.setCanonical(url);
  }

  setAdPage(ad: Ad): void {
    const raw = ad.description ?? '';
    const desc = raw.length
      ? raw.substring(0, 155).replace(/\s+/g, ' ').trim() + (raw.length > 155 ? '...' : '')
      : `Iznajmi ${ad.title} u ${ad.location?.city ?? 'Srbiji'}. Pogledaj oglas na Izdajem Iznajmljujem.`;

    this.setPage({
      title:         ad.title,
      description:   desc,
      canonicalPath: `/ads/${ad.id}`,
      ogImage:       ad.images?.length > 0 ? ad.images[0] : DEFAULT_IMAGE,
      ogType:        'product',
    });
    this.setJsonLd(this.buildAdSchema(ad));
  }

  setUserProfilePage(user: UserProfile): void {
    const desc = user.description?.length
      ? user.description.substring(0, 155).replace(/\s+/g, ' ').trim()
      : `Pogledaj profil korisnika ${user.displayName} na Izdajem Iznajmljujem.`;

    this.setPage({
      title:         `${user.displayName} — profil iznajmljivača`,
      description:   desc,
      canonicalPath: `/user/${user.id}/reviews`,
      ogImage:       user.avatarUrl || DEFAULT_IMAGE,
    });
    this.setJsonLd(this.buildPersonSchema(user));
  }

  setSearchPage(params: {
    categoryName?: string;
    keyword?: string;
    city?: string;
    totalResults?: number;
  }): void {
    const { categoryName, keyword, city, totalResults } = params;
    const loc = city ? ` — ${city}` : '';
    const inLoc = city ? ` u ${city}` : ' u Srbiji';
    let title: string;
    let desc: string;

    if (categoryName && categoryName !== 'Svi oglasi' && categoryName !== 'Najnoviji oglasi') {
      title = `${categoryName} za iznajmljivanje${loc}`;
      desc  = `Iznajmi ${categoryName.toLowerCase()}${inLoc}. ${totalResults ? totalResults + ' oglasa dostupno' : 'Pogledaj oglase'} na Izdajem Iznajmljujem.`;
    } else if (keyword) {
      title = `"${keyword}" za iznajmljivanje${loc}`;
      desc  = `Rezultati pretrage za "${keyword}"${inLoc} na Izdajem Iznajmljujem.`;
    } else {
      title = city ? `Oglasi za iznajmljivanje${loc}` : 'Svi oglasi za iznajmljivanje';
      desc  = `Iznajmi sve što ti treba${inLoc} — alati, tehnika, oprema i još mnogo toga.`;
    }

    this.setPage({ title, description: desc, canonicalPath: '/ads' });
  }

  setHomePage(): void {
    this.setPage({
      title:         DEFAULT_TITLE,
      description:   DEFAULT_DESC,
      canonicalPath: '/',
    });
    this.setJsonLd(this.buildWebSiteSchema());
  }

  setStaticPage(title: string, description: string, canonicalPath: string): void {
    this.setPage({ title, description, canonicalPath });
  }

  reset(): void {
    this.titleSvc.setTitle(DEFAULT_TITLE);
    this.meta.updateTag({ name: 'description',         content: DEFAULT_DESC   });
    this.meta.updateTag({ property: 'og:title',        content: DEFAULT_TITLE  });
    this.meta.updateTag({ property: 'og:description',  content: DEFAULT_DESC   });
    this.meta.updateTag({ property: 'og:image',        content: DEFAULT_IMAGE  });
    this.meta.updateTag({ property: 'og:url',          content: BASE_URL       });
    this.meta.updateTag({ property: 'og:type',         content: 'website'      });
    this.meta.updateTag({ name: 'twitter:title',       content: DEFAULT_TITLE  });
    this.meta.updateTag({ name: 'twitter:description', content: DEFAULT_DESC   });
    this.meta.updateTag({ name: 'twitter:image',       content: DEFAULT_IMAGE  });
    this.setCanonical(BASE_URL);
    this.removeJsonLd();
  }

  // ── Helpers ────────────────────────────────────────────

  private setCanonical(url: string): void {
    let link = this.doc.querySelector<HTMLLinkElement>('link[rel="canonical"]');
    if (!link) {
      link = this.doc.createElement('link');
      link.setAttribute('rel', 'canonical');
      this.doc.head.appendChild(link);
    }
    link.setAttribute('href', url);
  }

  private setJsonLd(schema: object): void {
    this.removeJsonLd();
    const script = this.doc.createElement('script');
    script.type = 'application/ld+json';
    script.id   = 'seo-json-ld';
    script.text = JSON.stringify(schema);
    this.doc.head.appendChild(script);
  }

  private removeJsonLd(): void {
    this.doc.getElementById('seo-json-ld')?.remove();
  }

  // ── Schema builders ────────────────────────────────────

  private buildAdSchema(ad: Ad): object {
    const url   = `${BASE_URL}/ads/${ad.id}`;
    const image = ad.images?.length > 0 ? ad.images[0] : DEFAULT_IMAGE;
    return {
      '@context': 'https://schema.org',
      '@type': 'Product',
      name: ad.title,
      ...(ad.description ? { description: ad.description } : {}),
      image: ad.images?.length > 0 ? ad.images : [image],
      url,
      ...(ad.category?.name ? { category: ad.category.name } : {}),
      offers: {
        '@type':        'Offer',
        priceCurrency:  ad.currency ?? 'RSD',
        price:          ad.price,
        availability:   ad.availableQuantity > 0
                          ? 'https://schema.org/InStock'
                          : 'https://schema.org/OutOfStock',
        url,
        ...(ad.location?.city ? { areaServed: { '@type': 'City', name: ad.location.city } } : {}),
        seller: {
          '@type': 'Person',
          name: ad.owner?.displayName ?? 'Korisnik'
        }
      }
    };
  }

  private buildPersonSchema(user: UserProfile): object {
    return {
      '@context': 'https://schema.org',
      '@type':    'Person',
      name:        user.displayName,
      url:        `${BASE_URL}/user/${user.id}/reviews`,
      ...(user.avatarUrl       ? { image:       user.avatarUrl       } : {}),
      ...(user.description     ? { description: user.description     } : {}),
      ...(user.locationDisplay ? { address:     user.locationDisplay } : {})
    };
  }

  private buildWebSiteSchema(): object {
    return {
      '@context':   'https://schema.org',
      '@type':      'WebSite',
      name:         'Izdajem Iznajmljujem',
      url:          BASE_URL,
      description:  DEFAULT_DESC,
      inLanguage:   'sr',
      potentialAction: {
        '@type': 'SearchAction',
        target: {
          '@type':      'EntryPoint',
          urlTemplate:  `${BASE_URL}/ads?keyword={search_term_string}`
        },
        'query-input': 'required name=search_term_string'
      }
    };
  }
}
