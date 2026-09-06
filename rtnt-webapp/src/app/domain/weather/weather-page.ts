import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import type { EChartsOption } from 'echarts'
import { catchError, EMPTY, interval, startWith, switchMap } from 'rxjs'
import { WeatherDto } from '../../models/weather.dto'
import { WeatherSampleDto } from '../../models/weather-sample.dto'
import { EchartsDirective } from './echarts.directive'
import { WeatherService } from './weather.service'

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
  public error = signal<string | null>(null)
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
          this.weatherService.history().pipe(
            catchError(() => {
              this.error.set('Failed to load weather history.')
              return EMPTY
            })
          )
        ),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((samples) => {
        this.samples.set(samples)
        this.error.set(null)
        this.darkTheme.set(document.body.classList.contains('elder-dark-theme'))
      })
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
        min: 0,
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
