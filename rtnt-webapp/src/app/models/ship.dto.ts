import { InventoryDto } from './inventory.dto'

export interface JourneyDto {
  id: string
  startIslandId: string
  startIslandName: string | null
  targetIslandId: string
  targetIslandName: string | null
  departedTick: number
  arrivedTick: number | null
  estimatedArrivalTick: number
  active: boolean
}

export interface ShipDto {
  id: string
  name: string
  islandId: string | null
  islandName: string | null
  playerId: string | null
  speed: number
  cargoCapacity: number
  inventory: InventoryDto
  journey: JourneyDto | null
}
