import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { TextareaModule } from 'primeng/textarea';
import { DropdownModule } from 'primeng/dropdown';
import { MessageModule } from 'primeng/message';
import { CalendarModule } from 'primeng/calendar';
import {
  TaskControllerApiService,
  ProjectControllerApiService,
  UserControllerApiService,
  UserResponse,
  TaskDto, ProjectDto
} from '@anna/flow-board-api';
import { StoryPointConverterService, CustomStoryPointMapping } from '../../shared/services/story-point-converter.service';

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
    DropdownModule,
    MessageModule,
    CalendarModule
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
  storyPointTimeValue: number = 0;
  calculatedTime: string = '';
  customStoryPointMappings: CustomStoryPointMapping[] = [];

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
    private storyPointConverter: StoryPointConverterService
  ) {
    this.taskForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      status: ['OPEN', Validators.required],
      projectId: ['', Validators.required],
      assignTo: [''],
      estimatedTime: [''],
      storyPoints: [null, [Validators.min(1), Validators.max(100)]]
    });
  }

  ngOnInit() {
    this.loadProjects();
    this.loadUsers();

    // Watch for project changes to determine if story points should be shown
    this.taskForm.get('projectId')?.valueChanges.subscribe(projectId => {
      this.selectedProject = this.projects.find(p => p.id === projectId) || null;
      this.updateStoryPointTimeValue();
      this.updateFieldValidators();
    });

    // Watch for story points changes to calculate time
    this.taskForm.get('storyPoints')?.valueChanges.subscribe(() => {
      this.calculateTimeFromStoryPoints();
    });
  }

  ngOnChanges() {
    if (this.task && this.isEditMode) {
      this.taskForm.patchValue({
        name: this.task.name,
        description: this.task.description,
        status: this.task.status,
        projectId: this.task.projectId,
        assignTo: this.task.assignTo || '',
        estimatedTime: this.task.estimatedTime,
        storyPoints: (this.task as any).storyPoints || null
      });
      // Set selected project for validation
      this.selectedProject = this.projects.find(p => p.id === this.task?.projectId) || null;
      this.updateStoryPointTimeValue();
    } else {
      this.taskForm.reset({
        name: '',
        description: '',
        status: 'OPEN',
        projectId: '',
        assignTo: '',
        estimatedTime: '',
        storyPoints: null
      });
      this.selectedProject = null;
    }
    this.updateFieldValidators();
  }

  loadProjects() {
    this.projectService.getAllProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.projectOptions = projects.map(project => ({
          label: project.name || 'Unnamed Project',
          value: project.id || ''
        }));
      },
      error: (error) => {
        console.error('Error loading projects:', error);
      }
    });
  }

  loadUsers() {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.userOptions = users.map(user => ({
          label: `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.emailAddress || 'Unknown User',
          value: user.id || ''
        }));
      },
      error: (error) => {
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
        assignTo: formValue.assignTo || undefined,
        estimatedTime: formValue.estimatedTime,
        storyPoints: this.isStoryPointBased() ? formValue.storyPoints : null
      };

      if (this.isEditMode && this.task?.id) {
        // Update existing task
        this.taskService.updateTask(this.task.id, taskData).subscribe({
          next: () => {
            this.loading = false;
            this.saved.emit();
          },
          error: (error) => {
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
          error: (error) => {
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

  formatDurationInput(duration: string): string {
    if (!duration) return '';

    // Convert from ISO 8601 format (PT2H30M) to user-friendly format (2h 30m)
    const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
    if (!match) return duration;

    const hours = parseInt(match[1] || '0');
    const minutes = parseInt(match[2] || '0');

    if (hours > 0 && minutes > 0) {
      return `${hours}h ${minutes}m`;
    } else if (hours > 0) {
      return `${hours}h`;
    } else if (minutes > 0) {
      return `${minutes}m`;
    }
    return '';
  }

  parseDurationInput(input: string): string {
    if (!input) return '';

    // Convert from user-friendly format (2h 30m) to ISO 8601 format (PT2H30M)
    const hourMatch = input.match(/(\d+)h/);
    const minuteMatch = input.match(/(\d+)m/);

    const hours = hourMatch ? parseInt(hourMatch[1]) : 0;
    const minutes = minuteMatch ? parseInt(minuteMatch[1]) : 0;

    if (hours > 0 || minutes > 0) {
      return `PT${hours}H${minutes}M`;
    }

    return '';
  }

  onEstimatedTimeChange() {
    const currentValue = this.taskForm.get('estimatedTime')?.value;
    if (currentValue && !currentValue.startsWith('PT')) {
      const parsed = this.parseDurationInput(currentValue);
      this.taskForm.patchValue({ estimatedTime: parsed });
    }
  }

  isStoryPointBased(): boolean {
    return this.selectedProject?.type === 'STORY_POINT_BASED';
  }

  updateFieldValidators() {
    const storyPointsControl = this.taskForm.get('storyPoints');
    const estimatedTimeControl = this.taskForm.get('estimatedTime');

    if (this.isStoryPointBased()) {
      // For story point based projects, story points are required, estimated time is optional
      storyPointsControl?.setValidators([Validators.required, Validators.min(1), Validators.max(100)]);
      estimatedTimeControl?.clearValidators();
    } else {
      // For time based projects, estimated time is optional, story points are not used
      storyPointsControl?.clearValidators();
      estimatedTimeControl?.clearValidators();
    }

    storyPointsControl?.updateValueAndValidity();
    estimatedTimeControl?.updateValueAndValidity();
  }

  updateStoryPointTimeValue() {
    if (this.selectedProject && this.isStoryPointBased()) {
      this.storyPointTimeValue = (this.selectedProject as any).storyPointTimeValue || 0;
      this.customStoryPointMappings = (this.selectedProject as any).storyPointTimeMappings || [];
      this.calculateTimeFromStoryPoints();
    } else {
      this.storyPointTimeValue = 0;
      this.customStoryPointMappings = [];
      this.calculatedTime = '';
    }
  }

  calculateTimeFromStoryPoints() {
    const storyPoints = this.taskForm.get('storyPoints')?.value;
    if (storyPoints && storyPoints > 0) {
      this.calculatedTime = this.storyPointConverter.convertStoryPointsToTimeWithCustomMappings(
        storyPoints,
        this.customStoryPointMappings,
        this.storyPointTimeValue
      );
    } else {
      this.calculatedTime = '';
    }
  }

  getCommonStoryPointValues() {
    if (this.customStoryPointMappings.length > 0) {
      return this.storyPointConverter.getCustomStoryPointValues(this.customStoryPointMappings);
    } else if (this.storyPointTimeValue > 0) {
      return this.storyPointConverter.getCommonStoryPointValues(this.storyPointTimeValue);
    }
    return [];
  }
}
