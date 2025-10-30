import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { TimeLogControllerApiService, TimeLogDto } from '@anna/flow-board-api';

@Component({
  selector: 'app-time-tracking',
  standalone: true,
  imports: [
    CommonModule,
    CardModule,
  ],
  templateUrl: './time-tracking.component.html'
})
export class TimeTrackingComponent implements OnInit, OnDestroy {
  timeLogs: TimeLogDto[] = [];
  loading = false;
  errorMessage = '';

  // Summary data
  todayHours = 0;
  weekHours = 0;
  monthHours = 0;
  yearHours = 0;

  // Chart data
  weeklyData: number[] = [0, 0, 0, 0, 0, 0, 0]; // Monday to Sunday
  chart: any;

  constructor(
    private timeLogService: TimeLogControllerApiService
  ) {}

  ngOnInit() {
    this.loadTimeLogs();
  }

  ngOnDestroy() {
    if (this.chart) {
      this.chart.destroy();
    }
  }

  loadTimeLogs() {
    this.loading = true;
    this.timeLogService.getAllTimeLogs().subscribe({
      next: (timeLogs) => {
        this.timeLogs = timeLogs;
        this.calculateSummaries();
        this.calculateWeeklyData();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading time logs:', error);
        this.errorMessage = 'Error loading time tracking data';
        this.loading = false;
      }
    });
  }

  calculateSummaries() {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const weekStart = new Date(today);
    weekStart.setDate(today.getDate() - today.getDay() + 1); // Monday
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
    const yearStart = new Date(now.getFullYear(), 0, 1);

    this.todayHours = this.calculateHoursForPeriod(today, new Date(today.getTime() + 24 * 60 * 60 * 1000));
    this.weekHours = this.calculateHoursForPeriod(weekStart, now);
    this.monthHours = this.calculateHoursForPeriod(monthStart, now);
    this.yearHours = this.calculateHoursForPeriod(yearStart, now);
  }

  calculateWeeklyData() {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    // Reset weekly data
    this.weeklyData = [0, 0, 0, 0, 0, 0, 0];

    // Calculate hours for each day of the current week
    for (let i = 0; i < 7; i++) {
      const dayStart = new Date(today);
      dayStart.setDate(today.getDate() - today.getDay() + 1 + i); // Monday + i days
      const dayEnd = new Date(dayStart);
      dayEnd.setDate(dayStart.getDate() + 1);

      this.weeklyData[i] = this.calculateHoursForPeriod(dayStart, dayEnd);
    }
  }

  calculateHoursForPeriod(startDate: Date, endDate: Date): number {
    return this.timeLogs
      .filter(log => {
        if (!log.logDate) return false;
        const logDate = new Date(log.logDate);
        return logDate >= startDate && logDate < endDate;
      })
      .reduce((total, log) => {
        if (!log.loggedTime) return total;

        // Parse ISO 8601 duration (PT2H30M)
        const match = log.loggedTime.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
        if (!match) return total;

        const hours = parseInt(match[1] || '0');
        const minutes = parseInt(match[2] || '0');

        return total + hours + (minutes / 60);
      }, 0);
  }

  formatHours(hours: number): string {
    const wholeHours = Math.floor(hours);
    const minutes = Math.round((hours - wholeHours) * 60);
    return `${wholeHours}:${minutes.toString().padStart(2, '0')}`;
  }

  getTodayDate(): string {
    const now = new Date();
    return `Today, ${now.getDate().toString().padStart(2, '0')}.${(now.getMonth() + 1).toString().padStart(2, '0')}.${now.getFullYear()}`;
  }

  getWeekNumber(): number {
    const now = new Date();
    const start = new Date(now.getFullYear(), 0, 1);
    const days = Math.floor((now.getTime() - start.getTime()) / (24 * 60 * 60 * 1000));
    return Math.ceil((days + start.getDay() + 1) / 7);
  }

  getMonthName(): string {
    const now = new Date();
    return now.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  }

  getCurrentYear(): number {
    return new Date().getFullYear();
  }

  getDayNames(): string[] {
    return ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
  }

  getMaxHours(): number {
    return Math.max(8, Math.ceil(Math.max(...this.weeklyData) / 2) * 2);
  }

  getBarHeight(hours: number): string {
    const maxHours = this.getMaxHours();
    const percentage = (hours / maxHours) * 100;
    return `${Math.max(percentage, 2)}%`; // Minimum 2% height for visibility
  }

  protected readonly Math = Math;
}
