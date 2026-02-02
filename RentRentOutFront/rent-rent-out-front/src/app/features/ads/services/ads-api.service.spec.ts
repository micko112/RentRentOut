import { TestBed } from '@angular/core/testing';

import { AdsApiService } from './ads-api.service';

describe('AdsApiService', () => {
  let service: AdsApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AdsApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
