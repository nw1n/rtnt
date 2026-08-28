import { CommonModule } from '@angular/common'
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  signal,
  untracked,
  viewChild,
} from '@angular/core'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { catchError, EMPTY, interval, startWith, switchMap } from 'rxjs'
import { ClockDto } from '../../../models/clock.dto'
import { IslandDto } from '../../../models/island.dto'
import { ClockService } from '../../clock/clock.service'
import { IslandService } from '../island.service'
import { IslandMapPixi } from './island-map-pixi'

@Component({
  selector: 'app-island-map',
  imports: [CommonModule, ElderSinglePaneWrapperComponent],
  templateUrl: './island-map.html',
  styleUrl: './island-map.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IslandMap {
  private readonly islandService = inject(IslandService)
  private readonly clockService = inject(ClockService)
  private readonly destroyRef = inject(DestroyRef)
  private readonly mapHost = viewChild<ElementRef<HTMLDivElement>>('mapHost')
  private readonly pixi = new IslandMapPixi()
  private pixiReady: Promise<void> | null = null

  public islands = signal<IslandDto[]>([])
  public clock = signal<ClockDto | null>(null)

  constructor() {
    this.islandService.listIslands().subscribe((islands) => this.islands.set(islands))
    interval(1000)
      .pipe(
        startWith(0),
        switchMap(() => this.clockService.getClock().pipe(catchError(() => EMPTY))),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((clock) => this.clock.set(clock))

    this.destroyRef.onDestroy(() => this.pixi.destroy())

    effect(() => {
      const host = this.mapHost()?.nativeElement
      const islands = this.islands()
      if (!host || islands.length === 0) {
        return
      }
      untracked(() => {
        void this.syncPixi(host, islands)
      })
    })
  }

  private async syncPixi(host: HTMLElement, islands: IslandDto[]): Promise<void> {
    this.pixiReady ??= this.pixi.attach(host)
    await this.pixiReady
    this.pixi.render(islands)
  }
}
