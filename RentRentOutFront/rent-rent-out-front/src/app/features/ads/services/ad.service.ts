import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {AdPreview, Page} from '../../../shared/models/adPreview.model';
import {Observable} from 'rxjs';
import {API_BASE_URL} from '../../../core/config/api.config';
import {AdSearchCriteria} from '../../../shared/models/adSearchCriteria';
import {Ad} from '../../../shared/models/ad.model';
import {UpdateAdRequest} from '../../../shared/models/update-ad-request';

@Injectable({
  providedIn: 'root'
})
export class AdService {


  constructor(private http: HttpClient) { }
  private adApiUrl = `${API_BASE_URL}/ads`;
  search(criteria: AdSearchCriteria) : Observable<Page<AdPreview>> {
    let params = new HttpParams();

    if(criteria.keyword){
      params = params.append('keyword', criteria.keyword)
    }
    if (criteria.categoryId) params = params.append('categoryId', criteria.categoryId.toString());
    if (criteria.minPrice) params = params.append('minPrice', criteria.minPrice.toString());
    if (criteria.maxPrice) params = params.append('maxPrice', criteria.maxPrice.toString());
    if (criteria.locationId) params = params.append('locationId', criteria.locationId);

    if (criteria.page) params = params.append('page', criteria.page.toString());
    if (criteria.size) params = params.append('size', criteria.size.toString());
    if (criteria.sort) params = params.append('sort', criteria.sort);


    return this.http.get<Page<AdPreview>>(`${this.adApiUrl}/search`, { params });
  }
  getAdById(id: number) : Observable<Ad> {
    return this.http.get<Ad>(`${this.adApiUrl}/${id}`);
  }
  createAd(ad: Ad) :Observable<Ad> {

    return this.http.post<Ad>(`${this.adApiUrl}`, ad);
  }

  updateAd(id: number, payload: UpdateAdRequest): Observable<Ad> {
    return this.http.put<Ad>(`${this.adApiUrl}/${id}`, payload);
  }

  getMyAds(page: number = 0, size: number=10 ): Observable<Page<AdPreview>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<AdPreview>>(`${this.adApiUrl}/me`, {params})
  }

  public uploadImages(files: File[]): Observable<string[]> {
    const formData = new FormData();
    files.forEach(file => {
      formData.append('files', file);
    });

    return this.http.post<string[]>(`${API_BASE_URL}/images/upload`, formData);
  }
  deleteAd(id: number) : Observable<string> {
    return this.http.delete(`${this.adApiUrl}/${id}`, {responseType: 'text'});

  }
  getAdsByUser(userId: number) : Observable<Page<AdPreview>> {
    return this.http.get<Page<AdPreview>>(`${this.adApiUrl}/user/${userId}`);
  }
}
