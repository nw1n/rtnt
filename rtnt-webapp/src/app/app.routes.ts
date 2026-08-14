import { Routes } from '@angular/router'
import { IslandBrowser } from './domain/island/island-browser/island-browser'
import { IslandMap } from './domain/island/island-map/island-map'

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
    path: 'island-browser',
    component: IslandBrowser,
    data: {
      title: 'Island Browser',
    },
  },
]
