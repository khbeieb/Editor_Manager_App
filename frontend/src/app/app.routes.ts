import { Routes } from '@angular/router';
import {AuthorListComponent} from './features/authors/author-list/author-list.component';
import {AuthorFormComponent} from './features/authors/author-form/author-form.component';
import {BookListComponent} from './features/books/book-list.component';
import {BookFormComponent} from './features/books/book-form.component';
import {MagazineListComponent} from './features/magazines/magazine-list.component';
import {MagazineFormComponent} from './features/magazines/magazine-form.component';
import {PublicationListComponent} from './features/publications/publication-list.component';

export const routes: Routes = [
  { path: '', redirectTo: 'authors', pathMatch: 'full' },
  { path: 'authors', component: AuthorListComponent },
  { path: 'authors/new', component: AuthorFormComponent },
  { path: 'books', component: BookListComponent },
  { path: 'books/new', component: BookFormComponent },
  { path: 'magazines', component: MagazineListComponent },
  { path: 'magazines/new', component: MagazineFormComponent },
  { path: 'publications', component: PublicationListComponent },
];
