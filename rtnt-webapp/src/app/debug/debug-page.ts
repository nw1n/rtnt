import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { MatButtonModule } from '@angular/material/button'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { catchError, EMPTY, firstValueFrom, interval, Observable, startWith, switchMap, timer } from 'rxjs'
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
  public batchSize = signal(100)
  public batchCount = signal(1)
  public snapshotTick = signal(0)

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
    this.runGameFlowAction(this.gameFlowService.setMode(mode), `Flow mode set to ${mode}.`)
  }

  public async advance(): Promise<void> {
    if (this.busy()) {
      return
    }
    const batchSize = Math.max(1, Math.min(100_000, Math.trunc(this.batchSize()) || 1))
    const batches = Math.max(1, Math.trunc(this.batchCount()) || 1)
    this.busy.set(true)
    this.status.set(null)
    try {
      for (let i = 0; i < batches; i++) {
        const gameFlow = await firstValueFrom(this.gameFlowService.advance(batchSize))
        this.gameFlow.set(gameFlow)
        this.status.set(`Batch ${i + 1} of ${batches} · ${batchSize} ticks`)
        if (i < batches - 1) {
          await firstValueFrom(timer(150))
        }
      }
      this.status.set(`Advanced ${batches} batches of ${batchSize} ticks.`)
    } catch {
      this.status.set('Game flow action failed.')
    } finally {
      this.busy.set(false)
    }
  }

  public loadSnapshot(): void {
    const tick = this.snapshotTick()
    this.runGameFlowAction(
      this.gameFlowService.loadSnapshot(tick),
      `Loaded snapshot ${tick}.`,
      `Snapshot ${tick} not found.`
    )
  }

  public onBatchSizeInput(event: Event): void {
    const value = Number((event.target as HTMLInputElement).value)
    this.batchSize.set(Number.isFinite(value) ? value : 1)
  }

  public onBatchCountInput(event: Event): void {
    const value = Number((event.target as HTMLInputElement).value)
    this.batchCount.set(Number.isFinite(value) ? value : 1)
  }

  public onSnapshotTickInput(event: Event): void {
    const value = Number((event.target as HTMLInputElement).value)
    this.snapshotTick.set(Number.isFinite(value) ? value : 0)
  }

  private runGameFlowAction(
    request: ReturnType<GameFlowService['pause']>,
    successMessage: string,
    errorMessage = 'Game flow action failed.'
  ): void {
    this.runAction(request, successMessage, errorMessage, (gameFlow) => this.gameFlow.set(gameFlow))
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
