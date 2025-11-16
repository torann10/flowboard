import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextarea } from 'primeng/inputtextarea';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { MessageModule } from 'primeng/message';
import {
  ReportControllerApiService,
  CreateCOCReportRequestDto,
  CreateEmployeeMatrixReportRequestDto,
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
  reportTypeOptions = [
    { label: 'COC Report', value: 'coc' },
    { label: 'Employee Matrix Report', value: 'employee-matrix' }
  ];
  private previousVisible = false;

  constructor(
    private fb: FormBuilder,
    private reportService: ReportControllerApiService,
    private projectService: ProjectControllerApiService
  ) {
    this.reportForm = this.fb.group({
      reportType: ['coc', Validators.required],
      projectId: [''],
      startDate: [null, Validators.required],
      endDate: [null, Validators.required],
      description: ['']
    });

    // Set up conditional validation based on report type
    this.reportForm.get('reportType')?.valueChanges.subscribe(reportType => {
      this.updateFormValidation(reportType);
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
      reportType: 'coc',
      projectId: '',
      startDate: null,
      endDate: null,
      description: ''
    });
    this.updateFormValidation('coc');
    this.reportForm.markAsPristine();
    this.reportForm.markAsUntouched();
    this.reportForm.updateValueAndValidity();
  }

  private updateFormValidation(reportType: string) {
    const projectIdControl = this.reportForm.get('projectId');
    const descriptionControl = this.reportForm.get('description');

    if (reportType === 'coc') {
      // COC report requires projectId and description
      projectIdControl?.setValidators([Validators.required]);
      descriptionControl?.setValidators([Validators.required, Validators.maxLength(1000)]);
    } else {
      // Employee Matrix report doesn't require projectId or description
      projectIdControl?.clearValidators();
      descriptionControl?.clearValidators();
    }

    projectIdControl?.updateValueAndValidity({ emitEvent: false });
    descriptionControl?.updateValueAndValidity({ emitEvent: false });
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
      const reportType = formData.reportType;
      const startDate = formData.startDate ? new Date(formData.startDate).toISOString().split('T')[0] : undefined;
      const endDate = formData.endDate ? new Date(formData.endDate).toISOString().split('T')[0] : undefined;

      console.log('Generating report:', { reportType, startDate, endDate, formData });

      let reportObservable: Observable<HttpResponse<any>>;

      try {
        if (reportType === 'coc') {
          const reportData: CreateCOCReportRequestDto = {
            projectId: formData.projectId,
            startDate: startDate,
            endDate: endDate,
            description: formData.description
          };
          console.log('Calling createCocReport with data:', reportData);
          reportObservable = this.reportService.createCocReport(reportData, 'response', true, {
            httpHeaderAccept: 'application/pdf'
          });
        } else {
          const reportData: CreateEmployeeMatrixReportRequestDto = {
            startDate: startDate,
            endDate: endDate
          };
          console.log('Calling createEmployeeMatrixReport with data:', reportData);
          reportObservable = this.reportService.createEmployeeMatrixReport(reportData, 'response', true, {
            httpHeaderAccept: 'application/pdf'
          });
        }

        console.log('Subscribing to observable...', reportObservable);
        if (!reportObservable) {
          console.error('Observable is null or undefined!');
          this.loading = false;
          this.errorMessage = 'Failed to create report request. Please try again.';
          return;
        }
        
        reportObservable.subscribe({
          next: (response: HttpResponse<any>) => {
            console.log('Response received:', response);
            try {
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
                let filename = reportType === 'coc' ? 'teljesitesi_igazolas.pdf' : 'munkavallaloi-matrix.pdf';
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
                
                this.saved.emit();
              } else {
                this.errorMessage = 'No PDF data received from server.';
                console.error('Response body is null or undefined');
              }
            } catch (error) {
              this.errorMessage = 'Error processing PDF download. Please try again.';
              console.error('Error processing PDF:', error);
            } finally {
              this.loading = false;
            }
          },
          error: (error: any) => {
          this.loading = false;
          this.errorMessage = 'Error generating report. Please try again.';
          console.error('Error creating report:', error);
        },
        complete: () => {
          // Ensure loading is reset even if next doesn't fire
          if (this.loading) {
            this.loading = false;
          }
        }
      });
      } catch (error) {
        console.error('Error creating observable:', error);
        this.loading = false;
        this.errorMessage = 'Failed to create report request. Please try again.';
      }
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

  get isCocReport(): boolean {
    return this.reportForm.get('reportType')?.value === 'coc';
  }
}

