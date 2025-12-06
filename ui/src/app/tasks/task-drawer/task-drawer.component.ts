import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DrawerModule } from 'primeng/drawer';
import { TagModule } from 'primeng/tag';
import { DividerModule } from 'primeng/divider';
import { TooltipModule } from 'primeng/tooltip';
import { TaskFormComponent } from '../task-form/task-form.component';
import { TimeLogFormComponent } from '../time-log-form/time-log-form.component';
import {
  TaskDto,
  ProjectDto,
  UserResponse,
  TimeLogDto,
  TaskControllerApiService,
  TimeLogControllerApiService,
  TimeLogUpdateRequestDto
} from '@anna/flow-board-api';
import { DurationFormatPipe } from '../../shared/pipes/duration-format.pipe';
import { BillableCountPipe } from '../../shared/pipes/billable-count.pipe';

export type SidebarMode = 'view' | 'create-task' | 'edit-task' | 'add-timelog' | 'edit-timelog';

@Component({
  selector: 'app-task-drawer',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    DrawerModule,
    TagModule,
    DividerModule,
    TooltipModule,
    TaskFormComponent,
    TimeLogFormComponent,
    DurationFormatPipe,
    BillableCountPipe,
    DatePipe
  ],
  templateUrl: './task-drawer.component.html'
})
export class TaskDrawerComponent implements OnInit, OnChanges {
  private _visible = false;

  @Input()
  get visible(): boolean {
    return this._visible;
  }

  set visible(value: boolean) {
    this._visible = value;
  }

  @Input() mode: SidebarMode = 'view';
  @Input() task: TaskDto | null = null;
  @Input() projects: ProjectDto[] = [];
  @Input() users: UserResponse[] = [];
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() close = new EventEmitter<boolean>(); // boolean indicates if data changed and reload is needed
  @Output() modeChange = new EventEmitter<SidebarMode>(); // Emit mode changes to parent

  // Internal state
  timeLogs: TimeLogDto[] = [];
  loadingTimeLogs = false;
  taskFormLoading = false;
  timeLogFormLoading = false;
  editingTimeLog: TimeLogDto | null = null;
  dataChanged = false;

  constructor(
    private taskService: TaskControllerApiService,
    private timeLogService: TimeLogControllerApiService
  ) {}

  getTitle(): string {
    switch (this.mode) {
      case 'view':
        return this.task?.name || 'Task Details';
      case 'create-task':
        return 'Create New Task';
      case 'edit-task':
        return 'Edit Task';
      case 'add-timelog':
        return 'Add Time Log';
      case 'edit-timelog':
        return 'Edit Time Log';
      default:
        return 'Task Details';
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

  getTotalLoggedTime(): string {
    if (!this.timeLogs || this.timeLogs.length === 0) {
      return '0h 0m';
    }
    const totalSeconds = this.timeLogs.reduce((total, log) => {
      if (!log.loggedTime) return total;
      const match = log.loggedTime.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
      if (match) {
        const hours = parseInt(match[1] || '0', 10);
        const minutes = parseInt(match[2] || '0', 10);
        return total + (hours * 3600) + (minutes * 60);
      }
      return total;
    }, 0);

    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    return `${hours}h ${minutes}m`;
  }

  getTotalBillableTime(): string {
    if (!this.timeLogs || this.timeLogs.length === 0) {
      return '0h 0m';
    }
    const billableLogs = this.timeLogs.filter(log => log.billable);
    const totalSeconds = billableLogs.reduce((total, log) => {
      if (!log.loggedTime) return total;
      const match = log.loggedTime.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
      if (match) {
        const hours = parseInt(match[1] || '0', 10);
        const minutes = parseInt(match[2] || '0', 10);
        return total + (hours * 3600) + (minutes * 60);
      }
      return total;
    }, 0);

    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    return `${hours}h ${minutes}m`;
  }

  ngOnInit() {
    if (this.task) {
      this.loadTimeLogs();
    }
  }

  ngOnChanges(changes: any) {
    if (changes.task && this.task && this.mode === 'view') {
      this.loadTimeLogs();
    }
    if (changes.mode) {
      if (this.mode === 'add-timelog') {
        this.editingTimeLog = null;
      }
    }
  }

  loadTimeLogs() {
    if (!this.task?.id) return;

    this.loadingTimeLogs = true;
    this.timeLogService.getAllTimeLogsByTask(this.task.id).subscribe({
      next: (timeLogs) => {
        this.timeLogs = timeLogs;
        this.loadingTimeLogs = false;
      },
      error: (error) => {
        console.error('Error loading time logs:', error);
        this.loadingTimeLogs = false;
      }
    });
  }

  onTaskFormSave(taskData: any) {
    this.taskFormLoading = true;

    if (this.mode === 'edit-task' && this.task?.id) {
      // Update existing task
      this.taskService.updateTask(this.task.id, taskData).subscribe({
        next: () => {
          this.taskFormLoading = false;
          this.dataChanged = true;
          // Refresh the task data
          this.taskService.getAllTasks().subscribe({
            next: tasks => {
              const updatedTask = tasks.find(t => t.id === this.task?.id);
              if (updatedTask) {
                this.task = updatedTask;
                this.loadTimeLogs();
              }
              this.mode = 'view';
              this.modeChange.emit(this.mode);
            }
          });
        },
        error: error => {
          this.taskFormLoading = false;
          console.error('Error updating task:', error);
        }
      });
    } else if (this.mode === 'create-task') {
      // Create new task
      this.taskService.createTask(taskData).subscribe({
        next: () => {
          this.taskFormLoading = false;
          this.dataChanged = true;
          this.onClose();
        },
        error: error => {
          this.taskFormLoading = false;
          console.error('Error creating task:', error);
        }
      });
    }
  }

  onTaskFormCancel() {
    if (this.task) {
      this.mode = 'view';
      this.modeChange.emit(this.mode);
    } else {
      this.onClose();
    }
  }

  onTimeLogFormSave(timeLogData: any) {
    if (!this.task?.id) return;

    this.timeLogFormLoading = true;
    const timeLogRequest: TimeLogUpdateRequestDto = {
      taskId: this.task.id,
      ...timeLogData
    };

    if (this.mode === 'edit-timelog' && this.editingTimeLog?.id) {
      // Update existing time log
      this.timeLogService.updateTimeLog(this.editingTimeLog.id, timeLogRequest).subscribe({
        next: () => {
          this.timeLogFormLoading = false;
          this.dataChanged = true;
          this.loadTimeLogs();
          this.mode = 'view';
          this.modeChange.emit(this.mode);
          this.editingTimeLog = null;
        },
        error: error => {
          this.timeLogFormLoading = false;
          console.error('Error updating time log:', error);
        }
      });
    } else if (this.mode === 'add-timelog') {
      // Create new time log
      this.timeLogService.createTimeLog(timeLogRequest).subscribe({
        next: () => {
          this.timeLogFormLoading = false;
          this.dataChanged = true;
          this.loadTimeLogs();
          this.mode = 'view';
          this.modeChange.emit(this.mode);
        },
        error: error => {
          this.timeLogFormLoading = false;
          console.error('Error creating time log:', error);
        }
      });
    }
  }

  onTimeLogFormCancel() {
    this.mode = 'view';
    this.modeChange.emit(this.mode);
    this.editingTimeLog = null;
  }

  onAddTimeLog() {
    this.mode = 'add-timelog';
    this.editingTimeLog = null;
    this.modeChange.emit(this.mode);
  }

  onEditTask() {
    this.mode = 'edit-task';
    this.modeChange.emit(this.mode);
  }

  onEditTimeLog(timeLog: TimeLogDto) {
    this.mode = 'edit-timelog';
    this.editingTimeLog = timeLog;
    this.modeChange.emit(this.mode);
  }

  onDeleteTask() {
    if (this.task?.id && confirm('Are you sure you want to delete this task?')) {
      this.taskService.deleteTask(this.task.id).subscribe({
        next: () => {
          this.dataChanged = true;
          this.onClose();
        },
        error: error => {
          console.error('Error deleting task:', error);
        }
      });
    }
  }

  onVisibleChange(value: boolean) {
    // If trying to close and we're in edit/create mode, prevent it
    if (!value && this.mode !== 'view') {
      // Keep drawer open
      this._visible = true;
      this.visibleChange.emit(true);
      return;
    }

    // Update internal state
    this._visible = value;

    // Only emit close event if actually closing from view mode
    if (!value && this.mode === 'view') {
      this.onClose();
    }
  }

  onClose() {
    this.close.emit(this.dataChanged);
    this.dataChanged = false;
    this.mode = 'view';
    this.modeChange.emit(this.mode);
    this.editingTimeLog = null;
  }
}

