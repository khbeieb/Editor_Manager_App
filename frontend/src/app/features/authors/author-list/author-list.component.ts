import { Component, OnInit, OnDestroy } from '@angular/core';
import { AsyncPipe, DatePipe, NgForOf, NgIf, CommonModule } from '@angular/common';
import { AuthorApiService } from '../../../services/author-api.service';
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
import { Author } from '../../../models/author.model';
import { ApiResponse } from '../../../models/api-response.model';
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
  templateUrl: './author-list.component.html' ,
  styleUrls: ['./author-list.component.scss'],
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
