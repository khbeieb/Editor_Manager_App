import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MenubarModule } from 'primeng/menubar';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, MenubarModule],
  template: `
    <p-menubar [model]="items"></p-menubar>
    <div class="p-4">
      <router-outlet></router-outlet>
    </div>
  `,
})
export class AppComponent {
  items: MenuItem[] = [
    { label: 'Authors', routerLink: '/authors' },
    { label: 'Books', routerLink: '/books' },
    { label: 'Magazines', routerLink: '/magazines' },
  ];
}
