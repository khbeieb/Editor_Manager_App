import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Book } from '../models/book.model';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root',
})
export class BookApiService {
  constructor(private http: HttpClient) {}

  getBooks(): Observable<ApiResponse<Book[]>> {
    return this.http.get<ApiResponse<Book[]>>(`${environment.apiUrl}/books`);
  }

  addBook(book: Book): Observable<ApiResponse<Book>> {
    return this.http.post<ApiResponse<Book>>(`${environment.apiUrl}/books`, book).pipe(
      catchError((error) => {
        console.error('Error adding book:', error);
        return of({
          statusCode: 500,
          message: 'Failed to add book',
          data: null as any,
          timestamp: new Date().toISOString(),
        });
      })
    );
  }
}
