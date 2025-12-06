import { Component, OnInit } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { CommonModule, DatePipe } from '@angular/common';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReportModalComponent } from './report-modal/report-modal.component';
import { ReportControllerApiService, ReportDto } from '@anna/flow-board-api';

@Component({
  selector: 'app-reports',
  imports: [
    ButtonModule,
    CardModule,
    TableModule,
    CommonModule,
    ReportModalComponent,
    ConfirmDialogModule,
    ToastModule,
    DialogModule,
    InputTextModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent implements OnInit {
  showModal = false;
  showRenameDialog = false;
  reports: ReportDto[] = [];
  loading = false;
  renamingReport: ReportDto | null = null;
  renameForm: FormGroup;

  columns = [
    { field: 'name', header: 'Report Name' },
    { field: 'projectName', header: 'Project' },
    { field: 'dateRange', header: 'Date Range' },
    { field: 'createdAt', header: 'Created At' },
    { field: 'download', header: 'Actions' }
  ];

  constructor(
    private reportService: ReportControllerApiService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private fb: FormBuilder
  ) {
    this.renameForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]]
    });
  }

  ngOnInit() {
    this.loadReports();
  }

  loadReports() {
    this.loading = true;
    this.reportService.listReportsForUser().subscribe({
      next: (reports) => {
        this.reports = reports || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  openCreateModal() {
    this.showModal = true;
  }

  onModalClose() {
    this.showModal = false;
  }

  onReportSaved(reportId?: string) {
    this.onModalClose();
    this.loadReports();

    // Automatically download the newly created report if ID is provided
    if (reportId) {
      this.downloadReportById(reportId);
    }
  }

  downloadReportById(reportId?: string) {
    if (!reportId) {
      return;
    }

    this.reportService.getReportDownloadUrl(reportId).subscribe({
      next: (presignedUrl) => {
        if (presignedUrl && presignedUrl.downloadUrl) {
          const link = document.createElement('a');
          link.href = presignedUrl.downloadUrl;
          link.target = '_blank';
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
        }
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to download report'
        });
      }
    });
  }

  deleteReport(report: ReportDto) {
    if (!report.id) {
      return;
    }

    this.confirmationService.confirm({
      message: `Are you sure you want to delete the report "${report.name || 'this report'}"?`,
      header: 'Confirm Deletion',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.reportService.deleteReport(report.id!).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Report deleted successfully'
            });
            this.loadReports();
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to delete report'
            });
          }
        });
      }
    });
  }

  openRenameDialog(report: ReportDto) {
    if (!report.id) {
      return;
    }

    this.renamingReport = report;
    this.renameForm.patchValue({
      name: report.name || ''
    });
    this.showRenameDialog = true;
  }

  closeRenameDialog() {
    this.showRenameDialog = false;
    this.renamingReport = null;
    this.renameForm.reset();
  }

  saveRename() {
    if (this.renameForm.valid && this.renamingReport?.id) {
      const newName = this.renameForm.get('name')?.value;
      
      this.reportService.renameReport(this.renamingReport.id, newName).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Report renamed successfully'
          });
          this.closeRenameDialog();
          this.loadReports();
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to rename report'
          });
        }
      });
    }
  }
}
