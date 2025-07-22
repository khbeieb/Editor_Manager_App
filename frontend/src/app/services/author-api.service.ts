import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthorApiService {

  constructor(private http: HttpClient) {  }

  getAuthors(): Observable<any> {
    console.log("khaleeeed", `${environment.apiUrl}/authors`)
    return this.http.get(`${environment.apiUrl}/authors`)
  }
}
