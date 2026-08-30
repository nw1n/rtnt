import { afterNextRender, Directive, effect, ElementRef, inject, input, OnDestroy } from '@angular/core'
import * as echarts from 'echarts'

@Directive({
  selector: '[appEcharts]',
})
export class EchartsDirective implements OnDestroy {
  public readonly option = input.required<echarts.EChartsOption>({ alias: 'appEcharts' })
  public readonly dark = input(false)

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef)
  private chart: echarts.ECharts | null = null
  private resizeObserver: ResizeObserver | null = null
  private lastDark: boolean | null = null
  private viewReady = false

  constructor() {
    afterNextRender(() => {
      this.viewReady = true
      this.resizeObserver = new ResizeObserver(() => this.chart?.resize())
      this.resizeObserver.observe(this.host.nativeElement)
      this.render()
    })
    effect(() => {
      this.option()
      this.dark()
      this.render()
    })
  }

  public ngOnDestroy(): void {
    this.resizeObserver?.disconnect()
    this.chart?.dispose()
    this.chart = null
  }

  private render(): void {
    if (!this.viewReady) {
      return
    }
    const element = this.host.nativeElement
    const dark = this.dark()
    if (!this.chart || this.lastDark !== dark) {
      this.chart?.dispose()
      this.chart = echarts.init(element, dark ? 'dark' : undefined)
      this.lastDark = dark
    }
    this.chart.setOption(this.option(), true)
    this.chart.resize()
  }
}
