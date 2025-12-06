import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';
import { CalendarModule } from 'primeng/calendar';
import {
  TaskControllerApiService,
  ProjectControllerApiService,
  UserControllerApiService,
  UserResponse,
  TaskDto, ProjectDto
} from '@anna/flow-board-api';
import { Select } from 'primeng/select';

@Component({
  selector: 'app-task-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DialogModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    TextareaModule,
    MessageModule,
    CalendarModule,
    Select,
  ],
  templateUrl: './task-modal.component.html'
})
export class TaskModalComponent implements OnInit, OnChanges {
  @Input() visible = false;
  @Input() task: TaskDto | null = null;
  @Input() isEditMode = false;
  @Output() close = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();

  taskForm: FormGroup;
  loading = false;
  errorMessage = '';
  projects: ProjectDto[] = [];
  users: UserResponse[] = [];
  selectedProject: ProjectDto | null = null;

  statusOptions = [
    { label: 'Open', value: 'OPEN' },
    { label: 'In Progress', value: 'IN_PROGRESS' },
    { label: 'Done', value: 'DONE' },
    { label: 'Canceled', value: 'CANCELED' }
  ];

  projectOptions: { label: string; value: string }[] = [];
  userOptions: { label: string; value: string }[] = [];

  constructor(
    private fb: FormBuilder,
    private taskService: TaskControllerApiService,
    private projectService: ProjectControllerApiService,
    private userService: UserControllerApiService,
  ) {
    this.taskForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      status: ['OPEN', Validators.required],
      projectId: ['', Validators.required],
      assignTo: [''],
      storyPointMappingId: [null]
    });

    this.taskForm.get('storyPointMappingId')?.disable();
  }

  ngOnInit() {
    this.loadProjects();
    this.loadUsers();

    // Watch for project changes to determine if story points should be shown
    this.taskForm.get('projectId')?.valueChanges.subscribe(projectId => {
      this.selectedProject = this.projects.find(p => p.id === projectId) || null;

      if (!!this.selectedProject) {
        this.taskForm.get('storyPointMappingId')?.enable();
      } else {
        this.taskForm.get('storyPointMappingId')?.disable();
      }
    });
  }

  ngOnChanges() {
    if (this.task && this.isEditMode) {
      this.taskForm.patchValue({
        name: this.task.name,
        description: this.task.description,
        status: this.task.status,
        projectId: this.task.projectId,
        assignTo: this.task.assignedToId || null,
        storyPointMappingId: this.task.storyPointMappingId || null,
      });
      // Set selected project for validation
      this.selectedProject = this.projects.find(p => p.id === this.task?.projectId) || null;
    } else {
      this.taskForm.reset({
        name: '',
        description: '',
        status: 'OPEN',
        projectId: '',
        assignTo: '',
        estimatedTime: '',
        storyPointMappingId: null
      });
      this.selectedProject = null;
    }
  }

  loadProjects() {
    this.projectService.getAllProjects().subscribe({
      next: projects => {
        this.projects = projects;
        this.projectOptions = projects.map(project => ({
          label: project.name || 'Unnamed Project',
          value: project.id || ''
        }));
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
        this.userOptions = users.map(user => ({
          label: `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.emailAddress || 'Unknown User',
          value: user.id || ''
        }));
      },
      error: error => {
        console.error('Error loading users:', error);
      }
    });
  }

  onSave() {
    if (this.taskForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      const formValue = this.taskForm.value;
      const taskData: any = {
        name: formValue.name,
        description: formValue.description,
        status: formValue.status,
        projectId: formValue.projectId,
        assignedToId: formValue.assignTo || undefined,
        storyPointMappingId: formValue.storyPointMappingId
      };

      if (this.isEditMode && this.task?.id) {
        // Update existing task
        this.taskService.updateTask(this.task.id, taskData).subscribe({
          next: () => {
            this.loading = false;
            this.saved.emit();
          },
          error: error => {
            this.loading = false;
            this.errorMessage = 'Error updating task. Please try again.';
            console.error('Error updating task:', error);
          }
        });
      } else {
        // Create new task
        this.taskService.createTask(taskData).subscribe({
          next: () => {
            this.loading = false;
            this.saved.emit();
          },
          error: error => {
            this.loading = false;
            this.errorMessage = 'Error creating task. Please try again.';
            console.error('Error creating task:', error);
          }
        });
      }
    } else {
      this.markFormGroupTouched();
    }
  }

  onCancel() {
    this.close.emit();
  }

  markFormGroupTouched() {
    Object.keys(this.taskForm.controls).forEach(key => {
      const control = this.taskForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.taskForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} is required`;
      }
      if (field.errors['minlength']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} must be at least ${field.errors['minlength'].requiredLength} characters`;
      }
      if (field.errors['min']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} must be at least ${field.errors['min'].min}`;
      }
      if (field.errors['max']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} must be at most ${field.errors['max'].max}`;
      }
    }
    return '';
  }
}
