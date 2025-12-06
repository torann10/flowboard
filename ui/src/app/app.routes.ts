import { Routes } from '@angular/router';
import { TasksComponent } from './tasks/tasks.component';
import { TimeTrackingComponent } from './time-tracking/time-tracking.component';
import { ReportsComponent } from './reports/reports.component';
import { ProjectsComponent } from './projects/projects.component';
import { canActivateAuthRole } from './guards/auth.guard';
import { NotFoundComponent } from './not-found/not-found.component';

export const routes: Routes = [
  {
    path: '',
    component: TimeTrackingComponent,
    canActivate: [canActivateAuthRole],
    pathMatch: 'full',
  },
  {
    path: 'tasks',
    component: TasksComponent,
    canActivate: [canActivateAuthRole]
  },
  {
    path: 'reports',
    component: ReportsComponent,
    canActivate: [canActivateAuthRole]
  },
  {
    path: 'projects',
    component: ProjectsComponent,
    canActivate: [canActivateAuthRole]
  },
  {
    path: 'not-found',
    component: NotFoundComponent
  },
  { path: '**', redirectTo: 'not-found' }
];
