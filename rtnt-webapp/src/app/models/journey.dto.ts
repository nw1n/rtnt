export interface JourneyDto {
  id: string
  shipId: string
  startIslandId: string
  targetIslandId: string
  departed: string | null
  arrived: string | null
  estimatedArrival: string | null
  active: boolean
}
