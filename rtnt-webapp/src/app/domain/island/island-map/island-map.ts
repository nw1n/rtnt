import { CommonModule } from '@angular/common'
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  signal,
  untracked,
} from '@angular/core'
import { MatButtonModule } from '@angular/material/button'
import { MatIconModule } from '@angular/material/icon'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { IslandDto } from '../../../models/island.dto'
import { IslandService } from '../island.service'

const MAP_MIN_PADDING = 40
const MAP_PADDING_RATIO = 0.08
const MAP_FALLBACK_SIZE = 600

interface MapBounds {
  minX: number
  minY: number
  width: number
  height: number
}

@Component({
  selector: 'app-island-map',
  imports: [CommonModule, ElderSinglePaneWrapperComponent, MatButtonModule, MatIconModule],
  templateUrl: './island-map.html',
  styleUrl: './island-map.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IslandMap {
  private readonly islandService = inject(IslandService)

  public islands = signal<IslandDto[]>([])
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
      this.islands()
      untracked(() => {
        this.mapBounds.set(this.getMapBounds())
      })
    })
    this.refresh()
  }

  public refresh(): void {
    this.islandService.listIslands().subscribe((islands) => {
      this.islands.set(islands)
    })
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
