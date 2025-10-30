import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { DropdownModule } from 'primeng/dropdown';
import { MessageModule } from 'primeng/message';
import { TabViewModule } from 'primeng/tabview';
import { ProjectControllerApiService, ProjectDto, ProjectUpdateRequestDto } from '@anna/flow-board-api';
import { UserAssignmentComponent } from './user-assignment/user-assignment.component';
import { StoryPointConverterService } from '../../shared/services/story-point-converter.service';
import { StoryPointMappingsComponent, StoryPointMapping } from './story-point-mappings/story-point-mappings.component';

@Component({
  selector: 'app-project-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DialogModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    DropdownModule,
    MessageModule,
    TabViewModule,
    UserAssignmentComponent,
    StoryPointMappingsComponent
  ],
  templateUrl: './project-modal.component.html',
  styleUrl: './project-modal.component.scss'
})
export class ProjectModalComponent implements OnInit, OnChanges {
  @Input() visible = false;
  @Input() project: ProjectDto | null = null;
  @Input() isEditMode = false;
  @Output() close = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();

  projectForm: FormGroup;
  loading = false;
  errorMessage = '';
  showUserAssignment = false;
  storyPointMappings: StoryPointMapping[] = [];

  statusOptions = [
    { label: 'Active', value: 'ACTIVE' },
    { label: 'Completed', value: 'COMPLETED' },
    { label: 'Archived', value: 'ARCHIVED' }
  ];

  typeOptions = [
    { label: 'Time-based', value: 'TIME_BASED' },
    { label: 'Story Point-based', value: 'STORY_POINT_BASED' }
  ];

  constructor(
    private fb: FormBuilder,
    private projectService: ProjectControllerApiService,
    private storyPointConverter: StoryPointConverterService
  ) {
    this.projectForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      status: ['ACTIVE', Validators.required],
      type: ['TIME_BASED', Validators.required],
      storyPointTimeValue: [null, [Validators.min(0.1), Validators.max(100)]]
    });
  }

  ngOnInit() {
    if (this.project) {
      this.projectForm.patchValue({
        name: this.project.name,
        status: this.project.status || 'ACTIVE',
        type: (this.project as any).type || 'TIME_BASED',
        storyPointTimeValue: (this.project as any).storyPointTimeValue || null
      });
    }

    // Add dynamic validation for story point time value
    this.projectForm.get('type')?.valueChanges.subscribe(type => {
      const storyPointTimeValueControl = this.projectForm.get('storyPointTimeValue');
      if (type === 'STORY_POINT_BASED') {
        storyPointTimeValueControl?.setValidators([Validators.required, Validators.min(0.1), Validators.max(100)]);
      } else {
        storyPointTimeValueControl?.clearValidators();
      }
      storyPointTimeValueControl?.updateValueAndValidity();
    });
  }

  ngOnChanges() {
    if (this.project && this.isEditMode) {
      this.projectForm.patchValue({
        name: this.project.name,
        status: this.project.status || 'ACTIVE',
        type: (this.project as any).type || 'TIME_BASED',
        storyPointTimeValue: (this.project as any).storyPointTimeValue || null
      });
    } else if (!this.isEditMode) {
      this.projectForm.reset({
        name: '',
        status: 'ACTIVE',
        type: 'TIME_BASED',
        storyPointTimeValue: null
      });
    }
  }

  onSave() {
    if (this.projectForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      const formData = this.projectForm.value;
      const projectData = {
        name: formData.name,
        status: formData.status,
        type: formData.type,
        storyPointTimeValue: this.storyPointMappings.map(value => ({
          timeValue: this.parseDurationInput(value.timeDisplay),
          storyPoints: value.storyPoints,
        }))
      } as ProjectUpdateRequestDto;

      if (this.isEditMode && this.project?.id) {
        // Update existing project
        this.projectService.updateProject(this.project.id, projectData).subscribe({
          next: () => {
            this.loading = false;
            this.saved.emit();
          },
          error: (error: any) => {
            this.loading = false;
            this.errorMessage = 'Error updating project. Please try again.';
            console.error('Error updating project:', error);
          }
        });
      } else {
        // Create new project
        this.projectService.createProject(projectData).subscribe({
          next: () => {
            this.loading = false;
            this.saved.emit();
          },
          error: (error: any) => {
            this.loading = false;
            this.errorMessage = 'Error creating project. Please try again.';
            console.error('Error creating project:', error);
          }
        });
      }
    } else {
      this.markFormGroupTouched();
    }
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

  onCancel() {
    this.close.emit();
  }

  openUserAssignment() {
    this.showUserAssignment = true;
  }

  onUserAssignmentClose() {
    this.showUserAssignment = false;
  }

  onUsersUpdated() {
    // Users were updated, could refresh project data if needed
  }

  private markFormGroupTouched() {
    Object.keys(this.projectForm.controls).forEach(key => {
      const control = this.projectForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.projectForm.get(fieldName);
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

  isStoryPointBased(): boolean {
    return this.projectForm.get('type')?.value === 'STORY_POINT_BASED';
  }

  getCommonStoryPointValues() {
    const storyPointTimeValue = this.projectForm.get('storyPointTimeValue')?.value;
    if (storyPointTimeValue && storyPointTimeValue > 0) {
      return this.storyPointConverter.getCommonStoryPointValues(storyPointTimeValue);
    }
    return [];
  }

  onStoryPointMappingsChange(mappings: StoryPointMapping[]) {
    this.storyPointMappings = mappings;
  }

  getStoryPointTimeValue(storyPoints: number): string {
    const mapping = this.storyPointMappings.find(m => m.storyPoints === storyPoints);
    if (mapping) {
      return mapping.timeDisplay;
    }

    // Fallback to linear calculation if no custom mapping exists
    const storyPointTimeValue = this.projectForm.get('storyPointTimeValue')?.value;
    if (storyPointTimeValue && storyPointTimeValue > 0) {
      return this.storyPointConverter.convertStoryPointsToTime(storyPoints, storyPointTimeValue);
    }

    return '';
  }
}
