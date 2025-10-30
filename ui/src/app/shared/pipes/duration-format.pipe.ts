import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'durationFormat',
  standalone: true
})
export class DurationFormatPipe implements PipeTransform {
  transform(duration: string | undefined): string {
    if (!duration) return '0h';
    
    // Parse ISO 8601 duration (PT2H30M)
    const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
    if (!match) return '0h';
    
    const hours = parseInt(match[1] || '0');
    const minutes = parseInt(match[2] || '0');
    
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

