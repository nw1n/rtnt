import { InventoryDto } from './inventory.dto'
import { TradePricesDto } from './island.dto'

export interface IslandPopulationDto {
  id: string
  name: string
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
  islands: IslandPopulationDto[]
  ships?: SnapshotShipDto[]
}
