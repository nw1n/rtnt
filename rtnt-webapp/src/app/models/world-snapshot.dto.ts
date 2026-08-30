export interface IslandPopulationDto {
  id: string
  name: string
  population: number
}

export interface WorldSnapshotDto {
  tick: number
  islands: IslandPopulationDto[]
}
