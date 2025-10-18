import { CommonModule } from '@angular/common';
import { Component, Output } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DataViewModule } from 'primeng/dataview';
import { TableModule } from 'primeng/table';
import { TaskModalComponent } from './task-modal/task-modal.component';

@Component({
  selector: 'app-tasks',
  imports: [TableModule, CommonModule, CardModule, ButtonModule, DataViewModule, TaskModalComponent],
  templateUrl: './tasks.component.html',
  styleUrl: './tasks.component.scss'
})
export class TasksComponent {
  @Output() globalFilter: string = '';

  visible: boolean = false;

  showDialog() {
    this.visible = true;
  }

  columns = [
    { field: 'title', header: 'Task' },
    { field: 'estimatedTime', header: 'Estimated time' },
    { field: 'bookedTime', header: 'Booked time' },
    { field: 'done', header: 'Done' }
  ];

  tasks = [
    { title: 'Login page', estimatedTime: 4, bookedTime: 3.5, done: true },
    { title: 'Auth flow', estimatedTime: 6, bookedTime: 6, done: true },
    { title: 'Unit tests', estimatedTime: 3, bookedTime: 1.5, done: false },
    { title: 'Bug fix', estimatedTime: 2, bookedTime: 2, done: false }
  ];
}
