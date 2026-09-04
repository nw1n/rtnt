export interface ShipDto {
  id: string
  name: string
  islandId: string | null
  islandName: string | null
  playerId: string | null
  speed: number
  cargoCapacity: number
}
