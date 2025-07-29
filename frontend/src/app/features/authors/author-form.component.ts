import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthorApiService } from '../../services/author-api.service';
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
  template: `
    <div class="author-form-container">
      <p-toast data-testid="toast-messages"></p-toast>
      <p-confirmDialog data-testid="confirm-dialog"></p-confirmDialog>

      <div class="form-wrapper">
        <p-card
          header="Add New Author"
          subheader="Create a new author profile with their published books"
          styleClass="author-form-card"
          data-testid="author-form-card"
        >
          <form
            (ngSubmit)="onSubmit()"
            #authorForm="ngForm"
            class="author-form"
            data-testid="author-form"
            [attr.data-form-valid]="authorForm.valid"
          >
            <!-- Author Information Section -->
            <div class="form-section">
              <h3 class="section-title">
                <i class="pi pi-user"></i>
                Author Information
              </h3>

              <div class="form-grid">
                <div class="form-field">
                  <label for="author-name" class="required-field">Full Name</label>
                  <input
                    pInputText
                    id="author-name"
                    [(ngModel)]="author.name"
                    name="authorName"
                    required
                    minlength="2"
                    maxlength="100"
                    placeholder="Enter author's full name"
                    class="form-input"
                    data-testid="author-name-input"
                    #nameField="ngModel"
                    [class.ng-invalid]="nameField.invalid && nameField.touched"
                  />
                  <small
                    class="error-message"
                    *ngIf="nameField.invalid && nameField.touched"
                    data-testid="name-error"
                  >
                    Name is required (2-100 characters)
                  </small>
                </div>

                <div class="form-field">
                  <label for="author-nationality" class="required-field">Nationality</label>
                  <input
                    pInputText
                    id="author-nationality"
                    [(ngModel)]="author.nationality"
                    name="authorNationality"
                    required
                    minlength="2"
                    maxlength="50"
                    placeholder="Enter nationality"
                    class="form-input"
                    data-testid="author-nationality-input"
                    #nationalityField="ngModel"
                    [class.ng-invalid]="nationalityField.invalid && nationalityField.touched"
                  />
                  <small
                    class="error-message"
                    *ngIf="nationalityField.invalid && nationalityField.touched"
                    data-testid="nationality-error"
                  >
                    Nationality is required (2-50 characters)
                  </small>
                </div>

                <div class="form-field full-width">
                  <label for="author-birth-date">Birth Date</label>
                  <p-datepicker
                    id="author-birth-date"
                    [(ngModel)]="author.birthDate"
                    name="authorBirthDate"
                    dateFormat="dd/mm/yy"
                    showIcon
                    [maxDate]="maxBirthDate"
                    [minDate]="minBirthDate"
                    placeholder="Select birth date"
                    inputId="author-birth-date-input"
                    styleClass="form-input"
                    data-testid="author-birth-date-picker"
                  ></p-datepicker>
                </div>
              </div>
            </div>

            <p-divider></p-divider>

            <!-- Books Section -->
            <div class="form-section">
              <h3 class="section-title">
                <i class="pi pi-book"></i>
                Published Books
              </h3>

              <!-- Add Book Form -->
              <div class="add-book-section">
                <h4 class="subsection-title">Add a Book</h4>
                <div class="book-form-grid">
                  <div class="form-field">
                    <label for="book-title">Book Title</label>
                    <input
                      pInputText
                      id="book-title"
                      [(ngModel)]="newBook.title"
                      name="bookTitle"
                      placeholder="Enter book title"
                      class="form-input"
                      data-testid="book-title-input"
                      maxlength="200"
                    />
                  </div>

                  <div class="form-field">
                    <label for="book-isbn">ISBN</label>
                    <input
                      pInputText
                      id="book-isbn"
                      [(ngModel)]="newBook.isbn"
                      name="bookIsbn"
                      placeholder="Enter ISBN"
                      class="form-input"
                      data-testid="book-isbn-input"
                      pattern="^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$"
                    />
                  </div>

                  <div class="form-field">
                    <label for="book-publication-date">Publication Date</label>
                    <p-datepicker
                      id="book-publication-date"
                      [(ngModel)]="newBook.publicationDate"
                      name="bookPublicationDate"
                      dateFormat="dd/mm/yy"
                      showIcon
                      [maxDate]="today"
                      placeholder="Select publication date"
                      inputId="book-publication-date-input"
                      styleClass="form-input"
                      data-testid="book-publication-date-picker"
                    ></p-datepicker>
                  </div>
                </div>

                <div class="add-book-actions">
                  <button
                    pButton
                    type="button"
                    icon="pi pi-plus"
                    label="Add Book"
                    (click)="addBook()"
                    class="p-button-outlined"
                    data-testid="add-book-button"
                    [disabled]="!isBookFormValid()"
                    [attr.data-can-add-book]="isBookFormValid()"
                  ></button>
                </div>
              </div>

              <!-- Books Table -->
              <div class="books-table-section" *ngIf="author.books.length > 0">
                <h4 class="subsection-title">
                  Books List ({{ author.books.length }})
                </h4>
                <p-table
                  [value]="author.books"
                  [responsiveLayout]="'scroll'"
                  styleClass="books-table"
                  data-testid="books-table"
                  [attr.data-books-count]="author.books.length"
                >
                  <ng-template pTemplate="header">
                    <tr>
                      <th>Title</th>
                      <th>ISBN</th>
                      <th>Publication Date</th>
                      <th class="actions-column">Actions</th>
                    </tr>
                  </ng-template>
                  <ng-template pTemplate="body" let-book let-i="rowIndex">
                    <tr [attr.data-testid]="'book-row-'+ i ">
                      <td>
                        <span class="book-title" [attr.data-testid]="'book-title-'+ i ">
                          {{ book.title }}
                        </span>
                      </td>
                      <td>
                        <span class="book-isbn" [attr.data-testid]="'book-isbn-'+ i ">
                          {{ book.isbn }}
                        </span>
                      </td>
                      <td>
                        <span class="book-date" [attr.data-testid]="'book-date-'+ i ">
                          {{ formatDate(book.publicationDate) }}
                        </span>
                      </td>
                      <td class="actions-column">
                        <button
                          pButton
                          icon="pi pi-trash"
                          class="p-button-danger p-button-rounded p-button-sm p-button-outlined"
                          type="button"
                          (click)="confirmRemoveBook(i)"
                          pTooltip="Remove book"
                          tooltipPosition="top"
                          [attr.data-testid]="'remove-book-button-'+ i "
                        ></button>
                      </td>
                    </tr>
                  </ng-template>
                  <ng-template pTemplate="emptymessage">
                    <tr>
                      <td colspan="4" class="empty-message">
                        No books added yet. Add some books using the form above.
                      </td>
                    </tr>
                  </ng-template>
                </p-table>
              </div>

              <div class="no-books-message" *ngIf="author.books.length === 0" data-testid="no-books-message">
                <i class="pi pi-info-circle"></i>
                <p>No books added yet. Use the form above to add books by this author.</p>
              </div>
            </div>

            <p-divider></p-divider>

            <!-- Form Actions -->
            <div class="form-actions">
              <button
                pButton
                type="button"
                label="Cancel"
                icon="pi pi-times"
                class="p-button-secondary p-button-outlined"
                (click)="onCancel()"
                data-testid="cancel-button"
              ></button>

              <button
                pButton
                type="submit"
                label="Save Author"
                icon="pi pi-check"
                class="p-button-primary"
                [disabled]="!authorForm.valid || isSubmitting"
                [loading]="isSubmitting"
                data-testid="save-author-button"
                [attr.data-form-valid]="authorForm.valid"
                [attr.data-submitting]="isSubmitting"
              ></button>
            </div>
          </form>
        </p-card>
      </div>
    </div>
  `,
  styles: [`
    .author-form-container {
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 2rem 1rem;
    }

    .form-wrapper {
      max-width: 1000px;
      margin: 0 auto;
    }

    .author-form-card {
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
      border-radius: 16px;
      overflow: hidden;
    }

    .author-form-card :deep(.p-card-header) {
      background: linear-gradient(45deg, #4facfe 0%, #00f2fe 100%);
      color: white;
      text-align: center;
      padding: 2rem;
      font-size: 1.5rem;
      font-weight: 600;
    }

    .author-form-card :deep(.p-card-subtitle) {
      color: rgba(255, 255, 255, 0.9);
      font-size: 0.95rem;
      margin-top: 0.5rem;
    }

    .author-form {
      padding: 2rem;
    }

    .form-section {
      margin-bottom: 2rem;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #2c3e50;
      font-size: 1.25rem;
      font-weight: 600;
      margin-bottom: 1.5rem;
      padding-bottom: 0.5rem;
      border-bottom: 2px solid #e9ecef;
    }

    .section-title i {
      color: #4facfe;
    }

    .subsection-title {
      color: #495057;
      font-size: 1rem;
      font-weight: 500;
      margin-bottom: 1rem;
    }

    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;
      margin-bottom: 1rem;
    }

    .book-form-grid {
      display: grid;
      grid-template-columns: 2fr 1.5fr 1.5fr;
      gap: 1rem;
      margin-bottom: 1rem;
    }

    .form-field {
      display: flex;
      flex-direction: column;
    }

    .form-field.full-width {
      grid-column: 1 / -1;
    }

    .form-field label {
      font-weight: 500;
      color: #495057;
      margin-bottom: 0.5rem;
      font-size: 0.9rem;
    }

    .required-field::after {
      content: " *";
      color: #e74c3c;
    }

    .form-input {
      transition: all 0.3s ease;
    }

    .form-input:focus {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(79, 172, 254, 0.2);
    }

    .error-message {
      color: #e74c3c;
      font-size: 0.8rem;
      margin-top: 0.25rem;
    }

    .add-book-section {
      background: #f8f9fa;
      padding: 1.5rem;
      border-radius: 12px;
      margin-bottom: 1.5rem;
      border: 1px solid #e9ecef;
    }

    .add-book-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 1rem;
    }

    .books-table-section {
      margin-top: 1.5rem;
    }

    .books-table :deep(.p-datatable) {
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
    }

    .books-table :deep(.p-datatable-header) {
      background: #4facfe;
      color: white;
    }

    .books-table :deep(.p-datatable-tbody tr:hover) {
      background: #f1f8ff;
      transform: translateY(-1px);
      transition: all 0.2s ease;
    }

    .actions-column {
      width: 80px;
      text-align: center;
    }

    .book-title {
      font-weight: 500;
      color: #2c3e50;
    }

    .book-isbn {
      font-family: 'Courier New', monospace;
      font-size: 0.9rem;
      color: #6c757d;
    }

    .book-date {
      color: #495057;
    }

    .empty-message {
      text-align: center;
      color: #6c757d;
      font-style: italic;
      padding: 2rem;
    }

    .no-books-message {
      text-align: center;
      color: #6c757d;
      padding: 2rem;
      background: #f8f9fa;
      border-radius: 12px;
      border: 2px dashed #dee2e6;
    }

    .no-books-message i {
      font-size: 2rem;
      margin-bottom: 1rem;
      color: #adb5bd;
    }

    .form-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 2rem;
      padding-top: 1rem;
    }

    .form-actions button {
      min-width: 140px;
      height: 48px;
      font-weight: 500;
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .author-form-container {
        padding: 1rem 0.5rem;
      }

      .form-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .book-form-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .form-actions {
        flex-direction: column;
        gap: 1rem;
      }

      .form-actions button {
        width: 100%;
      }

      .author-form {
        padding: 1rem;
      }
    }

    /* Animation for form submission */
    .form-actions button[data-submitting="true"] {
      animation: pulse 1.5s infinite;
    }

    @keyframes pulse {
      0% { transform: scale(1); }
      50% { transform: scale(1.02); }
      100% { transform: scale(1); }
    }

    /* Focus indicators for accessibility */
    button:focus,
    input:focus,
    .p-datepicker:focus-within {
      outline: 2px solid #4facfe;
      outline-offset: 2px;
    }
  `]
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
