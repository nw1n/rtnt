import { Routes } from '@angular/router'
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
]
