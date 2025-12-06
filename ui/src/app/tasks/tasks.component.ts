import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { TooltipModule } from 'primeng/tooltip';
import { DividerModule } from 'primeng/divider';
import { SidebarMode, TaskDrawerComponent } from './task-drawer/task-drawer.component';
import {
  ProjectControllerApiService,
  ProjectDto,
  TaskControllerApiService,
  TaskDto,
  UserControllerApiService,
  UserResponse
} from '@anna/flow-board-api';
import { DurationFormatPipe } from '../shared/pipes/duration-format.pipe';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { Select } from 'primeng/select';
import { InputText } from 'primeng/inputtext';

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
    TooltipModule,
    DividerModule,
    TaskDrawerComponent,
    DurationFormatPipe,
    IconField,
    InputIcon,
    Select,
    InputText
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
  showTaskDrawer = false;
  selectedTask: TaskDto | null = null;
  drawerMode: SidebarMode = 'view';

  statusOptions = [
    { label: 'Open', value: TaskDto.StatusEnum.Open, severity: 'info' },
    { label: 'In Progress', value: TaskDto.StatusEnum.InProgress, severity: 'warn' },
    { label: 'Done', value: TaskDto.StatusEnum.Done, severity: 'success' },
    { label: 'Canceled', value: TaskDto.StatusEnum.Canceled, severity: 'danger' }
  ];

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
    this.drawerMode = 'create-task';
    this.showTaskDrawer = true;
  }

  openTaskDrawer(task: TaskDto) {
    this.selectedTask = task;
    this.drawerMode = 'view';
    this.showTaskDrawer = true;
  }

  onDrawerClose(reloadNeeded: boolean) {
    this.showTaskDrawer = false;
    this.drawerMode = 'view';
    this.selectedTask = null;
    if (reloadNeeded) {
      this.loadTasks();
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
