import { Component, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Menu, MenuModule } from 'primeng/menu';

@Component({
  selector: 'app-menu',
  imports: [ MenuModule, Menu ],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent implements OnInit {
  items: MenuItem[] | undefined;

  ngOnInit() {
    this.items = [
      {
        label: 'Dashboard',
        icon: 'pi pi-home',
        routerLink: '/dashboard',
      },
      {
        label: 'Time Tracking',
        icon: 'pi pi-clock',
        routerLink: '/time-tracking',
      },
      {
        label: 'Tasks',
        icon: 'pi pi-list-check',
        routerLink: '/tasks',
      },
      {
        label: 'Board',
        icon: 'pi pi-clipboard',
        routerLink: '/board',
      },
      {
        label: 'Reports',
        icon: 'pi pi-file-check',
        routerLink: '/reports',
      },
    ];
  }

}
