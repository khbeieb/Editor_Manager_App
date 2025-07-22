import {Component, OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {AuthorApiService} from './services/author-api.service';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NgIf, NgForOf],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  authors: any[] = [];

  constructor(private authorApi: AuthorApiService) {}

  ngOnInit(): void {
    this.authorApi.getAuthors().subscribe({
      next: (res: any) => {
        console.log('✅ Response:', res);
        this.authors = res.data;
      },
      error: (err) => console.error('❌ Error:', err)
    });
  }
}
