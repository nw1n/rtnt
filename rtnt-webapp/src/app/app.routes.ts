import { Routes } from '@angular/router'
import { DebugPage } from './debug/debug-page'
import { HistoryPage } from './domain/history/history-page'
import { IslandMap } from './domain/island/island-map/island-map'
import { IslandTable } from './domain/island/island-table/island-table'

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'island-map',
    pathMatch: 'full',
  },
  {
    path: 'island-map',
    component: IslandMap,
    data: {
      title: 'Island Map',
    },
  },
  {
    path: 'islands',
    component: IslandTable,
    data: {
      title: 'Islands',
    },
  },
  {
    path: 'history',
    component: HistoryPage,
    data: {
      title: 'History',
    },
  },
  {
    path: 'debug',
    component: DebugPage,
    data: {
      title: 'Debug',
    },
  },
]
