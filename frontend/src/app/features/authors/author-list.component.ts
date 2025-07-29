import { Component, OnInit, OnDestroy } from '@angular/core';
import { AsyncPipe, DatePipe, NgForOf, NgIf, CommonModule } from '@angular/common';
import { AuthorApiService } from '../../services/author-api.service';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { RouterLink, Router } from '@angular/router';
import { Observable, BehaviorSubject, combineLatest, Subject } from 'rxjs';
import { map, startWith, takeUntil, catchError, finalize } from 'rxjs/operators';
import { MessageService } from 'primeng/api';
import { Author } from '../../models/author.model';
import { ApiResponse } from '../../models/api-response.model';
import { FormsModule } from '@angular/forms';

interface FilterOptions {
  searchTerm: string;
  nationalityFilter: string;
  sortBy: string;
  sortOrder: 'asc' | 'desc';
}

@Component({
  standalone: true,
  selector: 'app-author-list',
  imports: [
    CommonModule,
    AsyncPipe,
    DatePipe,
    NgForOf,
    NgIf,
    FormsModule,
    TableModule,
    ButtonModule,
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
    <div class="author-list-container">
      <p-toast data-testid="toast-messages"></p-toast>

      <div class="list-wrapper">
        <!-- Header Section -->
        <div class="header-section">
          <p-card
            styleClass="header-card"
            data-testid="header-card"
          >
            <div class="header-content">
              <div class="title-section">
                <h1 class="page-title">
                  <i class="pi pi-users"></i>
                  Authors Library
                </h1>
                <p class="page-subtitle">
                  Manage and explore our collection of authors and their published works
                </p>
              </div>

              <div class="header-actions">
                <button
                  pButton
                  type="button"
                  label="Add New Author"
                  icon="pi pi-plus"
                  class="p-button-primary add-author-btn"
                  routerLink="/authors/new"
                  data-testid="add-author-button"
                ></button>

                <button
                  pButton
                  type="button"
                  icon="pi pi-refresh"
                  class="p-button-outlined refresh-btn"
                  (click)="refreshAuthors()"
                  [loading]="isRefreshing"
                  pTooltip="Refresh authors list"
                  tooltipPosition="bottom"
                  data-testid="refresh-authors-button"
                  [attr.data-refreshing]="isRefreshing"
                ></button>
              </div>
            </div>
          </p-card>
        </div>

        <!-- Filters Section -->
        <div class="filters-section">
          <p-card
            header="Search & Filter"
            styleClass="filters-card"
            data-testid="filters-card"
          >
            <div class="filters-grid">
              <div class="filter-field">
                <label for="search-input">Search Authors</label>
                <input
                  pInputText
                  id="search-input"
                  [(ngModel)]="filters.searchTerm"
                  (ngModelChange)="onFilterChange()"
                  placeholder="Search by name or nationality..."
                  class="search-input"
                  data-testid="search-input"
                  [attr.data-search-term]="filters.searchTerm"
                />
                <i class="pi pi-search search-icon"></i>
              </div>

              <div class="filter-field">
                <label for="nationality-filter">Filter by Nationality</label>
                <p-select
                  id="nationality-filter"
                  [(ngModel)]="filters.nationalityFilter"
                  (ngModelChange)="onFilterChange()"
                  [options]="nationalityOptions"
                  optionLabel="label"
                  optionValue="value"
                  placeholder="All Nationalities"
                  styleClass="nationality-dropdown"
                  data-testid="nationality-filter"
                  [attr.data-nationality-filter]="filters.nationalityFilter"
                ></p-select>
              </div>

              <div class="filter-field">
                <label for="sort-by">Sort By</label>
                <p-select
                  id="sort-by"
                  [(ngModel)]="filters.sortBy"
                  (ngModelChange)="onFilterChange()"
                  [options]="sortOptions"
                  optionLabel="label"
                  optionValue="value"
                  styleClass="sort-dropdown"
                  data-testid="sort-by-filter"
                  [attr.data-sort-by]="filters.sortBy"
                ></p-select>
              </div>

              <div class="filter-field">
                <label for="sort-order">Sort Order</label>
                <p-select
                  id="sort-order"
                  [(ngModel)]="filters.sortOrder"
                  (ngModelChange)="onFilterChange()"
                  [options]="sortOrderOptions"
                  optionLabel="label"
                  optionValue="value"
                  styleClass="sort-order-dropdown"
                  data-testid="sort-order-filter"
                  [attr.data-sort-order]="filters.sortOrder"
                ></p-select>
              </div>
            </div>

            <div class="filter-summary" *ngIf="hasActiveFilters()" data-testid="filter-summary">
              <span class="summary-text">
                <i class="pi pi-filter"></i>
                Showing {{ (filteredAuthors$ | async)?.length || 0 }} of {{ (authors$ | async)?.data?.length || 0 }} authors
              </span>
              <button
                pButton
                type="button"
                label="Clear Filters"
                icon="pi pi-times"
                class="p-button-sm p-button-outlined clear-filters-btn"
                (click)="clearFilters()"
                data-testid="clear-filters-button"
              ></button>
            </div>
          </p-card>
        </div>

        <!-- Authors Table Section -->
        <div class="table-section">
          <p-card
            styleClass="table-card"
            data-testid="authors-table-card"
          >
            <!-- Loading State -->
            <div
              class="loading-container"
              *ngIf="isLoading"
              data-testid="loading-spinner"
            >
              <p-progressSpinner
                [style]="{width: '50px', height: '50px'}"
                strokeWidth="4"
                animationDuration="1s"
              ></p-progressSpinner>
              <p class="loading-text">Loading authors...</p>
            </div>

            <!-- Error State -->
            <div
              class="error-container"
              *ngIf="hasError && !isLoading"
              data-testid="error-message"
            >
              <i class="pi pi-exclamation-triangle error-icon"></i>
              <h3>Failed to Load Authors</h3>
              <p>We couldn't retrieve the authors list. Please try again.</p>
              <button
                pButton
                type="button"
                label="Retry"
                icon="pi pi-refresh"
                class="p-button-outlined"
                (click)="refreshAuthors()"
                data-testid="retry-button"
              ></button>
            </div>

            <!-- Authors Table -->
            <div *ngIf="!isLoading && !hasError" class="table-container">
              <div class="table-header">
                <h3 class="table-title">
                  <i class="pi pi-list"></i>
                  Authors Directory
                  <span class="authors-count" data-testid="authors-count">
                    ({{ (filteredAuthors$ | async)?.length || 0 }})
                  </span>
                </h3>
              </div>

              <p-table
                [value]="(filteredAuthors$ | async) ?? []"
                [responsiveLayout]="'scroll'"
                styleClass="authors-table"
                data-testid="authors-table"
                [attr.data-authors-count]="(filteredAuthors$ | async)?.length"
              >
                <ng-template pTemplate="header">
                  <tr>
                    <th class="author-name-col">Author</th>
                    <th class="nationality-col">Nationality</th>
                    <th class="birth-date-col">Birth Date</th>
                    <th class="books-col">Published Books</th>
                    <th class="actions-col">Actions</th>
                  </tr>
                </ng-template>

                <ng-template pTemplate="body" let-author let-i="rowIndex">
                  <tr
                    class="author-row"
                    [attr.data-testid]="'author-row-'+ i "
                    [attr.data-author-id]="author.id"
                    [attr.data-author-name]="author.name"
                  >
                    <td class="author-name-cell">
                      <div class="author-info">
                        <div class="author-avatar">
                          <i class="pi pi-user"></i>
                        </div>
                        <div class="author-details">
                          <span
                            class="author-name"
                            [attr.data-testid]="'author-name-'+ i "
                          >
                            {{ author.name }}
                          </span>
                          <small class="author-id">ID: {{ author.id }}</small>
                        </div>
                      </div>
                    </td>

                    <td class="nationality-cell">
                      <p-tag
                        [value]="author.nationality"
                        [style]="{background: getNationalityColor(author.nationality)}"
                        [attr.data-testid]="'author-nationality-'+ i "
                      ></p-tag>
                    </td>

                    <td class="birth-date-cell">
                      <span
                        class="birth-date"
                        [attr.data-testid]="'author-birth-date-'+ i "
                      >
                        {{ author.birthDate | date: 'mediumDate' }}
                      </span>
                      <small class="age-info">
                        ({{ calculateAge(author.birthDate) }} years old)
                      </small>
                    </td>

                    <td class="books-cell">
                      <div class="books-info">
                        <div class="books-count">
                          <i class="pi pi-book"></i>
                          <span
                            class="count-badge"
                            [attr.data-testid]="'author-books-count-'+ i "
                          >
                            {{ author.books?.length || 0 }}
                          </span>
                        </div>

                        <div
                          class="books-list"
                          *ngIf="author.books?.length; else noBooksTemplate"
                          [attr.data-testid]="'author-books-list-'+ i "
                        >
                          <div
                            class="book-item"
                            *ngFor="let book of author.books; let bookIndex = index"
                            [attr.data-testid]="'book-item--'+ i +'-'+ bookIndex "
                          >
                            <div class="book-title">{{ book.title }}</div>
                            <div class="book-isbn">ISBN: {{ book.isbn }}</div>
                            <div class="book-date">
                              <i class="pi pi-calendar"></i>
                              {{ book.publicationDate | date: 'yyyy' }}
                            </div>
                          </div>
                        </div>

                        <ng-template #noBooksTemplate>
                          <div
                            class="no-books"
                            [attr.data-testid]="'no-books-'+ i "
                          >
                            <i class="pi pi-info-circle"></i>
                            <span>No published books</span>
                          </div>
                        </ng-template>
                      </div>
                    </td>

                    <td class="actions-cell">
                      <div class="action-buttons">
                        <button
                          pButton
                          type="button"
                          icon="pi pi-eye"
                          class="p-button-rounded p-button-outlined p-button-sm view-btn"
                          (click)="viewAuthor(author)"
                          pTooltip="View details"
                          tooltipPosition="top"
                          [attr.data-testid]="'view-author-button-'+ i "
                          [attr.data-author-id]="author.id"
                        ></button>

                        <button
                          pButton
                          type="button"
                          icon="pi pi-pencil"
                          class="p-button-rounded p-button-outlined p-button-sm edit-btn"
                          (click)="editAuthor(author)"
                          pTooltip="Edit author"
                          tooltipPosition="top"
                          [attr.data-testid]="'edit-author-button-'+ i "
                          [attr.data-author-id]="author.id"
                        ></button>

                        <button
                          pButton
                          type="button"
                          icon="pi pi-trash"
                          class="p-button-rounded p-button-outlined p-button-sm p-button-danger delete-btn"
                          (click)="deleteAuthor(author)"
                          pTooltip="Delete author"
                          tooltipPosition="top"
                          [attr.data-testid]="'delete-author-button-'+ i "
                          [attr.data-author-id]="author.id"
                        ></button>
                      </div>
                    </td>
                  </tr>
                </ng-template>

                <ng-template pTemplate="emptymessage">
                  <tr>
                    <td colspan="5" class="empty-message" data-testid="empty-message">
                      <div class="empty-content">
                        <i class="pi pi-users empty-icon"></i>
                        <h3>No Authors Found</h3>
                        <p *ngIf="hasActiveFilters(); else noAuthorsTemplate">
                          No authors match your current filters. Try adjusting your search criteria.
                        </p>
                        <ng-template #noAuthorsTemplate>
                          <p>Start building your authors library by adding your first author.</p>
                          <button
                            pButton
                            type="button"
                            label="Add First Author"
                            icon="pi pi-plus"
                            class="p-button-primary"
                            routerLink="/authors/new"
                            data-testid="add-first-author-button"
                          ></button>
                        </ng-template>
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
  styles: [`
    .author-list-container {
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

    /* Header Section */
    .header-card {
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
      border-radius: 16px;
      overflow: hidden;
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem;
    }

    .title-section {
      flex: 1;
    }

    .page-title {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      font-size: 2rem;
      font-weight: 700;
      color: #2c3e50;
      margin: 0 0 0.5rem 0;
    }

    .page-title i {
      color: #4facfe;
      font-size: 1.8rem;
    }

    .page-subtitle {
      color: #6c757d;
      font-size: 1rem;
      margin: 0;
    }

    .header-actions {
      display: flex;
      gap: 1rem;
      align-items: center;
    }

    .add-author-btn {
      height: 48px;
      padding: 0 2rem;
      font-weight: 600;
      background: linear-gradient(45deg, #4facfe 0%, #00f2fe 100%);
      border: none;
      transition: all 0.3s ease;
    }

    .add-author-btn:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(79, 172, 254, 0.3);
    }

    .refresh-btn {
      height: 48px;
      width: 48px;
    }

    /* Filters Section */
    .filters-card :deep(.p-card-header) {
      background: linear-gradient(90deg, #f8f9fa 0%, #e9ecef 100%);
      color: #495057;
      font-weight: 600;
    }

    .filters-grid {
      display: grid;
      grid-template-columns: 2fr 1fr 1fr 1fr;
      gap: 1.5rem;
      margin-bottom: 1rem;
    }

    .filter-field {
      display: flex;
      flex-direction: column;
      position: relative;
    }

    .filter-field label {
      font-weight: 500;
      color: #495057;
      margin-bottom: 0.5rem;
      font-size: 0.9rem;
    }

    .search-input {
      padding-right: 2.5rem;
    }

    .search-icon {
      position: absolute;
      right: 1rem;
      top: 2.4rem;
      color: #6c757d;
      pointer-events: none;
    }

    .filter-summary {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem;
      background: #f1f8ff;
      border-radius: 8px;
      border-left: 4px solid #4facfe;
    }

    .summary-text {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #495057;
      font-weight: 500;
    }

    .clear-filters-btn {
      height: 32px;
    }

    /* Table Section */
    .table-card {
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
      border-radius: 16px;
      overflow: hidden;
    }

    .loading-container,
    .error-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem 2rem;
      text-align: center;
    }

    .loading-text {
      margin-top: 1rem;
      color: #6c757d;
      font-size: 1.1rem;
    }

    .error-icon {
      font-size: 3rem;
      color: #e74c3c;
      margin-bottom: 1rem;
    }

    .error-container h3 {
      color: #2c3e50;
      margin-bottom: 0.5rem;
    }

    .error-container p {
      color: #6c757d;
      margin-bottom: 2rem;
    }

    .table-header {
      padding: 1.5rem 1.5rem 0;
      border-bottom: 1px solid #e9ecef;
      margin-bottom: 1rem;
    }

    .table-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1.3rem;
      font-weight: 600;
      color: #2c3e50;
      margin: 0;
    }

    .authors-count {
      color: #4facfe;
      font-weight: 700;
    }

    .table-container {
      padding: 0 1.5rem 1.5rem;
    }

    /* Table Styling */
    .authors-table :deep(.p-datatable) {
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
    }

    .authors-table :deep(.p-datatable-thead tr th) {
      background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
      color: white;
      font-weight: 600;
      padding: 1rem;
      border: none;
    }

    .authors-table :deep(.p-datatable-tbody tr) {
      transition: all 0.2s ease;
    }

    .authors-table :deep(.p-datatable-tbody tr:hover) {
      background: #f1f8ff;
      transform: translateY(-1px);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    }

    .authors-table :deep(.p-datatable-tbody tr td) {
      padding: 1.5rem 1rem;
      border-bottom: 1px solid #f0f0f0;
      vertical-align: top;
    }

    /* Column Widths */
    .author-name-col { width: 20%; }
    .nationality-col { width: 15%; }
    .birth-date-col { width: 15%; }
    .books-col { width: 35%; }
    .actions-col { width: 15%; text-align: center; }

    /* Cell Content */
    .author-info {
      display: flex;
      align-items: flex-start;
      gap: 1rem;
    }

    .author-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: linear-gradient(45deg, #4facfe 0%, #00f2fe 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 1.2rem;
      flex-shrink: 0;
    }

    .author-details {
      display: flex;
      flex-direction: column;
    }

    .author-name {
      font-weight: 600;
      color: #2c3e50;
      font-size: 1rem;
    }

    .author-id {
      color: #6c757d;
      font-size: 0.8rem;
      margin-top: 0.25rem;
    }

    .birth-date {
      font-weight: 500;
      color: #495057;
    }

    .age-info {
      display: block;
      color: #6c757d;
      font-size: 0.8rem;
      margin-top: 0.25rem;
    }

    .books-info {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .books-count {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-weight: 600;
      color: #4facfe;
    }

    .count-badge {
      background: #4facfe;
      color: white;
      padding: 0.25rem 0.5rem;
      border-radius: 12px;
      font-size: 0.8rem;
      font-weight: 600;
    }

    .books-list {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .book-item {
      background: #f8f9fa;
      padding: 0.75rem;
      border-radius: 8px;
      border-left: 3px solid #4facfe;
    }

    .book-title {
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 0.25rem;
    }

    .book-isbn {
      font-family: 'Courier New', monospace;
      font-size: 0.8rem;
      color: #6c757d;
      margin-bottom: 0.25rem;
    }

    .book-date {
      display: flex;
      align-items: center;
      gap: 0.25rem;
      font-size: 0.8rem;
      color: #495057;
    }

    .no-books {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #6c757d;
      font-style: italic;
    }

    .action-buttons {
      display: flex;
      gap: 0.5rem;
      justify-content: center;
    }

    .action-buttons button {
      width: 36px;
      height: 36px;
    }

    .view-btn { color: #4facfe; border-color: #4facfe; }
    .edit-btn { color: #28a745; border-color: #28a745; }
    .delete-btn { color: #dc3545; border-color: #dc3545; }

    .empty-message {
      text-align: center;
      padding: 4rem 2rem;
    }

    .empty-content {
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .empty-icon {
      font-size: 4rem;
      color: #adb5bd;
      margin-bottom: 1rem;
    }

    .empty-content h3 {
      color: #495057;
      margin-bottom: 1rem;
    }

    .empty-content p {
      color: #6c757d;
      margin-bottom: 2rem;
      max-width: 400px;
    }

    /* Responsive Design */
    @media (max-width: 1200px) {
      .filters-grid {
        grid-template-columns: 1fr 1fr;
        gap: 1rem;
      }

      .books-col { width: 40%; }
      .actions-col { width: 10%; }
    }

    @media (max-width: 768px) {
      .author-list-container {
        padding: 1rem 0.5rem;
      }

      .header-content {
        flex-direction: column;
        align-items: stretch;
        gap: 1rem;
      }

      .header-actions {
        justify-content: space-between;
      }

      .filters-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .filter-summary {
        flex-direction: column;
        align-items: stretch;
        gap: 1rem;
      }

      .page-title {
        font-size: 1.5rem;
      }

      .table-header {
        padding: 1rem;
      }

      .table-container {
        padding: 0 1rem 1rem;
      }

      .authors-table :deep(.p-datatable-tbody tr td) {
        padding: 1rem 0.5rem;
      }

      .author-info {
        flex-direction: column;
        align-items: center;
        text-align: center;
        gap: 0.5rem;
      }

      .books-list {
        max-height: 200px;
        overflow-y: auto;
      }

      .action-buttons {
        flex-direction: column;
        gap: 0.25rem;
      }
    }

    /* Accessibility */
    button:focus,
    input:focus,
    .p-dropdown:focus-within {
      outline: 2px solid #4facfe;
      outline-offset: 2px;
    }

    /* Animation for loading and refresh states */
    .refresh-btn[data-refreshing="true"] {
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
  `]
})
export class AuthorListComponent implements OnInit, OnDestroy {
  authors$!: Observable<ApiResponse<Author[] | null>>;
  filteredAuthors$!: Observable<Author[]> ;

  private destroy$ = new Subject<void>();
  private filtersSubject = new BehaviorSubject<FilterOptions>({
    searchTerm: '',
    nationalityFilter: '',
    sortBy: 'name',
    sortOrder: 'asc'
  });

  filters: FilterOptions = {
    searchTerm: '',
    nationalityFilter: '',
    sortBy: 'name',
    sortOrder: 'asc'
  };

  nationalityOptions: Array<{label: string, value: string}> = [];
  sortOptions = [
    { label: 'Name', value: 'name' },
    { label: 'Birth Date', value: 'birthDate' },
    { label: 'Nationality', value: 'nationality' },
    { label: 'Number of Books', value: 'booksCount' }
  ];

  sortOrderOptions = [
    { label: 'Ascending', value: 'asc' },
    { label: 'Descending', value: 'desc' }
  ];

  isLoading = true;
  isRefreshing = false;
  hasError = false;

  constructor(
    private authorApiService: AuthorApiService,
    private messageService: MessageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAuthors();
    this.setupFilteredAuthors();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadAuthors(): void {
    this.isLoading = true;
    this.hasError = false;

    this.authors$ = this.authorApiService.getAuthors().pipe(
      catchError((error) => {
        console.error('Failed to load authors:', error);
        this.hasError = true;
        this.messageService.add({
          severity: 'error',
          summary: 'Loading Failed',
          detail: 'Could not load authors. Please try again.',
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

    // Extract nationalities for filter dropdown
    this.authors$.subscribe({
      next: (response) => {
        if (response?.data) {
          this.extractNationalities(response.data);
        }
      },
      error: () => {
        this.hasError = true;
      }
    });
  }

  private setupFilteredAuthors(): void {
    this.filteredAuthors$ = combineLatest([
      this.authors$.pipe(
        map(response => response?.data || []),
        startWith([])
      ),
      this.filtersSubject.asObservable()
    ]).pipe(
      map(([authors, filters]) => this.applyFilters(authors, filters)),
      takeUntil(this.destroy$)
    );
  }

  private extractNationalities(authors: Author[]): void {
    const uniqueNationalities = [...new Set(authors.map(author => author.nationality))]
      .filter(nationality => nationality)
      .sort();

    this.nationalityOptions = [
      { label: 'All Nationalities', value: '' },
      ...uniqueNationalities.map(nationality => ({
        label: nationality,
        value: nationality
      }))
    ];
  }

  private applyFilters(authors: Author[], filters: FilterOptions): Author[] {
    let filtered = [...authors];

    // Apply search filter
    if (filters.searchTerm) {
      const searchLower = filters.searchTerm.toLowerCase();
      filtered = filtered.filter(author =>
        author.name.toLowerCase().includes(searchLower) ||
        author.nationality.toLowerCase().includes(searchLower)
      );
    }

    // Apply nationality filter
    if (filters.nationalityFilter) {
      filtered = filtered.filter(author =>
        author.nationality === filters.nationalityFilter
      );
    }

    // Apply sorting
    filtered.sort((a, b) => {
      let aValue: any;
      let bValue: any;

      switch (filters.sortBy) {
        case 'name':
          aValue = a.name.toLowerCase();
          bValue = b.name.toLowerCase();
          break;
        case 'birthDate':
          aValue = new Date(a.birthDate);
          bValue = new Date(b.birthDate);
          break;
        case 'nationality':
          aValue = a.nationality.toLowerCase();
          bValue = b.nationality.toLowerCase();
          break;
        case 'booksCount':
          aValue = a.books?.length || 0;
          bValue = b.books?.length || 0;
          break;
        default:
          return 0;
      }

      if (aValue < bValue) return filters.sortOrder === 'asc' ? -1 : 1;
      if (aValue > bValue) return filters.sortOrder === 'asc' ? 1 : -1;
      return 0;
    });

    return filtered;
  }

  onFilterChange(): void {
    this.filtersSubject.next({ ...this.filters });
  }

  hasActiveFilters(): boolean {
    return !!(
      this.filters.searchTerm ||
      this.filters.nationalityFilter ||
      this.filters.sortBy !== 'name' ||
      this.filters.sortOrder !== 'asc'
    );
  }

  clearFilters(): void {
    this.filters = {
      searchTerm: '',
      nationalityFilter: '',
      sortBy: 'name',
      sortOrder: 'asc'
    };
    this.onFilterChange();

    this.messageService.add({
      severity: 'info',
      summary: 'Filters Cleared',
      detail: 'All filters have been reset.',
      life: 2000
    });
  }

  refreshAuthors(): void {
    this.isRefreshing = true;
    this.loadAuthors();

    this.messageService.add({
      severity: 'info',
      summary: 'Refreshing',
      detail: 'Updating authors list...',
      life: 2000
    });
  }

  calculateAge(birthDate: string): number {
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }

    return age;
  }

  getNationalityColor(nationality: string): string {
    // Generate consistent colors based on nationality
    const colors = [
      '#4facfe', '#00f2fe', '#43e97b', '#38f9d7',
      '#ffecd2', '#fcb69f', '#a8edea', '#fed6e3',
      '#d299c2', '#fef9d7', '#667eea', '#764ba2'
    ];

    const hash = nationality.split('').reduce((acc, char) => {
      return char.charCodeAt(0) + ((acc << 5) - acc);
    }, 0);

    return colors[Math.abs(hash) % colors.length];
  }

  viewAuthor(author: Author): void {
    this.messageService.add({
      severity: 'info',
      summary: 'View Author',
      detail: `Viewing details for ${author.name}`,
      life: 2000
    });

    // Navigate to author detail page
    this.router.navigate(['/authors', author.id]);
  }

  editAuthor(author: Author): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Edit Author',
      detail: `Editing ${author.name}`,
      life: 2000
    });

    // Navigate to author edit page
    this.router.navigate(['/authors', author.id, 'edit']);
  }

  deleteAuthor(author: Author): void {
    this.messageService.add({
      severity: 'warn',
      summary: 'Delete Author',
      detail: `Delete functionality for ${author.name}`,
      life: 3000
    });
  }
  }
