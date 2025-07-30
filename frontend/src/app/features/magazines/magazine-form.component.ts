import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MagazineApiService } from '../../services/magazine-api.service';
import { AuthorApiService } from '../../services/author-api.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { SelectModule } from 'primeng/select';
import { Tooltip } from 'primeng/tooltip';
import { Observable, Subject, of } from 'rxjs';
import { catchError, finalize, map, takeUntil } from 'rxjs/operators';
import {MultiSelect} from 'primeng/multiselect';
import {Author} from '../../models/author.model';

@Component({
  standalone: true,
  selector: 'app-magazine-form',
  imports: [
    CommonModule,
    FormsModule,
    InputTextModule,
    ButtonModule,
    DatePickerModule,
    CardModule,
    DividerModule,
    ToastModule,
    ConfirmDialogModule,
    SelectModule,
    Tooltip,
    MultiSelect,
  ],
  providers: [MessageService, ConfirmationService],
  template: `
  <div class="magazine-form-container" data-testid="magazine-form-container">
    <p-toast></p-toast>
    <p-confirmDialog></p-confirmDialog>

    <div class="form-wrapper">
      <p-card
        header="Add New Magazine"
        subheader="Fill in the magazine details"
        styleClass="magazine-form-card"
        data-testid="magazine-form-card"
      >
        <form
          (ngSubmit)="onSubmit()"
          #magazineForm="ngForm"
          class="magazine-form"
          [attr.data-form-valid]="magazineForm.valid"
          data-testid="magazine-form"
        >
          <div class="form-section">
            <h3 class="section-title" data-testid="section-title">
              <i class="pi pi-book"></i>
              Magazine Information
            </h3>

            <div class="form-grid">
              <div class="form-field">
                <label for="title" class="required-field">Title</label>
                <input
                  pInputText
                  id="title"
                  name="title"
                  [(ngModel)]="magazine.title"
                  required
                  minlength="2"
                  maxlength="200"
                  placeholder="Enter magazine title"
                  class="form-input"
                  #titleField="ngModel"
                  [class.ng-invalid]="titleField.invalid && titleField.touched"
                  data-testid="input-title"
                />
                <small
                  class="error-message"
                  *ngIf="titleField.invalid && titleField.touched"
                  data-testid="error-title"
                >
                  Title is required (2-200 characters)
                </small>
              </div>

              <div class="form-field">
                <label for="issueNumber" class="required-field">Issue Number</label>
                <input
                  pInputText
                  id="issueNumber"
                  name="issueNumber"
                  type="number"
                  [(ngModel)]="magazine.issueNumber"
                  required
                  min="1"
                  class="form-input"
                  #issueField="ngModel"
                  [class.ng-invalid]="issueField.invalid && issueField.touched"
                  data-testid="input-issue-number"
                />
                <small
                  class="error-message"
                  *ngIf="issueField.invalid && issueField.touched"
                  data-testid="error-issue-number"
                >
                  Issue number is required and must be greater than 0
                </small>
              </div>

              <div class="form-field full-width">
                <label for="publishedDate" class="required-field">Published Date</label>
                <p-datepicker
                  id="publishedDate"
                  name="publishedDate"
                  [(ngModel)]="magazine.publishedDate"
                  required
                  dateFormat="dd/mm/yy"
                  showIcon
                  [maxDate]="today"
                  inputId="publishedDateInput"
                  class="form-input"
                  #dateField="ngModel"
                  [class.ng-invalid]="dateField.invalid && dateField.touched"
                  data-testid="input-published-date"
                ></p-datepicker>
                <small
                  class="error-message"
                  *ngIf="dateField.invalid && dateField.touched"
                  data-testid="error-published-date"
                >
                  Published date is required
                </small>
              </div>

              <div class="form-field full-width">
                <label for="authors" class="required-field">Authors</label>
                <p-multiSelect
                  id="authors"
                  name="authors"
                  [options]="(authors$ | async) ?? []"
                  optionLabel="name"
                  optionValue="id"
                  [(ngModel)]="selectedAuthorIds"
                  required
                  placeholder="Select authors"
                  class="form-input"
                  #authorsField="ngModel"
                  [class.ng-invalid]="authorsField.invalid && authorsField.touched"
                  data-testid="input-authors"
                ></p-multiSelect>
                <small
                  class="error-message"
                  *ngIf="authorsField.invalid && authorsField.touched"
                  data-testid="error-authors"
                >
                  At least one author must be selected
                </small>
              </div>
            </div>
          </div>

          <p-divider></p-divider>

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
              label="Save Magazine"
              icon="pi pi-check"
              class="p-button-primary"
              [disabled]="!magazineForm.valid || isSubmitting"
              [loading]="isSubmitting"
              [attr.data-submitting]="isSubmitting"
              data-testid="submit-button"
            ></button>
          </div>
        </form>
      </p-card>
    </div>
  </div>
`,
  styles: [`
    .magazine-form-container {
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 2rem 1rem;
    }
    .form-wrapper {
      max-width: 700px;
      margin: 0 auto;
    }
    .magazine-form-card {
      box-shadow: 0 20px 60px rgba(0,0,0,0.15);
      border-radius: 16px;
      overflow: hidden;
    }
    .magazine-form {
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
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;
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
    @media (max-width: 768px) {
      .magazine-form-container {
        padding: 1rem 0.5rem;
      }
      .form-grid {
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
      .magazine-form {
        padding: 1rem;
      }
    }
    .form-actions button[data-submitting="true"] {
      animation: pulse 1.5s infinite;
    }
    @keyframes pulse {
      0% { transform: scale(1); }
      50% { transform: scale(1.02); }
      100% { transform: scale(1); }
    }
    button:focus,
    input:focus,
    .p-datepicker:focus-within,
    .p-multiselect:focus-within {
      outline: 2px solid #4facfe;
      outline-offset: 2px;
    }
  `]
})
export class MagazineFormComponent implements OnDestroy {
  magazine = {
    title: '',
    issueNumber: 1,
    publishedDate: new Date(),
  };

  selectedAuthorIds: number[] = [];

  today = new Date();
  isSubmitting = false;

  authors$!: Observable<Author[]>;
  hasError = false;
  isLoading = true;

  private destroy$ = new Subject<void>();

  constructor(
    private magazineApiService: MagazineApiService,
    private authorApiService: AuthorApiService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private router: Router
  ) {
    this.loadAuthors();
  }

  loadAuthors(): void {
    this.isLoading = true;
    this.hasError = false;

    this.authors$ = this.authorApiService.getAuthors().pipe(
      map(response => response.data ?? []),
      catchError((error) => {
        console.error('Failed to load authors:', error);
        this.hasError = true;
        this.messageService.add({
          severity: 'error',
          summary: 'Loading Failed',
          detail: 'Could not load authors. Please try again.',
          life: 5000
        });
        return of([]);
      }),
      finalize(() => {
        this.isLoading = false;
      }),
      takeUntil(this.destroy$)
    );
  }

  onCancel(): void {
    this.confirmationService.confirm({
      message: 'Are you sure you want to cancel? All unsaved changes will be lost.',
      header: 'Confirm Cancel',
      icon: 'pi pi-question-circle',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-secondary p-button-outlined',
      accept: () => this.router.navigate(['/magazines']),
    });
  }

  async onSubmit(): Promise<void> {
    if (this.isSubmitting) return;

    this.isSubmitting = true;

    try {
      const payload = {
        title: this.magazine.title,
        issueNumber: this.magazine.issueNumber,
        publishedDate: this.magazine.publishedDate.toISOString().split('T')[0],
        authors: this.selectedAuthorIds.map(id => ({ id })),
      };

      const response = await this.magazineApiService.addMagazine(payload).toPromise();

      if (response?.statusCode === 201 && response.data) {
        this.messageService.add({
          severity: 'success',
          summary: 'Magazine Created',
          detail: `"${this.magazine.title}" has been successfully added.`,
          life: 4000
        });

        setTimeout(() => {
          this.router.navigate(['/magazines']);
        }, 1500);
      } else {
        throw new Error(response?.message || 'Failed to create magazine');
      }
    } catch (error: any) {
      console.error('Failed to add magazine:', error);

      this.messageService.add({
        severity: 'error',
        summary: 'Creation Failed',
        detail: error.message || 'Failed to create magazine. Please try again.',
        life: 5000
      });
    } finally {
      this.isSubmitting = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
