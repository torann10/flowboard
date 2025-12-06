import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
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
  CreateProjectActivityReportRequestDto,
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
  @Output() saved = new EventEmitter<string>();

  reportForm: FormGroup;
  loading = false;
  errorMessage = '';
  projects: ProjectDto[] = [];
  projectOptions: { label: string; value: string }[] = [];
  reportTypeOptions = [
    { label: 'COC Report', value: 'coc' },
    { label: 'Employee Matrix Report', value: 'employee-matrix' },
    { label: 'Project Activity Report', value: 'project-activity' }
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
    } else if (reportType === 'project-activity') {
      // Project Activity report requires projectId but not description
      projectIdControl?.setValidators([Validators.required]);
      descriptionControl?.clearValidators();
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
      error: () => {
        // Error loading projects - silently fail
      }
    });
  }

  private formatDateForApi(date: Date | null | undefined): string | undefined {
    if (!date) {
      return undefined;
    }
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  onSave() {
    this.reportForm.updateValueAndValidity();
    
    if (this.reportForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      const formData = this.reportForm.value;
      const reportType = formData.reportType;
      const startDate = this.formatDateForApi(formData.startDate);
      const endDate = this.formatDateForApi(formData.endDate);

      let reportObservable: Observable<string>;

      try {
        if (!this.reportService) {
          throw new Error('ReportService is not initialized');
        }
        if (reportType === 'coc') {
          const reportData: CreateCOCReportRequestDto = {
            projectId: formData.projectId,
            startDate: startDate,
            endDate: endDate,
            description: formData.description
          };
          reportObservable = this.reportService.createCocReport(reportData);
        } else if (reportType === 'project-activity') {
          const reportData: CreateProjectActivityReportRequestDto = {
            projectId: formData.projectId,
            startDate: startDate,
            endDate: endDate
          };
          reportObservable = this.reportService.createProjectActivityReport(reportData);
        } else {
          const reportData: CreateEmployeeMatrixReportRequestDto = {
            startDate: startDate,
            endDate: endDate
          };
          reportObservable = this.reportService.createEmployeeMatrixReport(reportData);
        }

        if (!reportObservable) {
          this.loading = false;
          this.errorMessage = 'Failed to create report request. Please try again.';
          return;
        }
        
        reportObservable.subscribe({
          next: (reportId: string) => {
            this.loading = false;
            this.saved.emit(reportId);
          },
          error: () => {
            this.loading = false;
            this.errorMessage = 'Error generating report. Please try again.';
          }
        });
      } catch (error) {
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

  get isProjectActivityReport(): boolean {
    return this.reportForm.get('reportType')?.value === 'project-activity';
  }

  get showProjectField(): boolean {
    const reportType = this.reportForm.get('reportType')?.value;
    return reportType === 'coc' || reportType === 'project-activity';
  }
}

