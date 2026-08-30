import { CommonModule } from '@angular/common'
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { catchError, EMPTY, interval, startWith, switchMap } from 'rxjs'
import { GameLoopDto } from '../../../models/game-loop.dto'
import { IslandDto } from '../../../models/island.dto'
import { GameLoopService } from '../../game-loop/game-loop.service'
import { IslandService } from '../island.service'

const MAP_MIN_PADDING = 40
const MAP_PADDING_RATIO = 0.08
const MAP_FALLBACK_SIZE = 600

@Component({
  selector: 'app-island-map',
  imports: [CommonModule, ElderSinglePaneWrapperComponent],
  templateUrl: './island-map.html',
  styleUrl: './island-map.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IslandMap {
  private readonly islandService = inject(IslandService)
  private readonly gameLoopService = inject(GameLoopService)
  private readonly destroyRef = inject(DestroyRef)

  public islands = signal<IslandDto[]>([])
  public gameLoop = signal<GameLoopDto | null>(null)

  public mapBoundsString = computed(() => {
    const islands = this.islands()
    if (islands.length === 0) {
      return `0 0 ${MAP_FALLBACK_SIZE} ${MAP_FALLBACK_SIZE}`
    }

    let minX = Number.POSITIVE_INFINITY
    let minY = Number.POSITIVE_INFINITY
    let maxX = Number.NEGATIVE_INFINITY
    let maxY = Number.NEGATIVE_INFINITY

    for (const island of islands) {
      minX = Math.min(minX, island.x)
      minY = Math.min(minY, island.y)
      maxX = Math.max(maxX, island.x + island.width)
      maxY = Math.max(maxY, island.y + island.length)
    }

    const spanX = maxX - minX
    const spanY = maxY - minY
    const padding = Math.max(MAP_MIN_PADDING, Math.max(spanX, spanY) * MAP_PADDING_RATIO)
    return `${minX - padding} ${minY - padding} ${spanX + padding * 2} ${spanY + padding * 2}`
  })

  constructor() {
    this.islandService.listIslands().subscribe((islands) => this.islands.set(islands))
    interval(1000)
      .pipe(
        startWith(0),
        switchMap(() => this.gameLoopService.get().pipe(catchError(() => EMPTY))),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((gameLoop) => this.gameLoop.set(gameLoop))
  }
}
