import { Pipe, PipeTransform } from '@angular/core';
import { TimeLogDto } from '@anna/flow-board-api';

@Pipe({
  name: 'billableCount',
  standalone: true
})
export class BillableCountPipe implements PipeTransform {
  transform(timeLogs: TimeLogDto[]): number {
    if (!timeLogs) return 0;
    return timeLogs.filter(log => log.billable).length;
  }
}

