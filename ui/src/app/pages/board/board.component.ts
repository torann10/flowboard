import { Component } from '@angular/core';
import { PanelModule } from 'primeng/panel';
import { CardModule } from 'primeng/card';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-board',
  imports: [PanelModule, CardModule, CommonModule],
  templateUrl: './board.component.html',
  styleUrl: './board.component.scss'
})
export class BoardComponent {
  tasks = [
    { title: 'Implement Login', est: '4h', status: 'Not started' },
    { title: 'Fix Bug #123', est: '2h', status: 'Blocked' },
    { title: 'Write Unit Tests', est: '3h', status: 'Pending' },
  ];

}
