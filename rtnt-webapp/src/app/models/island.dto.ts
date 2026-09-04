import { InventoryDto } from './inventory.dto'

export interface IslandDto {
  id: string
  name: string
  x: number
  y: number
  width: number
  length: number
  population: number
  inventory: InventoryDto
}
