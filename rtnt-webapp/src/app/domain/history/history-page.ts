import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core'
import { MatButtonModule } from '@angular/material/button'
import { MatTabsModule } from '@angular/material/tabs'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import type { EChartsOption } from 'echarts'
import { WorldSnapshotDto } from '../../models/world-snapshot.dto'
import { EchartsDirective } from './echarts.directive'
import { WorldSnapshotService } from './world-snapshot.service'

@Component({
  selector: 'app-history-page',
  imports: [ElderSinglePaneWrapperComponent, MatButtonModule, MatTabsModule, EchartsDirective],
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
  public selectedTab = signal(0)

  public chartOption = computed<EChartsOption>(() => this.buildPopulationChart(this.snapshots()))
  public shipGoldChartOption = computed<EChartsOption>(() => this.buildShipGoldChart(this.snapshots()))

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

  private buildPopulationChart(snapshots: WorldSnapshotDto[]): EChartsOption {
    return this.buildLineChart(
      snapshots,
      'Population',
      (snapshot) =>
        snapshot.islands.map((island) => ({
          id: island.id,
          name: island.name,
          value: island.population,
        }))
    )
  }

  private buildShipGoldChart(snapshots: WorldSnapshotDto[]): EChartsOption {
    return this.buildLineChart(
      snapshots,
      'Gold',
      (snapshot) =>
        (snapshot.ships ?? []).map((ship) => ({
          id: ship.id,
          name: ship.name,
          value: ship.inventory?.gold ?? 0,
        }))
    )
  }

  private buildLineChart(
    snapshots: WorldSnapshotDto[],
    yAxisName: string,
    pointsOf: (snapshot: WorldSnapshotDto) => { id: string; name: string; value: number }[]
  ): EChartsOption {
    const seriesById = new Map<string, { name: string; data: [number, number][] }>()
    for (const snapshot of snapshots) {
      for (const point of pointsOf(snapshot)) {
        const series = seriesById.get(point.id) ?? { name: point.name, data: [] }
        series.data.push([snapshot.tick, point.value])
        seriesById.set(point.id, series)
      }
    }

    const nameCounts = new Map<string, number>()
    for (const series of seriesById.values()) {
      nameCounts.set(series.name, (nameCounts.get(series.name) ?? 0) + 1)
    }

    const series = [...seriesById.entries()]
      .sort((left, right) => left[1].name.localeCompare(right[1].name))
      .map(([id, item]) => ({
        name: (nameCounts.get(item.name) ?? 0) > 1 ? `${item.name} (${id.slice(0, 6)})` : item.name,
        type: 'line' as const,
        showSymbol: item.data.length < 24,
        data: item.data,
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
        name: yAxisName,
        min: 0,
        minInterval: 1,
      },
      series,
    }
  }
}
