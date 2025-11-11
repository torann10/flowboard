import { Component } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { CommonModule } from '@angular/common';
import { ReportModalComponent } from './report-modal/report-modal.component';

@Component({
  selector: 'app-reports',
  imports: [ButtonModule, CardModule, TableModule, CommonModule, ReportModalComponent],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent {
  showModal = false;

  columns = [
    { field: 'project', header: 'Project' },
    { field: 'timeInterval', header: 'Time Interval' },
    { field: 'total', header: 'Total' },
    { field: 'createdAt', header: 'Created At' },
    { field: 'download', header: '' }
  ];

  reports = [
    { project: 'Project A', timeInterval: 'Last 7 days', total: 1111 + ' HUF', createdAt: '2023-10-01', download: 'Download' },
    { project: 'Project B', timeInterval: 'Last 30 days', total: 77777777 + ' HUF', createdAt: '2023-09-15', download: 'Download' },
    { project: 'Project C', timeInterval: 'Last 90 days', total: 1000000 + ' HUF', createdAt: '2023-08-01', download: 'Download' }
  ];

  openCreateModal() {
    this.showModal = true;
  }

  onModalClose() {
    this.showModal = false;
  }

  onReportSaved() {
    this.onModalClose();
    // Optionally reload reports list here
  }
}
