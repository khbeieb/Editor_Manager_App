import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule, ButtonDirective } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { RouterLink, Router } from '@angular/router';
import { Observable, BehaviorSubject, combineLatest, Subject } from 'rxjs';
import { map, takeUntil, filter, catchError, finalize } from 'rxjs/operators';
import { MessageService } from 'primeng/api';
import { MagazineApiService } from '../../services/magazine-api.service';
import { Magazine } from '../../models/magazine.model';
import { ApiResponse } from '../../models/api-response.model';

interface FilterOptions {
  searchTerm: string;
  sortBy: string;
  sortOrder: 'asc' | 'desc';
}

@Component({
  standalone: true,
  selector: 'app-magazine-list',
  imports: [
    CommonModule,
    DatePipe,
    NgIf,
    FormsModule,
    TableModule,
    ButtonModule,
    ButtonDirective,
    CardModule,
    TagModule,
    ToastModule,
    ProgressSpinnerModule,
    InputTextModule,
    SelectModule,
    RouterLink
  ],
  providers: [MessageService],
  template: `
    <div class="book-list-container" data-testid="magazine-list-container">
      <p-toast></p-toast>
      <div class="list-wrapper">
        <div class="header-section">
          <p-card styleClass="header-card">
            <div class="header-content">
              <div class="title-section">
                <h1 class="page-title" data-testid="magazines-title">
                  <i class="pi pi-book"></i>
                  Magazines Library
                </h1>
                <p class="page-subtitle" data-testid="magazines-subtitle">
                  Manage and explore our collection of magazines
                </p>
              </div>
              <div class="header-actions">
                <button
                  pButton
                  type="button"
                  label="Add New Magazine"
                  icon="pi pi-plus"
                  class="p-button-primary add-book-btn"
                  routerLink="/magazines/new"
                  data-testid="add-magazine-btn"
                ></button>
                <button
                  pButton
                  type="button"
                  icon="pi pi-refresh"
                  class="p-button-outlined refresh-btn"
                  (click)="refreshMagazines()"
                  [loading]="isRefreshing"
                  data-testid="refresh-magazines-btn"
                ></button>
              </div>
            </div>
          </p-card>
        </div>

        <div class="table-section">
          <p-card styleClass="table-card">
            <div *ngIf="isLoading" class="loading-container" data-testid="magazines-loading">
              <p-progressSpinner
                [style]="{ width: '50px', height: '50px' }"
                strokeWidth="4"
              ></p-progressSpinner>
              <p class="loading-text">Loading magazines...</p>
            </div>

            <div
              *ngIf="hasError && !isLoading"
              class="error-container"
              data-testid="magazines-error"
            >
              <i class="pi pi-exclamation-triangle error-icon"></i>
              <h3>Failed to Load Magazines</h3>
              <p>We couldn't retrieve the magazines list. Please try again.</p>
              <button
                pButton
                type="button"
                label="Retry"
                icon="pi pi-refresh"
                class="p-button-outlined"
                (click)="refreshMagazines()"
                data-testid="retry-magazines-btn"
              ></button>
            </div>

            <div *ngIf="!isLoading && !hasError" class="table-container">
              <h3 class="table-title" data-testid="magazines-table-title">
                <i class="pi pi-list"></i>
                Magazines Directory ({{ (filteredMagazines$ | async)?.length || 0 }})
              </h3>
              <p-table
                [value]="(filteredMagazines$ | async) ?? []"
                [responsiveLayout]="'scroll'"
                data-testid="magazines-table"
              >
                <ng-template pTemplate="header">
                  <tr>
                    <th>Title</th>
                    <th>Issue Number</th>
                    <th>Published Date</th>
                    <th>Authors</th>
                  </tr>
                </ng-template>
                <ng-template pTemplate="body" let-magazine>
                  <tr data-testid="magazine-row">
                    <td data-testid="magazine-title">{{ magazine.title }}</td>
                    <td data-testid="magazine-issue">{{ magazine.issueNumber }}</td>
                    <td data-testid="magazine-date">
                      {{ magazine.publishedDate | date: 'mediumDate' }}
                    </td>
                    <td data-testid="magazine-authors">
                      <div class="authors-tags">
                        <p-tag
                          *ngFor="let author of magazine.authors"
                          [value]="author.name"
                          severity="info"
                          class="author-tag"
                          data-testid="magazine-author-tag"
                        ></p-tag>
                      </div>
                    </td>
                  </tr>
                </ng-template>
              </p-table>
            </div>
          </p-card>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
    /* Reuse BookListComponent styles for consistency */
    .book-list-container {
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 2rem 1rem;
    }
    .list-wrapper {
      max-width: 1400px;
      margin: 0 auto;
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .header-card { box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15); border-radius: 16px; overflow: hidden; }
    .header-content { display: flex; justify-content: space-between; align-items: center; padding: 1rem; }
    .title-section { flex: 1; }
    .page-title { display: flex; align-items: center; gap: 0.75rem; font-size: 2rem; font-weight: 700; color: #2c3e50; }
    .page-subtitle { color: #6c757d; font-size: 1rem; margin: 0; }
    .header-actions { display: flex; gap: 1rem; align-items: center; }
    .add-book-btn { height: 48px; padding: 0 2rem; font-weight: 600; background: linear-gradient(45deg, #4facfe 0%, #00f2fe 100%); border: none; transition: all 0.3s ease; }
    .add-book-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(79, 172, 254, 0.3); }
    .refresh-btn { height: 48px; width: 48px; }
    .table-card { box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15); border-radius: 16px; overflow: hidden; }
    .loading-container, .error-container { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 4rem 2rem; text-align: center; }
    .loading-text { margin-top: 1rem; color: #6c757d; font-size: 1.1rem; }
    .error-icon { font-size: 3rem; color: #e74c3c; margin-bottom: 1rem; }
    .table-title { font-size: 1.3rem; font-weight: 600; color: #2c3e50; margin: 1rem 0; }
    .table-container { padding: 0 1.5rem 1.5rem; }
    .authors-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
    }

    .author-tag {
      font-size: 0.75rem;
      font-weight: 500;
      padding: 0.25rem 0.5rem;
    }
    `
  ]
})
export class MagazineListComponent implements OnInit, OnDestroy {
  magazines$!: Observable<ApiResponse<Magazine[] | null>>;
  filteredMagazines$!: Observable<Magazine[]>;
  private destroy$ = new Subject<void>();

  isLoading = true;
  isRefreshing = false;
  hasError = false;

  constructor(
    private magazineApiService: MagazineApiService,
    private messageService: MessageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMagazines();
    this.setupFilteredMagazines();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadMagazines(): void {
    this.isLoading = true;
    this.hasError = false;

    this.magazines$ = this.magazineApiService.getMagazines().pipe(
      catchError((error) => {
        this.hasError = true;
        this.messageService.add({
          severity: 'error',
          summary: 'Loading Failed',
          detail: 'Could not load magazines. Please try again.',
          life: 5000
        });
        throw error;
      }),
      finalize(() => {
        this.isLoading = false;
        this.isRefreshing = false;
      }),
      takeUntil(this.destroy$)
    );

    this.magazines$.subscribe();
  }

  private setupFilteredMagazines(): void {
    this.filteredMagazines$ = this.magazines$.pipe(
      filter(res => !!res && !!res.data),
      map(res => res.data!),
      takeUntil(this.destroy$)
    );
  }

  refreshMagazines(): void {
    this.isRefreshing = true;
    this.loadMagazines();
    this.messageService.add({
      severity: 'info',
      summary: 'Refreshing',
      detail: 'Updating magazines list...',
      life: 2000
    });
  }
}
