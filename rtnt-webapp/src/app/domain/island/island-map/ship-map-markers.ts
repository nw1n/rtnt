import { IslandDto } from '../../../models/island.dto'
import { JourneyDto, ShipDto } from '../../../models/ship.dto'

export interface ShipMapMarker {
  id: string
  name: string
  x: number
  y: number
  headingDeg: number
  underway: boolean
  playerOwned: boolean
}

export function shipMapMarkers(ships: ShipDto[], islands: IslandDto[], tick: number): ShipMapMarker[] {
  const islandById = new Map(islands.map((island) => [island.id, island]))
  const dockedByIsland = new Map<string, ShipDto[]>()
  const markers: ShipMapMarker[] = []

  for (const ship of ships) {
    if (ship.journey?.active) {
      const underway = underwayMarker(ship, ship.journey, islandById, tick)
      if (underway) {
        markers.push(underway)
      }
      continue
    }
    if (!ship.islandId) {
      continue
    }
    const group = dockedByIsland.get(ship.islandId) ?? []
    group.push(ship)
    dockedByIsland.set(ship.islandId, group)
  }

  for (const [islandId, group] of dockedByIsland) {
    const island = islandById.get(islandId)
    if (!island) {
      continue
    }
    const center = islandCenter(island)
    const count = group.length
    const radius = Math.max(14, Math.min(island.width, island.length) * 0.28)
    group.forEach((ship, index) => {
      const angle = (2 * Math.PI * index) / count + Math.PI / 2
      markers.push({
        id: ship.id,
        name: ship.name,
        x: center.x + Math.cos(angle) * radius,
        y: center.y + Math.sin(angle) * radius,
        headingDeg: 0,
        underway: false,
        playerOwned: !!ship.playerId,
      })
    })
  }

  return markers
}

function underwayMarker(
  ship: ShipDto,
  journey: JourneyDto,
  islandById: Map<string, IslandDto>,
  tick: number
): ShipMapMarker | null {
  const start = islandById.get(journey.startIslandId)
  const target = islandById.get(journey.targetIslandId)
  if (!start || !target) {
    return null
  }
  const from = islandCenter(start)
  const to = islandCenter(target)
  const span = journey.estimatedArrivalTick - journey.departedTick
  const progress = span <= 0 ? 1 : clamp((tick - journey.departedTick) / span, 0, 1)
  return {
    id: ship.id,
    name: ship.name,
    x: from.x + (to.x - from.x) * progress,
    y: from.y + (to.y - from.y) * progress,
    headingDeg: (Math.atan2(to.y - from.y, to.x - from.x) * 180) / Math.PI,
    underway: true,
    playerOwned: !!ship.playerId,
  }
}

function islandCenter(island: IslandDto): { x: number; y: number } {
  return {
    x: island.x + island.width / 2,
    y: island.y + island.length / 2,
  }
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value))
}
