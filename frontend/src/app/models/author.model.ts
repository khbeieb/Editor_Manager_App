import {Book} from './book.model';

export interface Author {
  id?: number;
  name: string;
  birthDate: string; // ISO date string
  nationality: string;
  books?: Book[];
}
