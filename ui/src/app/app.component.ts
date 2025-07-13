import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MenuModule } from 'primeng/menu';
import { ChartModule } from 'primeng/chart';
import { CardModule } from 'primeng/card';
import { MenuComponent } from './shared/components/menu/menu.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, MenuComponent, FormsModule, MenuModule, ChartModule, CardModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'flowboard';
 
}
