import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthorApiService } from '../../../services/author-api.service';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import {Tooltip} from 'primeng/tooltip';

interface Book {
  title: string;
  isbn: string;
  publicationDate: string;
}

interface Author {
  name: string;
  birthDate: Date;
  nationality: string;
  books: Book[];
}

@Component({
  standalone: true,
  selector: 'app-author-form',
  imports: [
    CommonModule,
    FormsModule,
    InputTextModule,
    ButtonModule,
    DatePickerModule,
    TableModule,
    CardModule,
    DividerModule,
    ToastModule,
    ConfirmDialogModule,
    Tooltip,
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './author-form.component.html',
  styleUrls: ['./author-form.component.scss']
})
export class AuthorFormComponent {
  author: Author = {
    name: '',
    birthDate: new Date(),
    nationality: '',
    books: [],
  };

  newBook: Book = {
    title: '',
    isbn: '',
    publicationDate: '',
  };

  isSubmitting = false;
  today = new Date();
  maxBirthDate = new Date();
  minBirthDate = new Date();

  constructor(
    private authorService: AuthorApiService,
    private router: Router,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {
    // Set birth date limits: between 120 years ago and today
    this.minBirthDate.setFullYear(this.minBirthDate.getFullYear() - 120);

  }

  isBookFormValid(): boolean {
    return !!(
      this.newBook.title?.trim() &&
      this.newBook.isbn?.trim() &&
      this.newBook.publicationDate
    );
  }

  addBook(): void {
    if (!this.isBookFormValid()) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Incomplete Book Information',
        detail: 'Please fill in all book fields (Title, ISBN, and Publication Date).',
        life: 4000
      });
      return;
    }

    // Check for duplicate ISBN
    const existingBook = this.author.books.find(book => book.isbn === this.newBook.isbn);
    if (existingBook) {
      this.messageService.add({
        severity: 'error',
        summary: 'Duplicate ISBN',
        detail: 'A book with this ISBN already exists in the list.',
        life: 4000
      });
      return;
    }

    this.author.books.push({ ...this.newBook });
    this.resetNewBookForm();

    this.messageService.add({
      severity: 'success',
      summary: 'Book Added',
      detail: `"${this.newBook.title}" has been added to the author's book list.`,
      life: 3000
    });
  }

  confirmRemoveBook(index: number): void {
    const book = this.author.books[index];

    this.confirmationService.confirm({
      message: `Are you sure you want to remove "${book.title}" from the list?`,
      header: 'Confirm Removal',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-secondary p-button-outlined',
      accept: () => {
        this.removeBook(index);
      }
    });
  }

  removeBook(index: number): void {
    const removedBook = this.author.books[index];
    this.author.books.splice(index, 1);

    this.messageService.add({
      severity: 'info',
      summary: 'Book Removed',
      detail: `"${removedBook.title}" has been removed from the list.`,
      life: 3000
    });
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-GB'); // DD/MM/YYYY format
  }

  resetNewBookForm(): void {
    this.newBook = {
      title: '',
      isbn: '',
      publicationDate: '',
    };
  }

  onCancel(): void {
    this.confirmationService.confirm({
      message: 'Are you sure you want to cancel? All unsaved changes will be lost.',
      header: 'Confirm Cancel',
      icon: 'pi pi-question-circle',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-secondary p-button-outlined',
      accept: () => {
        this.router.navigate(['/authors']);
      }
    });
  }

  async onSubmit(): Promise<void> {
    if (this.isSubmitting) return;

    this.isSubmitting = true;

    try {
      const payload = {
        ...this.author,
        birthDate: this.author.birthDate.toISOString().split('T')[0],
      };

      const response = await this.authorService.addAuthor(payload).toPromise();

      if (response?.statusCode === 201 && response.data) {
        this.messageService.add({
          severity: 'success',
          summary: 'Author Created',
          detail: `${this.author.name} has been successfully added with ${this.author.books.length} book(s).`,
          life: 4000
        });

        // Navigate after a brief delay to show the success message
        setTimeout(() => {
          this.router.navigate(['/authors']);
        }, 1500);
      } else {
        throw new Error(response?.message || 'Failed to create author');
      }
    } catch (error: any) {
      console.error('Failed to add author:', error);

      this.messageService.add({
        severity: 'error',
        summary: 'Creation Failed',
        detail: error.message || 'Failed to create author. Please try again.',
        life: 5000
      });
    } finally {
      this.isSubmitting = false;
    }
  }
}
