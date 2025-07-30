import {Book} from './book.model';
import {Magazine} from './magazine.model';
import {ApiResponse} from './api-response.model';

export interface PublicationGrouped {
  books: Book[];
  magazines: Magazine[];
}

export type PublicationGroupedResponse = ApiResponse<PublicationGrouped>;
