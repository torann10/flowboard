import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { TasksComponent } from './pages/tasks/tasks.component';
import { TimeTrackingComponent } from './pages/time-tracking/time-tracking.component';
import { BoardComponent } from './pages/board/board.component';
import {ReportsComponent} from './pages/reports/reports.component';

export const routes: Routes = [
  { path: 'dashboard', component: DashboardComponent },
  { path: 'time-tracking', component: TimeTrackingComponent },
  { path: 'tasks', component: TasksComponent },
  { path: 'board', component: BoardComponent },
  { path: 'reports', component: ReportsComponent },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];
