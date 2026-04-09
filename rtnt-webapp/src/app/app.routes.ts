import { Routes } from '@angular/router'
import { Debug } from './domain/debug/debug/debug'
import { IslandBrowser } from './domain/island/island-browser/island-browser'
import { IslandInventoryBrowser } from './domain/island/island-inventory-browser/island-inventory-browser'
import { IslandMap } from './domain/island/island-map/island-map'
import { IslandPricesBrowser } from './domain/island/island-prices-browser/island-prices-browser'
import { JourneyBrowser } from './domain/journey/journey-browser/journey-browser'
import { Player } from './domain/player/player/player'
import { ShipBrowser } from './domain/ship/ship-browser/ship-browser'
import { PortTrade } from './domain/ship/port-trade/port-trade'

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'island-browser',
    pathMatch: 'full',
  },
  {
    path: 'island-browser',
    component: IslandBrowser,
    data: {
      title: 'Island Browser',
    },
  },
  {
    path: 'ship-browser',
    component: ShipBrowser,
    data: {
      title: 'Ship Browser',
    },
  },
  {
    path: 'port-trade',
    component: PortTrade,
    data: {
      title: 'Port trade',
    },
  },
  {
    path: 'journey-browser',
    component: JourneyBrowser,
    data: {
      title: 'Journey Browser',
    },
  },
  {
    path: 'island-inventory-browser',
    component: IslandInventoryBrowser,
    data: {
      title: 'Island Inventory Browser',
    },
  },
  {
    path: 'island-prices-browser',
    component: IslandPricesBrowser,
    data: {
      title: 'Island Prices Browser',
    },
  },
  {
    path: 'island-map',
    component: IslandMap,
    data: {
      title: 'Island Map',
    },
  },
  {
    path: 'player',
    component: Player,
    data: {
      title: 'Player',
    },
  },
  {
    path: 'debug',
    component: Debug,
    data: {
      title: 'Debug',
    },
  },
]
