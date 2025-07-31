import {Component, OnInit, OnDestroy} from '@angular/core';
import {CommonModule, DatePipe, NgIf} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {TableModule} from 'primeng/table';
import {ButtonModule, ButtonDirective} from 'primeng/button';
import {CardModule} from 'primeng/card';
import {TagModule} from 'primeng/tag';
import {ToastModule} from 'primeng/toast';
import {ProgressSpinnerModule} from 'primeng/progressspinner';
import {InputTextModule} from 'primeng/inputtext';
import {SelectModule} from 'primeng/select';
import {RouterLink, Router} from '@angular/router';
import {Observable, BehaviorSubject, combineLatest, Subject} from 'rxjs';
import {map, takeUntil, filter, catchError, finalize} from 'rxjs/operators';
import {MessageService} from 'primeng/api';
import {MagazineApiService} from '../../../services/magazine-api.service';
import {Magazine} from '../../../models/magazine.model';
import {ApiResponse} from '../../../models/api-response.model';

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
  templateUrl: './magazine-list.component.html',
  styleUrls: ['./magazine-list.component.scss']
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
  ) {
  }

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
