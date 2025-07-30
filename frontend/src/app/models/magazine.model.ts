import {AuthorBasic} from './authorBasic.model';

export interface Magazine {
  id: number;
  issueNumber: number;
  title: string;
  publishedDate: string;
  authors: AuthorBasic[];
}
