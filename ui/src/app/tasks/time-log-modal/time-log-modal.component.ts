import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CalendarModule } from 'primeng/calendar';
import { CheckboxModule } from 'primeng/checkbox';
import { MessageModule } from 'primeng/message';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { TaskDto, TimeLogControllerApiService, TimeLogDto, TimeLogUpdateRequestDto } from '@anna/flow-board-api';
import { BillableCountPipe } from '../../shared/pipes/billable-count.pipe';
import { DurationFormatPipe } from '../../shared/pipes/duration-format.pipe';

@Component({
  selector: 'app-time-log-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DialogModule,
    ButtonModule,
    InputTextModule,
    CalendarModule,
    CheckboxModule,
    MessageModule,
    TableModule,
    TagModule,
    TooltipModule,
    BillableCountPipe,
    DurationFormatPipe
  ],
  templateUrl: './time-log-modal.component.html'
})
export class TimeLogModalComponent implements OnInit, OnChanges {
  @Input() visible = false;
  @Input() task: TaskDto | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();

  timeLogForm: FormGroup;
  timeLogs: TimeLogDto[] = [];
  loading = false;
  errorMessage = '';
  editingTimeLog: TimeLogDto | null = null;
  isEditMode = false;

  constructor(
    private fb: FormBuilder,
    private timeLogService: TimeLogControllerApiService
  ) {
    this.timeLogForm = this.fb.group({
      loggedTime: ['', [Validators.required]],
      logDate: [new Date(), [Validators.required]],
      isBillable: [true]
    });
  }

  ngOnInit() {
    if (this.task) {
      this.loadTimeLogs();
    }
  }

  ngOnChanges() {
    if (this.task) {
      this.loadTimeLogs();
    }
  }

  loadTimeLogs() {
    if (!this.task?.id) return;

    this.loading = true;
    this.timeLogService.getAllTimeLogs().subscribe({
      next: (timeLogs) => {
        // Filter time logs for this specific task
        this.timeLogs = timeLogs.filter(log => log.taskId === this.task?.id);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading time logs:', error);
        this.loading = false;
      }
    });
  }

  onSave() {
    if (this.timeLogForm.valid && this.task?.id) {
      this.loading = true;
      this.errorMessage = '';

      const formValue = this.timeLogForm.value;
      const timeLogData: TimeLogUpdateRequestDto = {
        taskId: this.task.id,
        loggedTime: this.parseDurationInput(formValue.loggedTime),
        logDate: this.formatDate(formValue.logDate),
        billable: formValue.isBillable
      };

      if (this.isEditMode && this.editingTimeLog?.id) {
        // Update existing time log
        this.timeLogService.updateTimeLog(this.editingTimeLog.id, timeLogData).subscribe({
          next: () => {
            this.loading = false;
            this.saved.emit();
            this.resetForm();
            this.loadTimeLogs(); // Refresh the list
          },
          error: (error) => {
            this.loading = false;
            this.errorMessage = 'Error updating time log. Please try again.';
            console.error('Error updating time log:', error);
          }
        });
      } else {
        // Create new time log
        this.timeLogService.createTimeLog(timeLogData).subscribe({
          next: () => {
            this.loading = false;
            this.saved.emit();
            this.resetForm();
            this.loadTimeLogs(); // Refresh the list
          },
          error: (error) => {
            this.loading = false;
            this.errorMessage = 'Error creating time log. Please try again.';
            console.error('Error creating time log:', error);
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

  resetForm() {
    this.timeLogForm.reset({
      loggedTime: '',
      logDate: new Date(),
      isBillable: true
    });
    this.isEditMode = false;
    this.editingTimeLog = null;
  }

  editTimeLog(timeLog: TimeLogDto) {
    this.editingTimeLog = timeLog;
    this.isEditMode = true;

    // Populate form with existing data
    this.timeLogForm.patchValue({
      loggedTime: this.parseDurationForEdit(timeLog.loggedTime),
      logDate: new Date(timeLog.logDate || ''),
      isBillable: timeLog.billable,
    });
  }

  parseDurationForEdit(duration: string | undefined): string {
    if (!duration) return '';

    // Parse ISO 8601 duration (PT2H30M) back to user-friendly format
    const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
    if (!match) return '';

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

  cancelEdit() {
    this.resetForm();
  }

  deleteTimeLog(timeLog: TimeLogDto) {
    if (timeLog.id && confirm('Are you sure you want to delete this time log entry?')) {
      this.timeLogService.deleteTimeLog(timeLog.id).subscribe({
        next: () => {
          this.loadTimeLogs();
        },
        error: (error) => {
          console.error('Error deleting time log:', error);
        }
      });
    }
  }

  markFormGroupTouched() {
    Object.keys(this.timeLogForm.controls).forEach(key => {
      const control = this.timeLogForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.timeLogForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} is required`;
      }
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

  formatDate(date: Date | string | undefined): string {
    if (!date) return '';

    const dateObj = typeof date === 'string' ? new Date(date) : date;
    return dateObj.toISOString().split('T')[0]; // YYYY-MM-DD format
  }

  getTotalLoggedTime(): string {
    const totalMinutes = this.timeLogs.reduce((total, log) => {
      if (!log.loggedTime) return total;

      const match = log.loggedTime.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
      if (!match) return total;

      const hours = parseInt(match[1] || '0');
      const minutes = parseInt(match[2] || '0');

      return total + (hours * 60) + minutes;
    }, 0);

    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;

    if (hours > 0 && minutes > 0) {
      return `${hours}h ${minutes}m`;
    } else if (hours > 0) {
      return `${hours}h`;
    } else if (minutes > 0) {
      return `${minutes}m`;
    }
    return '0h';
  }

  getTotalBillableTime(): string {
    const billableLogs = this.timeLogs.filter(log => log.billable);
    const totalMinutes = billableLogs.reduce((total, log) => {
      if (!log.loggedTime) return total;

      const match = log.loggedTime.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
      if (!match) return total;

      const hours = parseInt(match[1] || '0');
      const minutes = parseInt(match[2] || '0');

      return total + (hours * 60) + minutes;
    }, 0);

    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;

    if (hours > 0 && minutes > 0) {
      return `${hours}h ${minutes}m`;
    } else if (hours > 0) {
      return `${hours}h`;
    } else if (minutes > 0) {
      return `${minutes}m`;
    }
    return '0h';
  }
}
