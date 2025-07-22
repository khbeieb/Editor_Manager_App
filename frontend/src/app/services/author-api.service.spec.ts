import { TestBed } from '@angular/core/testing';

import { AuthorApiService } from './author-api.service';

describe('AuthorApiService', () => {
  let service: AuthorApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthorApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
