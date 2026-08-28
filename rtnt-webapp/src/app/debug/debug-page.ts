import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { MatButtonModule } from '@angular/material/button'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { catchError, EMPTY, interval, Observable, startWith, switchMap } from 'rxjs'
import { ClockService } from '../domain/clock/clock.service'
import { IslandService } from '../domain/island/island.service'
import { ClockDto } from '../models/clock.dto'

@Component({
  selector: 'app-debug-page',
  imports: [ElderSinglePaneWrapperComponent, MatButtonModule],
  templateUrl: './debug-page.html',
  styleUrl: './debug-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DebugPage {
  private readonly islandService = inject(IslandService)
  private readonly clockService = inject(ClockService)
  private readonly destroyRef = inject(DestroyRef)

  public busy = signal(false)
  public status = signal<string | null>(null)
  public clock = signal<ClockDto | null>(null)
  public advanceTicks = signal(10)

  constructor() {
    interval(1000)
      .pipe(
        startWith(0),
        switchMap(() => this.clockService.getClock().pipe(catchError(() => EMPTY))),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((clock) => this.clock.set(clock))
  }

  public recreateIslands(): void {
    this.runAction(
      this.islandService.recreateIslands(),
      'Islands recreated.',
      'Failed to recreate islands.'
    )
  }

  public pauseClock(): void {
    this.runClockAction(this.clockService.pause(), 'Clock paused.')
  }

  public resumeClock(): void {
    this.runClockAction(this.clockService.resume(), 'Clock resumed.')
  }

  public setMode(mode: ClockDto['mode']): void {
    this.runClockAction(this.clockService.setMode(mode), `Clock mode set to ${mode}.`)
  }

  public advanceClock(): void {
    const ticks = this.advanceTicks()
    this.runClockAction(this.clockService.advance(ticks), `Clock advanced by ${ticks} ticks.`)
  }

  public onAdvanceTicksInput(event: Event): void {
    const value = Number((event.target as HTMLInputElement).value)
    this.advanceTicks.set(Number.isFinite(value) ? value : 1)
  }

  private runClockAction(
    request: ReturnType<ClockService['pause']>,
    successMessage: string
  ): void {
    this.runAction(request, successMessage, 'Clock action failed.', (clock) => this.clock.set(clock))
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
