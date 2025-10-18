import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { TasksComponent } from './tasks/tasks.component';
import { TimeTrackingComponent } from './time-tracking/time-tracking.component';
import { BoardComponent } from './board/board.component';
import { ReportsComponent } from './reports/reports.component';
import { canActivateAuthRole } from './guards/auth.guard';
import { NotFoundComponent } from './not-found/not-found.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [canActivateAuthRole]
  },
  {
    path: 'time-tracking',
    component: TimeTrackingComponent,
    canActivate: [canActivateAuthRole]
  },
  {
    path: 'tasks',
    component: TasksComponent,
    canActivate: [canActivateAuthRole]
  },
  {
    path: 'board',
    component: BoardComponent,
    canActivate: [canActivateAuthRole]
  },
  {
    path: 'reports',
    component: ReportsComponent,
    canActivate: [canActivateAuthRole]
  },
  {
    path: 'not-found',
    component: NotFoundComponent,
  },
  { path: '**', redirectTo: 'not-found' }
];
