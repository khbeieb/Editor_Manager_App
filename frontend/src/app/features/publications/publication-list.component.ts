// src/app/components/publication-list/publication-list.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import {
  CommonModule,
  DatePipe,
  NgIf
} from '@angular/common';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageService } from 'primeng/api';
import { PublicationApiService } from '../../services/publication-api.service';
import {takeUntil, catchError, finalize, map} from 'rxjs/operators';
import {Subject, Observable, of, filter, tap} from 'rxjs';
import { RouterLink } from '@angular/router';
import {PublicationGrouped} from '../../models/publication.model';
import {ButtonDirective} from 'primeng/button';

@Component({
  standalone: true,
  selector: 'app-publication-list',
  templateUrl: './publication-list.component.html',
  styleUrls: ['./publication-list.component.scss'],
  imports: [
    CommonModule,
    NgIf,
    DatePipe,
    TableModule,
    CardModule,
    TagModule,
    ToastModule,
    ProgressSpinnerModule,
    RouterLink,
    ButtonDirective
  ],
  providers: [MessageService]
})
export class PublicationListComponent implements OnInit, OnDestroy {
  groupedPublications$!: Observable<PublicationGrouped>;
  isLoading = true;
  hasError = false;
  private destroy$ = new Subject<void>();

  constructor(
    private publicationApiService: PublicationApiService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.loadPublications();
  }

  protected loadPublications(): void {
    console.log('[loadPublications] Called');
    this.isLoading = true;
    this.hasError = false;

    this.groupedPublications$ = this.publicationApiService.getGroupedPublications().pipe(
      tap(res => console.log('[API Success]', res)),
      map(res => {
        console.log('[Mapping] res.data =', res.data);
        return res.data ?? { books: [], magazines: [] };
      }),
      catchError(error => {
        this.hasError = true;
        console.error('[API Error]', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error loading publications',
          detail: 'Please try again.',
        });
        return of({ books: [], magazines: [] });
      }),
      finalize(() => {
        this.isLoading = false;
        console.log('[finalize] Loading done');
      }),
      takeUntil(this.destroy$)
    );

    this.groupedPublications$.subscribe();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
