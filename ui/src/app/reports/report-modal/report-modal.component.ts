import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextarea } from 'primeng/inputtextarea';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { MessageModule } from 'primeng/message';
import {
  ReportControllerApiService,
  ReportCreateRequestDto,
  ProjectControllerApiService,
  ProjectDto
} from '@anna/flow-board-api';

@Component({
  selector: 'app-report-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DialogModule,
    ButtonModule,
    InputTextModule,
    InputTextarea,
    DropdownModule,
    CalendarModule,
    MessageModule
  ],
  templateUrl: './report-modal.component.html',
  styleUrl: './report-modal.component.scss'
})
export class ReportModalComponent implements OnInit, OnChanges {
  @Input() visible = false;
  @Output() close = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();

  reportForm: FormGroup;
  loading = false;
  errorMessage = '';
  projects: ProjectDto[] = [];
  projectOptions: { label: string; value: string }[] = [];
  private previousVisible = false;

  constructor(
    private fb: FormBuilder,
    private reportService: ReportControllerApiService,
    private projectService: ProjectControllerApiService
  ) {
    this.reportForm = this.fb.group({
      projectId: ['', Validators.required],
      startDate: [null, Validators.required],
      endDate: [null, Validators.required],
      description: ['', [Validators.required, Validators.maxLength(1000)]]
    });
  }

  ngOnInit() {
    this.loadProjects();
  }

  ngOnChanges() {
    if (this.visible && !this.previousVisible) {
      this.initializeForm();
    }
    this.previousVisible = this.visible;
  }

  private initializeForm() {
    this.reportForm.reset({
      projectId: '',
      startDate: null,
      endDate: null,
      description: ''
    });
    this.reportForm.markAsPristine();
    this.reportForm.markAsUntouched();
    this.reportForm.updateValueAndValidity();
  }

  loadProjects() {
    this.projectService.getAllProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.projectOptions = projects.map(p => ({
          label: p.name || '',
          value: p.id || ''
        }));
      },
      error: (error) => {
        console.error('Error loading projects:', error);
      }
    });
  }

  onSave() {
    this.reportForm.updateValueAndValidity();
    
    if (this.reportForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      const formData = this.reportForm.value;
      const reportData: ReportCreateRequestDto = {
        projectId: formData.projectId,
        startDate: formData.startDate ? new Date(formData.startDate).toISOString().split('T')[0] : undefined,
        endDate: formData.endDate ? new Date(formData.endDate).toISOString().split('T')[0] : undefined,
        description: formData.description
      };

      this.reportService.createReport(reportData, 'response', true, {
        httpHeaderAccept: 'application/pdf'
      }).subscribe({
        next: (response) => {
          this.loading = false;
          
          // Handle PDF download
          if (response.body) {
            // Convert response body to Blob (handles ArrayBuffer, string, or Blob)
            let blob: Blob;
            if (response.body instanceof Blob) {
              blob = response.body;
            } else if (response.body instanceof ArrayBuffer) {
              blob = new Blob([response.body], { type: 'application/pdf' });
            } else {
              // If it's a string, convert to ArrayBuffer first
              const arrayBuffer = new TextEncoder().encode(response.body).buffer;
              blob = new Blob([arrayBuffer], { type: 'application/pdf' });
            }
            
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            
            // Get filename from Content-Disposition header if available
            const contentDisposition = response.headers.get('Content-Disposition');
            let filename = 'teljesitesi_igazolas.pdf';
            if (contentDisposition) {
              const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
              if (filenameMatch && filenameMatch[1]) {
                filename = filenameMatch[1].replace(/['"]/g, '');
              }
            }
            
            link.download = filename;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
          }
          
          this.saved.emit();
        },
        error: (error: any) => {
          this.loading = false;
          this.errorMessage = 'Error generating report. Please try again.';
          console.error('Error creating report:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  onVisibleChange(visible: boolean) {
    if (!visible) {
      this.close.emit();
    }
  }

  onCancel() {
    this.close.emit();
  }

  private markFormGroupTouched() {
    Object.keys(this.reportForm.controls).forEach(key => {
      const control = this.reportForm.get(key);
      if (control) {
        control.markAsTouched();
      }
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.reportForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        const displayName = fieldName.charAt(0).toUpperCase() + fieldName.slice(1).replace(/([A-Z])/g, ' $1').trim();
        return `${displayName} is required`;
      }
      if (field.errors['maxlength']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} must not exceed ${field.errors['maxlength'].requiredLength} characters`;
      }
    }
    return '';
  }
}

