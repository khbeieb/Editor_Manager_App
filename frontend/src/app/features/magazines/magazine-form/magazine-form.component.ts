import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MagazineApiService } from '../../../services/magazine-api.service';
import { AuthorApiService } from '../../../services/author-api.service';
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
import {Author} from '../../../models/author.model';

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
  templateUrl: './magazine-form.component.html',
  styleUrls: ['./magazine-form.component.scss']
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
