import type { Routes } from '@angular/router';
import { NativeShellComponent } from './native-shell.component.ts';
import { DashboardScreenComponent } from './screens/dashboard-screen.component.ts';
import { HomeScreenComponent } from './screens/home-screen.component.ts';

export const appRoutes: Routes = [
  {
    path: '',
    component: NativeShellComponent,
    children: [
      { path: '', component: HomeScreenComponent },
      { path: 'dashboard', component: DashboardScreenComponent },
      { path: '**', redirectTo: '' },
    ],
  },
];
