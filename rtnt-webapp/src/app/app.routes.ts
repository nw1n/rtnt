import { Routes } from '@angular/router'
import { DebugPage } from './debug/debug-page'

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'debug',
    pathMatch: 'full',
  },
  {
    path: 'debug',
    component: DebugPage,
    data: {
      title: 'Game flow',
    },
  },
]
