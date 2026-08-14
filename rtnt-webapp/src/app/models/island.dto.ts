export interface IslandFootprintDto {
  x: number
  y: number
  width: number
  length: number
}

export interface IslandInventoryDto {
  gold: number
  rum: number
  sugar: number
  spices: number
  tobacco: number
}

export interface IslandTradePricesDto {
  rum: number
  sugar: number
  spices: number
  tobacco: number
}

export interface IslandDto {
  id: string
  name: string
  footprint: IslandFootprintDto
  inventory: IslandInventoryDto
  tradePrices: IslandTradePricesDto
}
