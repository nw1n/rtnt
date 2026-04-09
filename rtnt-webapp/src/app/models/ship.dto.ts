import { JourneyDto } from './journey.dto'

export interface ShipInventoryDto {
  gold: number
  rum: number
  sugar: number
  spices: number
  tobacco: number
}

export interface ShipDto {
  id: string
  name: string
  islandId: string | null
  journey: JourneyDto | null
  playerId: string | null
  playerHexColor: string | null
  inventory: ShipInventoryDto
}
