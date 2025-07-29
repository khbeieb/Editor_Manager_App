import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {catchError, Observable, of} from 'rxjs';
import {environment} from '../../environments/environment';
import {ApiResponse} from '../models/api-response.model';
import {Author} from '../models/author.model';

@Injectable({
  providedIn: 'root'
})

export class AuthorApiService {
  constructor(private http: HttpClient) {}

  getAuthors(): Observable<ApiResponse<Author[]>> {
    return this.http.get<ApiResponse<Author[]>>(`${environment.apiUrl}/authors`);
  }

  addAuthor(author: Author): Observable<ApiResponse<Author>> {
    return this.http.post<ApiResponse<Author>>(`${environment.apiUrl}/authors`, author).pipe(
      catchError((error) => {
        console.error('Error adding new author:', error);
        return of({
          statusCode: 500,
          message: 'Failed to add author',
          data: null,
          timestamp: new Date().toISOString(),
        } as ApiResponse<Author>);
      })
    );
  }
}
