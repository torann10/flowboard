import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CalendarModule } from 'primeng/calendar';
import { CheckboxModule } from 'primeng/checkbox';
import { MessageModule } from 'primeng/message';
import { TimeLogDto } from '@anna/flow-board-api';
import { parseDurationInput } from '../../shared/helpers/duration-helper';

@Component({
  selector: 'app-time-log-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    CalendarModule,
    CheckboxModule,
    MessageModule
  ],
  templateUrl: './time-log-form.component.html'
})
export class TimeLogFormComponent implements OnInit, OnChanges {
  @Input() timeLog: TimeLogDto | null = null;
  @Input() isEditMode = false;
  @Input() loading = false;
  @Output() save = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  timeLogForm: FormGroup;
  errorMessage = '';

  constructor(private fb: FormBuilder) {
    this.timeLogForm = this.fb.group({
      loggedTime: ['', [Validators.required]],
      logDate: [new Date(), [Validators.required]],
      isBillable: [true]
    });
  }

  ngOnInit() {
    this.resetForm();
  }

  ngOnChanges() {
    if (this.timeLog && this.isEditMode) {
      this.timeLogForm.patchValue({
        loggedTime: this.parseDurationForEdit(this.timeLog.loggedTime),
        logDate: new Date(this.timeLog.logDate || ''),
        isBillable: this.timeLog.billable
      });
    } else {
      this.resetForm();
    }
  }

  resetForm() {
    this.timeLogForm.reset({
      loggedTime: '',
      logDate: new Date(),
      isBillable: true
    });
  }

  onSave() {
    if (this.timeLogForm.valid) {
      const formValue = this.timeLogForm.value;
      const timeLogData = {
        loggedTime: parseDurationInput(formValue.loggedTime),
        logDate: this.formatDateForApi(formValue.logDate),
        billable: formValue.isBillable
      };
      this.save.emit(timeLogData);
    } else {
      this.markFormGroupTouched();
    }
  }

  onCancel() {
    this.cancel.emit();
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

  formatDateForApi(date: Date | string | undefined): string {
    if (!date) return '';
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    const year = dateObj.getFullYear();
    const month = String(dateObj.getMonth() + 1).padStart(2, '0');
    const day = String(dateObj.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  parseDurationForEdit(duration: string | undefined): string {
    if (!duration) return '';
    const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
    if (match) {
      const hours = parseInt(match[1] || '0', 10);
      const minutes = parseInt(match[2] || '0', 10);
      if (hours > 0 && minutes > 0) {
        return `${hours}h ${minutes}m`;
      } else if (hours > 0) {
        return `${hours}h`;
      } else if (minutes > 0) {
        return `${minutes}m`;
      }
    }
    return '';
  }
}


