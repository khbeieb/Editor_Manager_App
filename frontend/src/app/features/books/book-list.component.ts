import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookApiService } from '../../services/book-api.service';
import { Book } from '../../models/book.model';
import { TableModule } from 'primeng/table';
import {ButtonDirective, ButtonIcon, ButtonLabel} from 'primeng/button';
import {RouterLink} from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-book-list',
  imports: [CommonModule, TableModule, ButtonDirective, ButtonIcon, RouterLink, ButtonLabel],
  template: `
    <h2>Book List</h2>
    <p-table [value]="books" [paginator]="true" [rows]="5">
      <ng-template pTemplate="header">
        <tr>
          <th>Title</th>
          <th>ISBN</th>
          <th>Publication Date</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-book>
        <tr>
          <td>{{ book.title }}</td>
          <td>{{ book.isbn }}</td>
          <td>{{ book.publicationDate }}</td>
        </tr>
      </ng-template>
    </p-table>
    <button
      pButton
      type="button"
      label="Add Book"
      icon="pi pi-plus"
      routerLink="/books/new">
    </button>  `,
})
export class BookListComponent implements OnInit {
  books: Book[] = [];

  constructor(private bookService: BookApiService) {}

  ngOnInit(): void {
    this.bookService.getBooks().subscribe({
      next: (res) => (this.books = res.data ?? []),
      error: (err) => console.error('Failed to load books', err),
    });
  }
}
