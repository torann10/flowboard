import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { TooltipModule } from 'primeng/tooltip';
import { TaskModalComponent } from './task-modal/task-modal.component';
import { TimeLogModalComponent } from './time-log-modal/time-log-modal.component';
import {
  TaskControllerApiService,
  ProjectControllerApiService,
  UserControllerApiService,
  UserResponse,
  TaskDto, ProjectDto
} from '@anna/flow-board-api';
import { DurationFormatPipe } from '../shared/pipes/duration-format.pipe';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { Select } from 'primeng/select';
import { filter } from 'rxjs';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    TableModule,
    CardModule,
    TagModule,
    DialogModule,
    InputTextModule,
    DropdownModule,
    CalendarModule,
    TooltipModule,
    TaskModalComponent,
    TimeLogModalComponent,
    DurationFormatPipe,
    IconField,
    InputIcon,
    Select
  ],
  templateUrl: './tasks.component.html'
})
export class TasksComponent implements OnInit {
  tasks: TaskDto[] = [];
  projects: ProjectDto[] = [];
  users: UserResponse[] = [];
  loading = false;
  showTaskModal = false;
  showTimeLogModal = false;
  selectedTask: TaskDto | null = null;
  selectedTaskForTimeLog: TaskDto | null = null;
  isEditMode = false;

  statusOptions = [
    { label: 'Open', value: TaskDto.StatusEnum.Open, severity: 'info' },
    { label: 'In Progress', value: TaskDto.StatusEnum.InProgress, severity: 'warn' },
    { label: 'Done', value: TaskDto.StatusEnum.Done, severity: 'success' },
    { label: 'Canceled', value: TaskDto.StatusEnum.Canceled, severity: 'danger' }
  ];

  projectOptions: { label: string; value: string }[] = [{ label: 'All Projects', value: '' }];

  log(asd: any) {
    console.log(asd.value);
  }

  constructor(
    private taskService: TaskControllerApiService,
    private projectService: ProjectControllerApiService,
    private userService: UserControllerApiService
  ) {}

  ngOnInit() {
    this.loadTasks();
    this.loadProjects();
    this.loadUsers();
  }

  loadTasks() {
    this.loading = true;
    this.taskService.getAllTasks().subscribe({
      next: tasks => {
        this.tasks = tasks;
        this.loading = false;
      },
      error: error => {
        console.error('Error loading tasks:', error);
        this.loading = false;
      }
    });
  }

  loadProjects() {
    this.projectService.getAllProjects().subscribe({
      next: projects => {
        this.projects = projects;
        this.projectOptions = [
          { label: 'All Projects', value: '' },
          ...projects.map(project => ({
            label: project.name || 'Unnamed Project',
            value: project.id || ''
          }))
        ];
      },
      error: error => {
        console.error('Error loading projects:', error);
      }
    });
  }

  loadUsers() {
    this.userService.getAllUsers().subscribe({
      next: users => {
        this.users = users;
      },
      error: error => {
        console.error('Error loading users:', error);
      }
    });
  }

  openCreateTask() {
    this.selectedTask = null;
    this.isEditMode = false;
    this.showTaskModal = true;
  }

  openEditTask(task: TaskDto) {
    this.selectedTask = task;
    this.isEditMode = true;
    this.showTaskModal = true;
  }

  openTimeLogModal(task: TaskDto) {
    this.selectedTaskForTimeLog = task;
    this.showTimeLogModal = true;
  }

  onTaskModalClose() {
    this.showTaskModal = false;
    this.selectedTask = null;
  }

  onTimeLogModalClose() {
    this.showTimeLogModal = false;
    this.selectedTaskForTimeLog = null;
  }

  onTaskSaved() {
    this.loadTasks();
    this.onTaskModalClose();
  }

  onTimeLogSaved() {
    this.loadTasks(); // Refresh to update booked time
    this.onTimeLogModalClose();
  }

  deleteTask(task: TaskDto) {
    if (task.id && confirm('Are you sure you want to delete this task?')) {
      this.taskService.deleteTask(task.id).subscribe({
        next: () => {
          this.loadTasks();
        },
        error: error => {
          console.error('Error deleting task:', error);
        }
      });
    }
  }

  getStatusSeverity(status: string | undefined): 'success' | 'info' | 'warn' | 'secondary' | 'contrast' | 'danger' | undefined {
    switch (status) {
      case 'OPEN':
        return 'info';
      case 'IN_PROGRESS':
        return 'warn';
      case 'DONE':
        return 'success';
      case 'CANCELED':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  getStatusLabel(status: string | undefined): string {
    switch (status) {
      case 'OPEN':
        return 'Open';
      case 'IN_PROGRESS':
        return 'In Progress';
      case 'DONE':
        return 'Done';
      case 'CANCELED':
        return 'Canceled';
      default:
        return 'Unknown';
    }
  }

  getProjectName(projectId: string | undefined): string {
    const project = this.projects.find(p => p.id === projectId);
    return project?.name || 'Unknown Project';
  }

  getAssignedUserName(userId: string | undefined): string {
    if (!userId) {
      return 'Unassigned';
    }

    const user = this.users.find(u => u.id === userId);
    if (user) {
      const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim();
      return fullName || user.emailAddress || 'Unknown User';
    }
    return 'Unknown User';
  }
}
