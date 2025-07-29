import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BookApiService } from '../../services/book-api.service';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-book-form',
  imports: [
    CommonModule,
    FormsModule,
    InputTextModule,
    ButtonModule,
    DatePickerModule,
    CardModule,
    ToastModule,
    ConfirmDialogModule
  ],
  providers: [MessageService, ConfirmationService],
  template: `
    <div class="book-form-container">
      <p-toast></p-toast>
      <p-confirmDialog></p-confirmDialog>

      <div class="form-wrapper">
        <p-card
          header="Add New Book"
          subheader="Fill in the book details"
          styleClass="book-form-card"
        >
          <form (ngSubmit)="submit()" #bookForm="ngForm" class="book-form">
            <div class="form-field">
              <label for="title" class="required-field">Title</label>
              <input
                pInputText
                id="title"
                name="title"
                [(ngModel)]="book.title"
                required
                minlength="2"
                maxlength="200"
                placeholder="Enter book title"
                class="form-input"
              />
            </div>

            <div class="form-field">
              <label for="isbn" class="required-field">ISBN</label>
              <input
                pInputText
                id="isbn"
                name="isbn"
                [(ngModel)]="book.isbn"
                required
                maxlength="20"
                placeholder="Enter ISBN"
                class="form-input"
              />
            </div>

            <div class="form-field">
              <label for="publicationDate">Publication Date</label>
              <p-datepicker
                id="publicationDate"
                name="publicationDate"
                [(ngModel)]="book.publicationDate"
                dateFormat="dd/mm/yy"
                showIcon
                [maxDate]="today"
                inputId="book-publication-date-input"
                styleClass="form-input"
              ></p-datepicker>
            </div>

            <div class="form-field">
              <label for="authorId" class="required-field">Author ID</label>
              <input
                pInputText
                id="authorId"
                name="authorId"
                type="number"
                [(ngModel)]="book.authorId"
                required
                placeholder="Enter Author ID"
                class="form-input"
              />
            </div>

            <div class="form-actions">
              <button
                pButton
                type="button"
                label="Cancel"
                icon="pi pi-times"
                class="p-button-secondary p-button-outlined"
                (click)="onCancel()"
              ></button>

              <button
                pButton
                type="submit"
                label="Save Book"
                icon="pi pi-check"
                class="p-button-primary"
                [disabled]="!bookForm.valid || isSubmitting"
                [loading]="isSubmitting"
              ></button>
            </div>
          </form>
        </p-card>
      </div>
    </div>
  `,
  styles: [`
    .book-form-container {
      min-height: 100vh;
      background: linear-gradient(135deg, #6dd5fa 0%, #2980b9 100%);
      padding: 2rem 1rem;
    }

    .form-wrapper {
      max-width: 600px;
      margin: 0 auto;
    }

    .book-form-card {
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
      border-radius: 16px;
      overflow: hidden;
    }

    .book-form {
      padding: 2rem;
    }

    .form-field {
      display: flex;
      flex-direction: column;
      margin-bottom: 1.5rem;
    }

    .form-field label {
      font-weight: 500;
      margin-bottom: 0.5rem;
      color: #2c3e50;
    }

    .required-field::after {
      content: " *";
      color: #e74c3c;
    }

    .form-input:focus {
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
    }

    .form-actions {
      display: flex;
      justify-content: space-between;
      margin-top: 2rem;
    }

    @media (max-width: 768px) {
      .form-actions {
        flex-direction: column;
        gap: 1rem;
      }
    }
  `]
})
export class BookFormComponent {
  book = {
    title: '',
    isbn: '',
    publicationDate: new Date(),
    authorId: null as number | null
  };

  today = new Date();
  isSubmitting = false;

  constructor(
    private bookService: BookApiService,
    private router: Router,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

    async submit(): Promise<void> {
      if (this.isSubmitting) return;

    this.isSubmitting = true;

    try {
      const payload = {
        ...this.book,
        publicationDate: this.book.publicationDate.toISOString().split('T')[0],
        author: { id: this.book.authorId }
      };

      const response = await this.bookService.addBook(payload).toPromise();
      if (response?.statusCode === 201 && response.data) {
        this.messageService.add({
          severity: 'success',
          summary: 'Book Added',
          detail: `"${this.book.title}" has been successfully saved.`,
          life: 3000
        });

        setTimeout(() => {
          this.router.navigate(['/books']);
        }, 1500);
      } else {
        throw new Error(response?.message || 'Failed to add book');
      }

    } catch (error: any) {
      console.error('Failed to add book:', error);
      this.messageService.add({
        severity: 'error',
        summary: 'Failed to Add Book',
        detail: error?.error?.message || error?.message || 'Unexpected error occurred.',
        life: 5000
      });
    } finally {
      this.isSubmitting = false;
    }
  }

  onCancel(): void {
    this.confirmationService.confirm({
      message: 'Are you sure you want to cancel? All unsaved changes will be lost.',
      header: 'Cancel Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.router.navigate(['/books']);
      }
    });
  }
}
