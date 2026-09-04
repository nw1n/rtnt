import { InventoryDto } from './inventory.dto'
import { TradePricesDto } from './island.dto'

export interface IslandSnapshotDto {
  id: string
  name: string
}

export interface IslandStatusSnapshotDto {
  islandId: string
  population: number
  inventory: InventoryDto
  tradePrices: TradePricesDto
}

export interface SnapshotShipDto {
  id: string
  name: string
  inventory: InventoryDto
}

export interface WorldSnapshotDto {
  tick: number
  islands: IslandSnapshotDto[]
  islandStatuses?: IslandStatusSnapshotDto[]
  ships?: SnapshotShipDto[]
}
