import { Routes } from '@angular/router'
import { DebugPage } from './debug/debug-page'
import { WeatherPage } from './domain/weather/weather-page'

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'weather',
    pathMatch: 'full',
  },
  {
    path: 'weather',
    component: WeatherPage,
    data: {
      title: 'Weather',
    },
  },
  {
    path: 'debug',
    component: DebugPage,
    data: {
      title: 'Game flow',
    },
  },
]
