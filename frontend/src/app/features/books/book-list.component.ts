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
import {Observable, BehaviorSubject, combineLatest, Subject, of, filter, tap} from 'rxjs';
import { map, startWith, takeUntil, catchError, finalize } from 'rxjs/operators';
import { MessageService } from 'primeng/api';
import { BookApiService } from '../../services/book-api.service';
import { Book } from '../../models/book.model';
import { ApiResponse } from '../../models/api-response.model';

interface FilterOptions {
  searchTerm: string;
  sortBy: string;
  sortOrder: 'asc' | 'desc';
}

@Component({
  standalone: true,
  selector: 'app-book-list',
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
    <div class="book-list-container">
      <p-toast data-testid="toast-messages"></p-toast>
      <div class="list-wrapper">
        <!-- Header Section -->
        <div class="header-section">
          <p-card styleClass="header-card" data-testid="header-card">
            <div class="header-content">
              <div class="title-section">
                <h1 class="page-title">
                  <i class="pi pi-book"></i>
                  Books Library
                </h1>
                <p class="page-subtitle">
                  Manage and explore our collection of books
                </p>
              </div>
              <div class="header-actions">
                <button
                  pButton
                  type="button"
                  label="Add New Book"
                  icon="pi pi-plus"
                  class="p-button-primary add-book-btn"
                  routerLink="/books/new"
                  data-testid="add-book-button"
                ></button>
                <button
                  pButton
                  type="button"
                  icon="pi pi-refresh"
                  class="p-button-outlined refresh-btn"
                  (click)="refreshBooks()"
                  [loading]="isRefreshing"
                  pTooltip="Refresh books list"
                  tooltipPosition="bottom"
                  data-testid="refresh-books-button"
                  [attr.data-refreshing]="isRefreshing"
                ></button>
              </div>
            </div>
          </p-card>
        </div>

        <!-- Filters Section -->
        <div class="filters-section">
          <p-card header="Search & Filter" styleClass="filters-card" data-testid="filters-card">
            <div class="filters-grid">
              <div class="filter-field">
                <label for="search-input">Search Books</label>
                <input
                  pInputText
                  id="search-input"
                  [(ngModel)]="filters.searchTerm"
                  (ngModelChange)="onFilterChange()"
                  placeholder="Search by title..."
                  class="search-input"
                  data-testid="search-input"
                  [attr.data-search-term]="filters.searchTerm"
                />
                <i class="pi pi-search search-icon"></i>
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
                Showing {{ (filteredBooks$ | async)?.length || 0 }} of {{ (books$ | async)?.data?.length || 0 }} books
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

        <!-- Books Table Section -->
        <div class="table-section">
          <p-card styleClass="table-card" data-testid="books-table-card">
            <!-- Loading State -->
            <div class="loading-container" *ngIf="isLoading" data-testid="loading-spinner">
              <p-progressSpinner
                [style]="{width: '50px', height: '50px'}"
                strokeWidth="4"
                animationDuration="1s"
              ></p-progressSpinner>
              <p class="loading-text">Loading books...</p>
            </div>
            <!-- Error State -->
            <div class="error-container" *ngIf="hasError && !isLoading" data-testid="error-message">
              <i class="pi pi-exclamation-triangle error-icon"></i>
              <h3>Failed to Load Books</h3>
              <p>We couldn't retrieve the books list. Please try again.</p>
              <button
                pButton
                type="button"
                label="Retry"
                icon="pi pi-refresh"
                class="p-button-outlined"
                (click)="refreshBooks()"
                data-testid="retry-button"
              ></button>
            </div>
            <!-- Books Table -->
            <div *ngIf="!isLoading && !hasError" class="table-container">
              <div class="table-header">
                <h3 class="table-title">
                  <i class="pi pi-list"></i>
                  Books Directory
                  <span class="books-count" data-testid="books-count">
                    ({{ (filteredBooks$ | async)?.length || 0 }})
                  </span>
                </h3>
              </div>
              <p-table
                [value]="(filteredBooks$ | async) ?? []"
                [responsiveLayout]="'scroll'"
                styleClass="books-table"
                data-testid="books-table"
                [attr.data-books-count]="(filteredBooks$ | async)?.length"
              >
                <ng-template pTemplate="header">
                  <tr>
                    <th class="book-title-col">Title</th>
                    <th class="isbn-col">ISBN</th>
                    <th class="publication-date-col">Publication Date</th>
                    <th class="actions-col">Actions</th>
                  </tr>
                </ng-template>
                <ng-template pTemplate="body" let-book let-i="rowIndex">
                  <tr
                    class="book-row"
                    [attr.data-testid]="'book-row-' + i"
                    [attr.data-book-id]="book.id"
                    [attr.data-book-title]="book.title"
                  >
                    <td class="book-title-cell">
                      <div class="book-info">
                        <div class="book-icon">
                          <i class="pi pi-book"></i>
                        </div>
                        <div class="book-details">
                          <span
                            class="book-title"
                            [attr.data-testid]="'book-title-' + i"
                          >
                            {{ book.title }}
                          </span>
                          <small class="book-id">ID: {{ book.id }}</small>
                        </div>
                      </div>
                    </td>
                    <td class="isbn-cell">
                      <span
                        class="isbn"
                        [attr.data-testid]="'book-isbn-' + i"
                      >
                        {{ book.isbn }}
                      </span>
                    </td>
                    <td class="publication-date-cell">
                      <span
                        class="publication-date"
                        [attr.data-testid]="'book-publication-date-' + i"
                      >
                        {{ book.publicationDate | date: 'mediumDate' }}
                      </span>
                    </td>
                    <td class="actions-cell">
                      <div class="action-buttons">
                        <button
                          pButton
                          type="button"
                          icon="pi pi-eye"
                          class="p-button-rounded p-button-outlined p-button-sm view-btn"
                          (click)="viewBook(book)"
                          pTooltip="View details"
                          tooltipPosition="top"
                          [attr.data-testid]="'view-book-button-' + i"
                          [attr.data-book-id]="book.id"
                        ></button>
                        <button
                          pButton
                          type="button"
                          icon="pi pi-pencil"
                          class="p-button-rounded p-button-outlined p-button-sm edit-btn"
                          (click)="editBook(book)"
                          pTooltip="Edit book"
                          tooltipPosition="top"
                          [attr.data-testid]="'edit-book-button-' + i"
                          [attr.data-book-id]="book.id"
                        ></button>
                        <button
                          pButton
                          type="button"
                          icon="pi pi-trash"
                          class="p-button-rounded p-button-outlined p-button-sm p-button-danger delete-btn"
                          (click)="deleteBook(book)"
                          pTooltip="Delete book"
                          tooltipPosition="top"
                          [attr.data-testid]="'delete-book-button-' + i"
                          [attr.data-book-id]="book.id"
                        ></button>
                      </div>
                    </td>
                  </tr>
                </ng-template>
                <ng-template pTemplate="emptymessage">
                  <tr>
                    <td colspan="5" class="empty-message" data-testid="empty-message">
                      <div class="empty-content">
                        <i class="pi pi-book empty-icon"></i>
                        <h3>No Books Found</h3>
                        <p *ngIf="hasActiveFilters(); else noBooksTemplate">
                          No books match your current filters. Try adjusting your search criteria.
                        </p>
                        <ng-template #noBooksTemplate>
                          <p>Start building your books library by adding your first book.</p>
                          <button
                            pButton
                            type="button"
                            label="Add First Book"
                            icon="pi pi-plus"
                            class="p-button-primary"
                            routerLink="/books/new"
                            data-testid="add-first-book-button"
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

    .add-book-btn {
      height: 48px;
      padding: 0 2rem;
      font-weight: 600;
      background: linear-gradient(45deg, #4facfe 0%, #00f2fe 100%);
      border: none;
      transition: all 0.3s ease;
    }

    .add-book-btn:hover {
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

    .books-count {
      color: #4facfe;
      font-weight: 700;
    }

    .table-container {
      padding: 0 1.5rem 1.5rem;
    }

    /* Table Styling */
    .books-table :deep(.p-datatable) {
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
    }

    .books-table :deep(.p-datatable-thead tr th) {
      background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
      color: white;
      font-weight: 600;
      padding: 1rem;
      border: none;
    }

    .books-table :deep(.p-datatable-tbody tr) {
      transition: all 0.2s ease;
    }

    .books-table :deep(.p-datatable-tbody tr:hover) {
      background: #f1f8ff;
      transform: translateY(-1px);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    }

    .books-table :deep(.p-datatable-tbody tr td) {
      padding: 1.5rem 1rem;
      border-bottom: 1px solid #f0f0f0;
      vertical-align: top;
    }

    /* Column Widths */
    .book-title-col { width: 25%; }
    .isbn-col { width: 20%; }
    .publication-date-col { width: 20%; }
    .actions-col { width: 20%; text-align: center; }

    /* Cell Content */
    .book-info {
      display: flex;
      align-items: flex-start;
      gap: 1rem;
    }

    .book-icon {
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

    .book-details {
      display: flex;
      flex-direction: column;
    }

    .book-title {
      font-weight: 600;
      color: #2c3e50;
      font-size: 1rem;
    }

    .book-id {
      color: #6c757d;
      font-size: 0.8rem;
      margin-top: 0.25rem;
    }

    .isbn {
      font-family: 'Courier New', monospace;
      color: #495057;
    }

    .publication-date {
      font-weight: 500;
      color: #495057;
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
    }

    @media (max-width: 768px) {
      .book-list-container {
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

      .books-table :deep(.p-datatable-tbody tr td) {
        padding: 1rem 0.5rem;
      }

      .book-info {
        flex-direction: column;
        align-items: center;
        text-align: center;
        gap: 0.5rem;
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
export class BookListComponent implements OnInit, OnDestroy {
  books$!: Observable<ApiResponse<Book[] | null>>;
  filteredBooks$!: Observable<Book[]>;
  private destroy$ = new Subject<void>();
  private filtersSubject = new BehaviorSubject<FilterOptions>({
    searchTerm: '',
    sortBy: 'title',
    sortOrder: 'asc'
  });

  filters: FilterOptions = {
    searchTerm: '',
    sortBy: 'title',
    sortOrder: 'asc'
  };

  sortOptions = [
    { label: 'Title', value: 'title' },
    { label: 'Publication Date', value: 'publicationDate' },
    { label: 'ISBN', value: 'isbn' }
  ];
  sortOrderOptions = [
    { label: 'Ascending', value: 'asc' },
    { label: 'Descending', value: 'desc' }
  ];

  isLoading = true;
  isRefreshing = false;
  hasError = false;

  constructor(
    private bookApiService: BookApiService,
    private messageService: MessageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadBooks();
    this.filtersSubject.next(this.filters);
    this.setupFilteredBooks();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadBooks(): void {
    this.isLoading = true;
    this.hasError = false;

    this.books$ = this.bookApiService.getBooks().pipe(
      catchError((error) => {
        console.error('[loadBooks] Failed to load books:', error);
        this.hasError = true;
        this.messageService.add({
          severity: 'error',
          summary: 'Loading Failed',
          detail: 'Could not load books. Please try again.',
          life: 5000
        });
        throw error;
      }),
      finalize(() => {
        this.isLoading = false;
        this.isRefreshing = false;
      }),
      takeUntil(this.destroy$),

    );

    // Manual subscription for debugging:
    this.books$.subscribe();
  }

  private setupFilteredBooks(): void {
    this.filteredBooks$ = combineLatest([
      this.books$.pipe(
        filter(response => !!response && !!response.data),
        map(response => {
          return response.data!;
        })
      ),
      this.filtersSubject.asObservable().pipe(
      )
    ])
      .pipe(
        map(([books, filters]) => {
          const result = this.applyFilters(books, filters);
          return result;
        }),
        takeUntil(this.destroy$)
      );
  }



  private applyFilters(books: Book[], filters: FilterOptions): Book[] {
    let filtered = [...books];
    // Apply search filter
    if (filters.searchTerm) {
      const searchLower = filters.searchTerm.toLowerCase();
      filtered = filtered.filter(book =>
        book.title.toLowerCase().includes(searchLower)
      );
    }



    // Apply sorting
    filtered.sort((a, b) => {
      let aValue: any;
      let bValue: any;
      switch (filters.sortBy) {
        case 'title':
          aValue = a.title.toLowerCase();
          bValue = b.title.toLowerCase();
          break;
        case 'publicationDate':
          aValue = new Date(a.publicationDate);
          bValue = new Date(b.publicationDate);
          break;
        case 'isbn':
          aValue = a.isbn;
          bValue = b.isbn;
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
      this.filters.sortBy !== 'title' ||
      this.filters.sortOrder !== 'asc'
    );
  }

  clearFilters(): void {
    this.filters = {
      searchTerm: '',
      sortBy: 'title',
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

  refreshBooks(): void {
    this.isRefreshing = true;
    this.loadBooks();
    this.messageService.add({
      severity: 'info',
      summary: 'Refreshing',
      detail: 'Updating books list...',
      life: 2000
    });
  }


  viewBook(book: Book): void {
    this.messageService.add({
      severity: 'info',
      summary: 'View Book',
      detail: `Viewing details for ${book.title}`,
      life: 2000
    });
    this.router.navigate(['/books', book.id]);
  }

  editBook(book: Book): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Edit Book',
      detail: `Editing ${book.title}`,
      life: 2000
    });
    this.router.navigate(['/books', book.id, 'edit']);
  }

  deleteBook(book: Book): void {
    this.messageService.add({
      severity: 'warn',
      summary: 'Delete Book',
      detail: `Delete functionality for ${book.title}`,
      life: 3000
    });
  }
}
