import { Component, effect, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MenuModule } from 'primeng/menu';
import { ChartModule } from 'primeng/chart';
import { CardModule } from 'primeng/card';
import { MenuComponent } from './shared/components/menu/menu.component';
import { KEYCLOAK_EVENT_SIGNAL, KeycloakEventType, ReadyArgs, typeEventArgs } from 'keycloak-angular';
import Keycloak from 'keycloak-js';
import { NgIf } from '@angular/common';
import { UserService } from './services/user.service';
import { ProgressSpinner } from 'primeng/progressspinner';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, MenuComponent, FormsModule, MenuModule, ChartModule, CardModule, NgIf, ProgressSpinner],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'Flowboard';

  authenticated = false;
  keycloakStatus: string | undefined;
  private readonly keycloak = inject(Keycloak);
  private readonly keycloakSignal = inject(KEYCLOAK_EVENT_SIGNAL);
  readonly userService = inject(UserService);

  constructor() {
    effect(() => {
      const keycloakEvent = this.keycloakSignal();

      this.keycloakStatus = keycloakEvent.type;

      if (keycloakEvent.type === KeycloakEventType.Ready) {
        this.authenticated = typeEventArgs<ReadyArgs>(keycloakEvent.args);

        if (!this.authenticated) {
          this.keycloak.login();
        }
      }

      if (keycloakEvent.type === KeycloakEventType.AuthLogout) {
        this.authenticated = false;
      }
    });
  }

  login() {
    this.keycloak.login();
  }

  logout() {
    this.keycloak.logout();
  }
}
