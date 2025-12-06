import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';
import {
  TaskDto,
  ProjectDto,
  UserResponse
} from '@anna/flow-board-api';
import { Select } from 'primeng/select';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    TextareaModule,
    MessageModule,
    Select
  ],
  templateUrl: './task-form.component.html'
})
export class TaskFormComponent implements OnInit, OnChanges {
  @Input() task: TaskDto | null = null;
  @Input() projects: ProjectDto[] = [];
  @Input() users: UserResponse[] = [];
  @Input() isEditMode = false;
  @Input() loading = false;
  @Output() save = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  taskForm: FormGroup;
  errorMessage = '';
  selectedProject: ProjectDto | null = null;

  statusOptions = [
    { label: 'Open', value: 'OPEN' },
    { label: 'In Progress', value: 'IN_PROGRESS' },
    { label: 'Done', value: 'DONE' },
    { label: 'Canceled', value: 'CANCELED' }
  ];

  projectOptions: { label: string; value: string }[] = [];
  userOptions: { label: string; value: string }[] = [];

  constructor(private fb: FormBuilder) {
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
    this.updateOptions();

    // Watch for project changes
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
    this.updateOptions();
    if (this.task && this.isEditMode) {
      this.taskForm.patchValue({
        name: this.task.name,
        description: this.task.description,
        status: this.task.status,
        projectId: this.task.projectId,
        assignTo: this.task.assignedToId || null,
        storyPointMappingId: this.task.storyPointMappingId || null,
      });
      this.taskForm.get('storyPointMappingId')?.enable();
      this.selectedProject = this.projects.find(p => p.id === this.task?.projectId) || null;
    } else {
      this.taskForm.reset({
        name: '',
        description: '',
        status: 'OPEN',
        projectId: '',
        assignTo: '',
        storyPointMappingId: null
      });
      this.selectedProject = null;
    }
  }

  updateOptions() {
    this.projectOptions = this.projects.map(project => ({
      label: project.name || 'Unnamed Project',
      value: project.id || ''
    }));

    this.userOptions = this.users.map(user => ({
      label: `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.emailAddress || 'Unknown User',
      value: user.id || ''
    }));
  }

  onSave() {
    if (this.taskForm.valid) {
      const formValue = this.taskForm.value;
      const taskData = {
        name: formValue.name,
        description: formValue.description,
        status: formValue.status,
        projectId: formValue.projectId,
        assignedToId: formValue.assignTo || undefined,
        storyPointMappingId: formValue.storyPointMappingId
      };
      this.save.emit(taskData);
    } else {
      this.markFormGroupTouched();
    }
  }

  onCancel() {
    this.cancel.emit();
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
    }
    return '';
  }
}

