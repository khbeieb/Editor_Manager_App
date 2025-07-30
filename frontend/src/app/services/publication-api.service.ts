// src/app/services/publication-api.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import {PublicationGroupedResponse} from '../models/publication.model';

@Injectable({
  providedIn: 'root',
})
export class PublicationApiService {
  private readonly baseUrl = `${environment.apiUrl}/publications/grouped`;

  constructor(private http: HttpClient) {}

  getGroupedPublications(): Observable<PublicationGroupedResponse> {
    return this.http.get<PublicationGroupedResponse>(this.baseUrl);
  }
}
