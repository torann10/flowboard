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
import {
  ProjectControllerApiService,
  ProjectDto,
  ProjectUpdateRequestDto,
  StoryPointTimeMappingDto,
  CompanyDto
} from '@anna/flow-board-api';
import { UserAssignmentComponent } from './user-assignment/user-assignment.component';
import { StoryPointMappingsComponent } from './story-point-mappings/story-point-mappings.component';
import { parseDurationInput } from '../../shared/helpers/duration-helper';

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
  private readonly defaultStoryPointMappings: StoryPointTimeMappingDto[] = [
    { storyPoints: 1, timeValue: 'PT2H' },
    { storyPoints: 2, timeValue: 'PT4H' },
    { storyPoints: 3, timeValue: 'PT6H' },
    { storyPoints: 5, timeValue: 'PT10H' },
    { storyPoints: 8, timeValue: 'PT16H' },
    { storyPoints: 13, timeValue: 'PT26H' },
    { storyPoints: 21, timeValue: 'PT42H' }
  ];


  @Input() visible = false;
  @Input() project: ProjectDto | null = null;
  @Input() isEditMode = false;
  @Output() close = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();

  projectForm: FormGroup;
  loading = false;
  errorMessage = '';
  showUserAssignment = false;
  private previousVisible = false;

  statusOptions = [
    { label: 'Active', value: ProjectDto.StatusEnum.Active },
    { label: 'Completed', value: ProjectDto.StatusEnum.Completed },
    { label: 'Archived', value: ProjectDto.StatusEnum.Archived }
  ];

  typeOptions = [
    { label: 'Time-based', value: ProjectDto.TypeEnum.TimeBased },
    { label: 'Story Point-based', value: ProjectDto.TypeEnum.StoryPointBased }
  ];

  constructor(
    private fb: FormBuilder,
    private projectService: ProjectControllerApiService,
  ) {
    this.projectForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      status: [ProjectDto.StatusEnum.Active, Validators.required],
      type: [ProjectDto.TypeEnum.TimeBased, Validators.required],
      storyPointFee: [null, [Validators.min(0)]],
      storyPointTimeMappings: [this.defaultStoryPointMappings],
      customerName: ['', Validators.required],
      customerAddress: ['', Validators.required],
      contractorName: ['', Validators.required],
      contractorAddress: ['', Validators.required]
    });
  }

  ngOnInit() {
    if (this.project) {
      this.projectForm.patchValue({
        name: this.project.name,
        status: this.project.status || ProjectDto.StatusEnum.Active,
        type: this.project.type || ProjectDto.TypeEnum.TimeBased,
        storyPointFee: this.project.storyPointFee || null,
        storyPointTimeMappings: this.project.storyPointTimeMappings || [],
        customerName: this.project.customer?.name || '',
        customerAddress: this.project.customer?.address || '',
        contractorName: this.project.contractor?.name || '',
        contractorAddress: this.project.contractor?.address || ''
      });
    }
  }

  ngOnChanges() {
    // Only reset when dialog becomes visible and we're in create mode
    if (this.visible && !this.previousVisible && !this.isEditMode) {
      this.initializeFormForCreate();
    }

    if (this.project && this.isEditMode && this.visible) {
      this.projectForm.patchValue({
        name: this.project.name,
        status: this.project.status || ProjectDto.StatusEnum.Active,
        type: this.project.type || ProjectDto.TypeEnum.TimeBased,
        storyPointFee: this.project.storyPointFee || null,
        storyPointTimeMappings: this.project.storyPointTimeMappings || [],
        customerName: this.project.customer?.name || '',
        customerAddress: this.project.customer?.address || '',
        contractorName: this.project.contractor?.name || '',
        contractorAddress: this.project.contractor?.address || ''
      });
      this.projectForm.updateValueAndValidity();
    }

    this.previousVisible = this.visible;
  }

  private initializeFormForCreate() {
    // Reset the entire form
    this.projectForm.reset({
      name: '',
      status: ProjectDto.StatusEnum.Active,
      type: ProjectDto.TypeEnum.TimeBased,
      storyPointFee: null,
      storyPointTimeMappings: this.defaultStoryPointMappings,
      customerName: '',
      customerAddress: '',
      contractorName: '',
      contractorAddress: ''
    });

    // Ensure form validation state is updated
    this.projectForm.updateValueAndValidity();
  }

  onSave() {
    // Update form validity before checking
    this.projectForm.updateValueAndValidity();

    if (this.projectForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      const formData = this.projectForm.value;
      const projectData = {
        name: formData.name,
        status: formData.status,
        type: formData.type,
        storyPointFee: formData.type === ProjectDto.TypeEnum.StoryPointBased ? formData.storyPointFee : undefined,
        storyPointTimeMappings: formData.storyPointTimeMappings,
        customer: {
          name: formData.customerName,
          address: formData.customerAddress
        } as CompanyDto,
        contractor: {
          name: formData.contractorName,
          address: formData.contractorAddress
        } as CompanyDto
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
      // Log form errors for debugging
    }
  }

  private getFormErrors(): any {
    const errors: any = {};
    Object.keys(this.projectForm.controls).forEach(key => {
      const control = this.projectForm.get(key);
      if (control && control.errors) {
        errors[key] = control.errors;
      }
    });
    return errors;
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
      if (control) {
        control.markAsTouched();
      }
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.projectForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        const displayName = fieldName.charAt(0).toUpperCase() + fieldName.slice(1).replace(/([A-Z])/g, ' $1').trim();
        return `${displayName} is required`;
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

  get isStoryPointBased(): boolean {
    return this.projectForm.get('type')?.value === ProjectDto.TypeEnum.StoryPointBased;
  }
}
