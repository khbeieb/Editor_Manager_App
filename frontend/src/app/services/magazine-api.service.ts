import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Magazine } from '../models/magazine.model';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root',
})
export class MagazineApiService {
  private readonly baseUrl = `${environment.apiUrl}/magazines`;

  constructor(private http: HttpClient) {}

  getMagazines(): Observable<ApiResponse<Magazine[]>> {
    return this.http
      .get<ApiResponse<Magazine[]>>(this.baseUrl)
      .pipe(catchError(this.handleError));
  }

  addMagazine(payload: any): Observable<ApiResponse<Magazine>> {
    return this.http
      .post<ApiResponse<Magazine>>(this.baseUrl, payload)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    console.error('[MagazineApiService] Error:', error);
    return throwError(() =>
      new Error(
        error?.error?.message || 'An unexpected error occurred while processing your request.'
      )
    );
  }
}
