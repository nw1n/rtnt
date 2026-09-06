import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import type { EChartsOption } from 'echarts'
import { catchError, EMPTY, forkJoin, interval, startWith, switchMap } from 'rxjs'
import { WeatherAnalysisDto } from '../../models/weather-analysis.dto'
import { WeatherDto } from '../../models/weather.dto'
import { WeatherSampleDto } from '../../models/weather-sample.dto'
import { EchartsDirective } from './echarts.directive'
import { WeatherRange, WeatherService } from './weather.service'

@Component({
  selector: 'app-weather-page',
  imports: [ElderSinglePaneWrapperComponent, EchartsDirective],
  templateUrl: './weather-page.html',
  styleUrl: './weather-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WeatherPage {
  private readonly weatherService = inject(WeatherService)
  private readonly destroyRef = inject(DestroyRef)

  public weather = signal<WeatherDto | null>(null)
  public samples = signal<WeatherSampleDto[]>([])
  public analysis = signal<WeatherAnalysisDto | null>(null)
  public error = signal<string | null>(null)
  public fromTickInput = signal('')
  public toTickInput = signal('')
  public darkTheme = signal(document.body.classList.contains('elder-dark-theme'))
  public chartOption = computed<EChartsOption>(() => this.buildChart(this.samples()))

  constructor() {
    interval(1000)
      .pipe(
        startWith(0),
        switchMap(() => this.weatherService.get().pipe(catchError(() => EMPTY))),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((weather) => this.weather.set(weather))
    interval(1000)
      .pipe(
        startWith(0),
        switchMap(() =>
          forkJoin({
            samples: this.weatherService.history(this.range()),
            analysis: this.weatherService.analysis(this.range()),
          }).pipe(
            catchError(() => {
              this.error.set('Failed to load weather history.')
              return EMPTY
            })
          )
        ),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(({ samples, analysis }) => {
        this.samples.set(samples)
        this.analysis.set(analysis)
        this.error.set(null)
        this.darkTheme.set(document.body.classList.contains('elder-dark-theme'))
      })
  }

  public onFromTickInput(event: Event): void {
    this.fromTickInput.set((event.target as HTMLInputElement).value)
  }

  public onToTickInput(event: Event): void {
    this.toTickInput.set((event.target as HTMLInputElement).value)
  }

  public formatAverage(value: number): string {
    return value.toFixed(1)
  }

  private range(): WeatherRange {
    return {
      fromTick: this.parseTick(this.fromTickInput()),
      toTick: this.parseTick(this.toTickInput()),
    }
  }

  private parseTick(value: string): number | null {
    const trimmed = value.trim()
    if (trimmed === '') {
      return null
    }
    const parsed = Number(trimmed)
    return Number.isFinite(parsed) ? Math.trunc(parsed) : null
  }

  private buildChart(samples: WeatherSampleDto[]): EChartsOption {
    return {
      animation: false,
      grid: { left: 48, right: 24, top: 32, bottom: 48 },
      tooltip: {
        trigger: 'axis',
        valueFormatter: (value) => `${value}°`,
      },
      xAxis: {
        type: 'value',
        name: 'Tick',
        min: samples[0]?.tick ?? 0,
        minInterval: 1,
      },
      yAxis: {
        type: 'value',
        name: 'Temperature',
        axisLabel: { formatter: '{value}°' },
      },
      series: [
        {
          name: 'Temperature',
          type: 'line',
          step: 'end',
          showSymbol: samples.length < 24,
          data: samples.map((sample) => [sample.tick, sample.temperature]),
        },
      ],
    }
  }
}
