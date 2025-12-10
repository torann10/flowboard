import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { CardModule } from 'primeng/card';
import { TimeLogControllerApiService, TimeLogDto } from '@anna/flow-board-api';
import { DurationFormatPipe } from '../shared/pipes/duration-format.pipe';
import { UIChart } from 'primeng/chart';
import { Button } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { FormsModule } from '@angular/forms';
import { Select } from 'primeng/select';

enum IntervalType {
  Week = 'WEEK',
  Month = 'MONTH',
  Custom = 'CUSTOM',
}

@Component({
  selector: 'app-time-tracking',
  standalone: true,
  imports: [CommonModule, CardModule, DurationFormatPipe, UIChart, Button, DatePicker, FormsModule, Select],
  templateUrl: './time-tracking.component.html'
})
export class TimeTrackingComponent implements OnInit {
  protected readonly IntervalType = IntervalType;
  private _selectedDateRange: Date[] = [];
  private _intervalType: IntervalType = IntervalType.Week;
  intervalOptions = [
    { label: 'Week', value: IntervalType.Week },
    { label: 'Month', value: IntervalType.Month },
    { label: 'Custom', value: IntervalType.Custom },
  ]
  timeLogs: TimeLogDto[] = [];
  loading = false;
  errorMessage = '';
  now!: Date;
  today!: Date;

  // Summary data
  todayHours: string[] = [];
  weekHours: string[] = [];
  monthHours: string[] = [];
  yearHours: string[] = [];

  // Chart data
  basicData: any;
  basicOptions: any;

  get intervalType(): IntervalType {
    return this._intervalType;
  }

  set intervalType(value: IntervalType) {
    this._intervalType = value;

    const startDate = this._selectedDateRange[0];

    if (value === IntervalType.Week) {
      const weekStart = new Date(startDate);
      weekStart.setDate(startDate.getDate() - startDate.getDay() + 1);
      const weekEnd = new Date(weekStart);
      weekEnd.setDate(weekStart.getDate() + 6);
      this._selectedDateRange = [weekStart, weekEnd];
    } else if (value === IntervalType.Month) {
      const monthStart = new Date(startDate.getFullYear(), startDate.getMonth(), 1);
      const monthEnd = new Date(startDate.getFullYear(), startDate.getMonth() + 1, 1);
      monthEnd.setDate(0);
      this._selectedDateRange = [monthStart, monthEnd];
    }

    this.initChart();
  }

  get selectedDateRange(): Date[] {
    return this._selectedDateRange;
  }

  set selectedDateRange(value: Date[]) {
    this._selectedDateRange = value;
    this._intervalType = IntervalType.Custom;
    this.initChart();
  }

  constructor(
    private timeLogService: TimeLogControllerApiService,
    private durationFormatPipe: DurationFormatPipe,
    private datePipe: DatePipe
  ) {}

  ngOnInit() {
    this.now = new Date();
    this.today = new Date(this.now.getFullYear(), this.now.getMonth(), this.now.getDate());
    this.loadTimeLogs();
    const startDate = new Date(this.today);
    startDate.setDate(this.today.getDate() - this.today.getDay() + 1);
    const endDate = new Date(startDate);
    endDate.setDate(startDate.getDate() + 6);
    this._selectedDateRange = [startDate, endDate];
  }

  loadTimeLogs() {
    this.loading = true;
    this.timeLogService.getAllTimeLogs().subscribe({
      next: timeLogs => {
        this.timeLogs = timeLogs;
        this.calculateSummaries();
        this.initChart();
        this.loading = false;
      },
      error: error => {
        console.error('Error loading time logs:', error);
        this.errorMessage = 'Error loading time tracking data';
        this.loading = false;
      }
    });
  }

  calculateSummaries() {
    const weekStart = new Date(this.today);
    weekStart.setDate(this.today.getDate() - this.today.getDay() + 1); // Monday
    const monthStart = new Date(this.now.getFullYear(), this.now.getMonth(), 1);
    const yearStart = new Date(this.now.getFullYear(), 0, 1);

    this.todayHours = this.calculateHoursForPeriod(this.today, new Date(this.today.getTime() + 24 * 60 * 60 * 1000));
    this.weekHours = this.calculateHoursForPeriod(weekStart, this.now);
    this.monthHours = this.calculateHoursForPeriod(monthStart, this.now);
    this.yearHours = this.calculateHoursForPeriod(yearStart, this.now);
  }

  calculateDataForRange() {
    let startDate = this._selectedDateRange[0];
    let endDate = this._selectedDateRange[1];
    let currentDate = new Date(startDate);

    let index = 0;
    const result: string[][] = [];
    const labels: string[] = [];

    while (currentDate <= endDate) {
      const dayEnd = new Date(currentDate);
      dayEnd.setDate(currentDate.getDate() + 1);

      result.push(this.calculateHoursForPeriod(currentDate, dayEnd));

      let label = this.datePipe.transform(currentDate, 'MM-dd, EEEE') as string;

      if (
        this.now.getFullYear() === currentDate.getFullYear() &&
        this.now.getMonth() === currentDate.getMonth() &&
        this.now.getDate() === currentDate.getDate()
      ) {
        label += ' (Today)';
      }

      labels.push(label);
      index++;
      currentDate.setDate(currentDate.getDate() + 1);
    }

    return { data: result, labels: labels };
  }

  initChart() {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--p-text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--p-text-muted-color');
    const surfaceBorder = documentStyle.getPropertyValue('--p-content-border-color');
    const data = this.calculateDataForRange();
    const workedHours = data.data.map(data => this.durationFormatPipe.transform(data, false) as number);

    this.basicData = {
      labels: data.labels,
      datasets: [
        {
          label: 'Worked hours',
          data: workedHours,
          backgroundColor: ['rgba(249, 115, 22, 0.2)', 'rgba(6, 182, 212, 0.2)', 'rgb(107, 114, 128, 0.2)', 'rgba(139, 92, 246, 0.2)'],
          borderColor: ['rgb(249, 115, 22)', 'rgb(6, 182, 212)', 'rgb(107, 114, 128)', 'rgb(139, 92, 246)'],
          borderWidth: 1
        }
      ]
    };

    let maxValue = Math.max(...workedHours) || 0;

    if (maxValue < 10) {
      maxValue = 12;
    } else {
      maxValue += 2;
    }

    let that = this;

    this.basicOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder
          }
        },
        y: {
          beginAtZero: true,
          suggestedMax: maxValue,
          ticks: {
            color: textColorSecondary,
            callback: function (value: number, index: number, ticks: any) {
              return that.durationFormatPipe.transform(value, true);
            }
          },
          grid: {
            color: surfaceBorder
          }
        }
      }
    };
  }

  calculateHoursForPeriod(startDate: Date, endDate: Date): string[] {
    return this.timeLogs
      .filter(log => {
        if (!log.logDate) {
          return false;
        }

        const logDate = new Date(log.logDate);

        return logDate >= startDate && logDate <= endDate;
      })
      .map(log => log.loggedTime!);
  }

  calculateInterval(increment: -1 | 1) {
    const startDate = this._selectedDateRange[0];

    if (this._intervalType === IntervalType.Week) {
      const weekStart = new Date(startDate);
      weekStart.setDate(startDate.getDate() + 7 * increment);
      const weekEnd = new Date(weekStart);
      weekEnd.setDate(weekStart.getDate() + 6);
      this._selectedDateRange = [weekStart, weekEnd];
    } else if (this._intervalType === IntervalType.Month) {
      const monthStart = new Date(startDate.getFullYear(), startDate.getMonth() + 1 * increment, 1);
      const monthEnd = new Date(startDate.getFullYear(), startDate.getMonth() + 1 * increment + 1, 1);
      monthEnd.setDate(0);
      this._selectedDateRange = [monthStart, monthEnd];
    }

    this.initChart();
  }
}
