import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { MatButtonModule } from '@angular/material/button'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { catchError, EMPTY, interval, Observable, startWith, switchMap } from 'rxjs'
import { GameFlowService } from '../domain/game-flow/game-flow.service'
import { IslandService } from '../domain/island/island.service'
import { GameFlowDto } from '../models/game-flow.dto'

@Component({
  selector: 'app-debug-page',
  imports: [ElderSinglePaneWrapperComponent, MatButtonModule],
  templateUrl: './debug-page.html',
  styleUrl: './debug-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DebugPage {
  private readonly islandService = inject(IslandService)
  private readonly gameFlowService = inject(GameFlowService)
  private readonly destroyRef = inject(DestroyRef)

  public busy = signal(false)
  public status = signal<string | null>(null)
  public gameFlow = signal<GameFlowDto | null>(null)
  public advanceTicks = signal(10)

  constructor() {
    interval(1000)
      .pipe(
        startWith(0),
        switchMap(() => this.gameFlowService.get().pipe(catchError(() => EMPTY))),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((gameFlow) => this.gameFlow.set(gameFlow))
  }

  public recreateIslands(): void {
    this.runAction(
      this.islandService.recreateIslands(),
      'Islands recreated.',
      'Failed to recreate islands.'
    )
  }

  public pause(): void {
    this.runGameFlowAction(this.gameFlowService.pause(), 'Game flow paused.')
  }

  public resume(): void {
    this.runGameFlowAction(this.gameFlowService.resume(), 'Game flow resumed.')
  }

  public setMode(mode: GameFlowDto['mode']): void {
    this.runGameFlowAction(this.gameFlowService.setMode(mode), `Time mode set to ${mode}.`)
  }

  public advance(): void {
    const ticks = this.advanceTicks()
    this.runGameFlowAction(this.gameFlowService.advance(ticks), `Game flow advanced by ${ticks} ticks.`)
  }

  public onAdvanceTicksInput(event: Event): void {
    const value = Number((event.target as HTMLInputElement).value)
    this.advanceTicks.set(Number.isFinite(value) ? value : 1)
  }

  private runGameFlowAction(
    request: ReturnType<GameFlowService['pause']>,
    successMessage: string
  ): void {
    this.runAction(request, successMessage, 'Game flow action failed.', (gameFlow) => this.gameFlow.set(gameFlow))
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
