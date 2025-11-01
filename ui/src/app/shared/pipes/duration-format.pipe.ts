import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'durationFormat',
  standalone: true
})
export class DurationFormatPipe implements PipeTransform {
  transform(duration: string | string[] | number | undefined, format = true): string | number {
    if (!duration) return '0h';

    let hours = 0;
    let minutes = 0;

    if (typeof duration === 'number') {
      const result = this.handleNumber(duration);
      hours += result.hours;
      minutes += result.minutes;
    } else if (Array.isArray(duration)) {
      for (const dur of duration) {
        const result = this.parseDurationString(dur);

        hours += result.hours;
        minutes += result.minutes;
      }
    } else {
      const result = this.parseDurationString(duration);

      hours += result.hours;
      minutes += result.minutes;
    }

    if (!format) {
      return hours + minutes / 60;
    }

    if (hours > 0 && minutes > 0) {
      return `${hours}h ${minutes}m`;
    } else if (hours > 0) {
      return `${hours}h`;
    } else if (minutes > 0) {
      return `${minutes}m`;
    }

    return '0h';
  }

  private handleNumber(duration: number): { hours: number; minutes: number } {
    const hours = Math.floor(duration);
    const minutes = Math.round((duration - hours) * 60);
    return { hours, minutes };
  }

  private parseDurationString(dur: string): { hours: number; minutes: number } {
    const match = dur.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
    if (!match) {
      return { hours: 0, minutes: 0 };
    }

    const hours = parseInt(match[1] || '0');
    const minutes = parseInt(match[2] || '0');

    return { hours, minutes };
  }
}

