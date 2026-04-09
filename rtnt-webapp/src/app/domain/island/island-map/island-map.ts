import { CommonModule } from '@angular/common'
import { HttpClient } from '@angular/common/http'
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  model,
  signal,
  untracked,
  ViewChild,
} from '@angular/core'
import { MatButtonModule } from '@angular/material/button'
import { MatIconModule } from '@angular/material/icon'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { interval } from 'rxjs'
import { environment } from '../../../../environments/environment'
import { IslandDto } from '../../../models/island.dto'
import { ShipDto } from '../../../models/ship.dto'
import { IslandService } from '../island.service'
import { ShipService } from '../../ship/ship.service'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { MatSlideToggle } from '@angular/material/slide-toggle'
import { FormsModule } from '@angular/forms'
import { PlayerService } from '../../player/player.service'
import { MatMenuModule, MatMenuTrigger } from '@angular/material/menu'
import { Router } from '@angular/router'

const MAP_MIN_PADDING = 40
const MAP_PADDING_RATIO = 0.08
const MAP_FALLBACK_SIZE = 600

interface MapBounds {
  minX: number
  minY: number
  width: number
  height: number
}

interface ShipMarker {
  id: string
  name: string
  x: number
  y: number
  color: string | null
}

@Component({
  selector: 'app-island-map',
  imports: [
    CommonModule,
    ElderSinglePaneWrapperComponent,
    MatButtonModule,
    MatIconModule,
    MatSlideToggle,
    FormsModule,
    MatMenuModule,
  ],
  templateUrl: './island-map.html',
  styleUrl: './island-map.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IslandMap {
  private readonly islandService = inject(IslandService)
  private readonly shipService = inject(ShipService)
  private readonly playerService = inject(PlayerService)
  private readonly router = inject(Router)
  public readonly isAutoRefreshing = model<boolean>(true)
  @ViewChild(MatMenuTrigger) private islandMenuTrigger?: MatMenuTrigger

  public islands = signal<IslandDto[]>([])
  public shipMarkers = signal<ShipMarker[]>([])
  public selectedIsland = signal<IslandDto | null>(null)
  public isStartingJourney = signal(false)
  public selectedShipId = signal<string | null>(null)
  public islandActionMode = signal<'journey' | 'visit' | null>(null)
  public journeyMessage = signal<string | null>(null)
  public menuPosition = signal({ x: 0, y: 0 })
  public mapBounds = signal<MapBounds>({
    minX: 0,
    minY: 0,
    width: MAP_FALLBACK_SIZE,
    height: MAP_FALLBACK_SIZE,
  })

  public mapBoundsString = computed(() => {
    return `${this.mapBounds().minX} ${this.mapBounds().minY} ${this.mapBounds().width} ${this.mapBounds().height}`
  })

  constructor() {
    effect(() => {
      const islands = this.islands()
      untracked(() => {
        this.mapBounds.set(this.getMapBounds())
      })
    })

    this.fetchIslands()
    this.refresh()

    interval(1000)
      .pipe(takeUntilDestroyed())
      .subscribe(() => {
        if (this.isAutoRefreshing()) {
          this.refresh()
        }
      })
  }

  private fetchIslands(): void {
    this.islandService.listIslands().subscribe((islands) => {
      this.islands.set(islands)
    })
  }

  private fetchShips(): void {
    this.shipService.listShips().subscribe((ships) => {
      this.shipMarkers.set(this.calculateShipMarkers(this.islands(), ships))
    })
  }

  public refresh(): void {
    this.fetchShips()
  }

  public canPlayerJourney(): boolean {
    return !!this.playerService.getSelectedPlayerId()
  }

  public onIslandClick(event: MouseEvent, island: IslandDto): void {
    if (!this.canPlayerJourney()) {
      return
    }
    const selectedPlayerId = this.playerService.getSelectedPlayerId()
    if (!selectedPlayerId) {
      return
    }

    event.stopPropagation()
    this.isStartingJourney.set(true)
    this.selectedIsland.set(island)
    this.selectedShipId.set(null)
    this.islandActionMode.set(null)
    this.journeyMessage.set(null)
    this.menuPosition.set({
      x: event.clientX,
      y: event.clientY,
    })

    this.shipService.listShips({ playerId: selectedPlayerId }).subscribe({
      next: (ships) => {
        this.isStartingJourney.set(false)
        const availableShips = ships.filter((ship) => !ship.journey?.active && !!ship.islandId)
        if (availableShips.length === 0) {
          this.journeyMessage.set('No available player ship.')
          return
        }

        const shipAtIsland = availableShips.find((ship) => ship.islandId === island.id) ?? null
        const selectedShip = shipAtIsland ?? availableShips[0]
        this.selectedShipId.set(selectedShip.id)
        this.islandActionMode.set(shipAtIsland ? 'visit' : 'journey')
        this.islandMenuTrigger?.openMenu()
      },
      error: (error) => {
        this.isStartingJourney.set(false)
        this.journeyMessage.set(error?.error?.message ?? 'Could not load player ships.')
      },
    })
  }

  public performSelectedIslandAction(): void {
    if (this.islandActionMode() === 'visit') {
      const island = this.selectedIsland()
      if (!island) {
        return
      }
      this.router.navigate(['/port-trade']).then(() => {
        this.journeyMessage.set(`Visiting ${island.name}.`)
      })
      return
    }

    this.startJourneyToSelectedIsland()
  }

  private startJourneyToSelectedIsland(): void {
    if (this.isStartingJourney()) {
      return
    }

    const island = this.selectedIsland()
    const selectedShipId = this.selectedShipId()
    if (!island || !selectedShipId) {
      return
    }

    const selectedPlayerId = this.playerService.getSelectedPlayerId()
    if (!selectedPlayerId) {
      this.journeyMessage.set('Please select or create a player first.')
      return
    }

    this.isStartingJourney.set(true)
    this.shipService
      .startJourney(selectedShipId, {
        playerId: selectedPlayerId,
        targetIslandId: island.id,
      })
      .subscribe({
        next: () => {
          this.isStartingJourney.set(false)
          this.journeyMessage.set(`Journey started to ${island.name}.`)
          this.refresh()
        },
        error: (error) => {
          this.isStartingJourney.set(false)
          this.journeyMessage.set(error?.error?.message ?? 'Could not start journey.')
        },
      })
  }

  private calculateShipMarkers(islands: IslandDto[], ships: ShipDto[]): ShipMarker[] {
    const islandsById = new Map<string, IslandDto>(islands.map((island) => [island.id, island]))
    const now = Date.now()

    return ships
      .map((ship) => {
        const dockedIsland = ship.islandId ? islandsById.get(ship.islandId) : undefined
        if (dockedIsland) {
          const center = this.getIslandCenter(dockedIsland)
          return {
            id: ship.id,
            name: ship.name,
            x: center.x,
            y: center.y,
            color: ship.playerHexColor,
          } satisfies ShipMarker
        }

        if (!ship.journey) {
          return null
        }

        const start = islandsById.get(ship.journey.startIslandId)
        const target = islandsById.get(ship.journey.targetIslandId)
        if (!start || !target) {
          return null
        }

        const startCenter = this.getIslandCenter(start)
        const targetCenter = this.getIslandCenter(target)
        const departedMs = ship.journey.departed ? Date.parse(ship.journey.departed) : Number.NaN
        const etaMs = ship.journey.estimatedArrival
          ? Date.parse(ship.journey.estimatedArrival)
          : Number.NaN

        let progress = 0.5
        if (Number.isFinite(departedMs) && Number.isFinite(etaMs) && etaMs > departedMs) {
          progress = Math.min(1, Math.max(0, (now - departedMs) / (etaMs - departedMs)))
        }

        return {
          id: ship.id,
          name: ship.name,
          x: startCenter.x + (targetCenter.x - startCenter.x) * progress,
          y: startCenter.y + (targetCenter.y - startCenter.y) * progress,
          color: ship.playerHexColor,
        } satisfies ShipMarker
      })
      .filter((marker): marker is ShipMarker => marker !== null)
  }

  private getIslandCenter(island: IslandDto): { x: number; y: number } {
    return {
      x: island.footprint.x + island.footprint.width / 2,
      y: island.footprint.y + island.footprint.length / 2,
    }
  }

  public getMapBounds(): MapBounds {
    const islands = this.islands()
    if (islands.length === 0) {
      return {
        minX: 0,
        minY: 0,
        width: MAP_FALLBACK_SIZE,
        height: MAP_FALLBACK_SIZE,
      }
    }

    let minX = Number.POSITIVE_INFINITY
    let minY = Number.POSITIVE_INFINITY
    let maxX = Number.NEGATIVE_INFINITY
    let maxY = Number.NEGATIVE_INFINITY

    for (const island of islands) {
      minX = Math.min(minX, island.footprint.x)
      minY = Math.min(minY, island.footprint.y)
      maxX = Math.max(maxX, island.footprint.x + island.footprint.width)
      maxY = Math.max(maxY, island.footprint.y + island.footprint.length)
    }

    const spanX = maxX - minX
    const spanY = maxY - minY
    const dynamicPadding = Math.max(MAP_MIN_PADDING, Math.max(spanX, spanY) * MAP_PADDING_RATIO)

    return {
      minX: minX - dynamicPadding,
      minY: minY - dynamicPadding,
      width: spanX + dynamicPadding * 2,
      height: spanY + dynamicPadding * 2,
    }
  }
}
