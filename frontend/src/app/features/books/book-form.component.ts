import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BookApiService } from '../../services/book-api.service';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';

@Component({
  standalone: true,
  selector: 'app-book-form',
  imports: [FormsModule, InputTextModule, ButtonModule, DatePickerModule],
  template: `
    <h2>Add Book</h2>
    <form (ngSubmit)="submit()" class="p-fluid">
      <div class="field">
        <label for="title">Title</label>
        <input pInputText id="title" [(ngModel)]="book.title" name="title" required />
      </div>
      <div class="field">
        <label for="isbn">ISBN</label>
        <input pInputText id="isbn" [(ngModel)]="book.isbn" name="isbn" required />
      </div>
      <div class="field">
        <label for="publicationDate">Publication Date</label>
        <p-datepicker
          id="publicationDate"
          [(ngModel)]="book.publicationDate"
          name="publicationDate"
          dateFormat="yy-mm-dd"
        ></p-datepicker>
      </div>
      <button pButton type="submit" label="Save" pButtonIcon="pi pi-check"></button>
    </form>
  `,
})
export class BookFormComponent {
  book = {
    title: '',
    isbn: '',
    publicationDate: new Date().toISOString().split('T')[0],
  };

  constructor(private bookService: BookApiService, private router: Router) {}

  submit() {
    this.bookService.addBook(this.book).subscribe({
      next: () => this.router.navigate(['/books']),
      error: (err) => console.error('Failed to add book', err),
    });
  }
}
