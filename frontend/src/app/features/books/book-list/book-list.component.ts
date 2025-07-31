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
import { BookApiService } from '../../../services/book-api.service';
import { Book } from '../../../models/book.model';
import { ApiResponse } from '../../../models/api-response.model';

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
  templateUrl: './book-list.component.html',
  styleUrls: ['./book-list.component.scss']
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
