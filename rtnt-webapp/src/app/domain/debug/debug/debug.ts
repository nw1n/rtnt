import { DatePipe } from '@angular/common'
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { MatCardModule } from '@angular/material/card'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { pollEverySecond } from '../../../utils/polling'
import { GameService } from '../../game/game.service'

@Component({
  selector: 'app-debug',
  imports: [DatePipe, MatCardModule, ElderSinglePaneWrapperComponent],
  templateUrl: './debug.html',
  styleUrl: './debug.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Debug {
  private readonly gameService = inject(GameService)

  public tickCount = signal(0)
  public ticksAsTime = computed(() => this.numberOfSecondsToHumanReadable(this.tickCount()))

  constructor() {
    pollEverySecond(() => this.gameService.getTicks())
      .pipe(takeUntilDestroyed())
      .subscribe((data) => {
        console.warn('data', data)
        this.tickCount.set(data.tickCount)
      })
  }

  private numberOfSecondsToHumanReadable(seconds: number): string {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const remainingSeconds = seconds % 60
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
  }
}
