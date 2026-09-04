import { InventoryDto } from './inventory.dto'

export interface TradePricesDto {
  rum: number
  sugar: number
  spices: number
  tobacco: number
}

export interface IslandDto {
  id: string
  name: string
  x: number
  y: number
  width: number
  length: number
  population: number
  inventory: InventoryDto
  tradePrices: TradePricesDto
}
