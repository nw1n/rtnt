import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { MatButtonModule } from '@angular/material/button'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { catchError, EMPTY, interval, Observable, startWith, switchMap } from 'rxjs'
import { GameLoopService } from '../domain/game-loop/game-loop.service'
import { IslandService } from '../domain/island/island.service'
import { GameLoopDto } from '../models/game-loop.dto'

@Component({
  selector: 'app-debug-page',
  imports: [ElderSinglePaneWrapperComponent, MatButtonModule],
  templateUrl: './debug-page.html',
  styleUrl: './debug-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DebugPage {
  private readonly islandService = inject(IslandService)
  private readonly gameLoopService = inject(GameLoopService)
  private readonly destroyRef = inject(DestroyRef)

  public busy = signal(false)
  public status = signal<string | null>(null)
  public gameLoop = signal<GameLoopDto | null>(null)
  public advanceTicks = signal(10)

  constructor() {
    interval(1000)
      .pipe(
        startWith(0),
        switchMap(() => this.gameLoopService.get().pipe(catchError(() => EMPTY))),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((gameLoop) => this.gameLoop.set(gameLoop))
  }

  public recreateIslands(): void {
    this.runAction(
      this.islandService.recreateIslands(),
      'Islands recreated.',
      'Failed to recreate islands.'
    )
  }

  public pause(): void {
    this.runGameLoopAction(this.gameLoopService.pause(), 'Game loop paused.')
  }

  public resume(): void {
    this.runGameLoopAction(this.gameLoopService.resume(), 'Game loop resumed.')
  }

  public setMode(mode: GameLoopDto['mode']): void {
    this.runGameLoopAction(this.gameLoopService.setMode(mode), `Game loop mode set to ${mode}.`)
  }

  public advance(): void {
    const ticks = this.advanceTicks()
    this.runGameLoopAction(this.gameLoopService.advance(ticks), `Game loop advanced by ${ticks} ticks.`)
  }

  public onAdvanceTicksInput(event: Event): void {
    const value = Number((event.target as HTMLInputElement).value)
    this.advanceTicks.set(Number.isFinite(value) ? value : 1)
  }

  private runGameLoopAction(
    request: ReturnType<GameLoopService['pause']>,
    successMessage: string
  ): void {
    this.runAction(request, successMessage, 'Game loop action failed.', (gameLoop) => this.gameLoop.set(gameLoop))
  }

  private runAction<T>(
    request: Observable<T>,
    successMessage: string,
    errorMessage: string,
    onSuccess?: (value: T) => void
  ): void {
    if (this.busy()) {
      return
    }
    this.busy.set(true)
    this.status.set(null)
    request.subscribe({
      next: (value) => {
        onSuccess?.(value)
        this.busy.set(false)
        this.status.set(successMessage)
      },
      error: () => {
        this.busy.set(false)
        this.status.set(errorMessage)
      },
    })
  }
}
