import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core'
import { MatButtonModule } from '@angular/material/button'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import type { EChartsOption } from 'echarts'
import { WorldSnapshotDto } from '../../models/world-snapshot.dto'
import { EchartsDirective } from './echarts.directive'
import { WorldSnapshotService } from './world-snapshot.service'

@Component({
  selector: 'app-history-page',
  imports: [ElderSinglePaneWrapperComponent, MatButtonModule, EchartsDirective],
  templateUrl: './history-page.html',
  styleUrl: './history-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoryPage {
  private readonly worldSnapshotService = inject(WorldSnapshotService)

  public snapshots = signal<WorldSnapshotDto[]>([])
  public busy = signal(false)
  public error = signal<string | null>(null)
  public darkTheme = signal(document.body.classList.contains('elder-dark-theme'))

  public chartOption = computed<EChartsOption>(() => this.buildChartOption(this.snapshots()))

  constructor() {
    this.refresh()
  }

  public refresh(): void {
    if (this.busy()) {
      return
    }
    this.busy.set(true)
    this.error.set(null)
    this.darkTheme.set(document.body.classList.contains('elder-dark-theme'))
    this.worldSnapshotService.listSnapshots().subscribe({
      next: (snapshots) => {
        this.snapshots.set(snapshots)
        this.busy.set(false)
      },
      error: () => {
        this.busy.set(false)
        this.error.set('Failed to load history.')
      },
    })
  }

  private buildChartOption(snapshots: WorldSnapshotDto[]): EChartsOption {
    const seriesByIsland = new Map<string, { name: string; data: [number, number][] }>()
    for (const snapshot of snapshots) {
      for (const island of snapshot.islands) {
        const series = seriesByIsland.get(island.id) ?? { name: island.name, data: [] }
        series.data.push([snapshot.tick, island.population])
        seriesByIsland.set(island.id, series)
      }
    }

    const nameCounts = new Map<string, number>()
    for (const islandSeries of seriesByIsland.values()) {
      nameCounts.set(islandSeries.name, (nameCounts.get(islandSeries.name) ?? 0) + 1)
    }

    const series = [...seriesByIsland.entries()]
      .sort((left, right) => left[1].name.localeCompare(right[1].name))
      .map(([islandId, islandSeries]) => ({
        name:
          (nameCounts.get(islandSeries.name) ?? 0) > 1
            ? `${islandSeries.name} (${islandId.slice(0, 6)})`
            : islandSeries.name,
        type: 'line' as const,
        showSymbol: islandSeries.data.length < 24,
        data: islandSeries.data,
      }))

    return {
      animationDuration: 300,
      tooltip: {
        trigger: 'axis',
      },
      legend: {
        type: 'scroll',
        bottom: 0,
      },
      grid: {
        left: 56,
        right: 24,
        top: 32,
        bottom: 88,
      },
      dataZoom: [
        { type: 'inside', xAxisIndex: 0 },
        { type: 'slider', xAxisIndex: 0, height: 18, bottom: 40 },
      ],
      xAxis: {
        type: 'value',
        name: 'Tick',
        minInterval: 1,
      },
      yAxis: {
        type: 'value',
        name: 'Population',
        min: 0,
        minInterval: 1,
      },
      series,
    }
  }
}
