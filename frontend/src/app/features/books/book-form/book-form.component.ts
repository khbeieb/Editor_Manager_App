import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BookApiService } from '../../../services/book-api.service';
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
  templateUrl: './book-form.component.html',
  styleUrls: ['./book-form.component.scss']
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
