import { Component, inject, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Menu, MenuModule } from 'primeng/menu';
import { Ripple } from 'primeng/ripple';
import { UserService } from '../../../services/user.service';
import { Divider } from 'primeng/divider';
import { Button } from 'primeng/button';
import { KeycloakService } from 'keycloak-angular';
import Keycloak from 'keycloak-js';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-menu',
  imports: [MenuModule, Menu, Ripple, Divider, Button, RouterLink],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent implements OnInit {
  items: MenuItem[] | undefined;

  readonly userService = inject(UserService);
  readonly keycloak = inject(Keycloak);

  ngOnInit() {
    this.items = [
      {
        separator: true
      },
      {
        label: 'Time Tracking',
        icon: 'pi pi-clock',
        routerLink: '/time-tracking'
      },
      {
        label: 'Projects',
        icon: 'pi pi-folder',
        routerLink: '/projects'
      },
      {
        label: 'Tasks',
        icon: 'pi pi-list-check',
        routerLink: '/tasks'
      },
      {
        label: 'Reports',
        icon: 'pi pi-file-check',
        routerLink: '/reports'
      }
    ];
  }
}
