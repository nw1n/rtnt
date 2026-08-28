import { Application, Container, Graphics, Text } from 'pixi.js'
import { IslandDto } from '../../../models/island.dto'

const MAP_MIN_PADDING = 40
const MAP_PADDING_RATIO = 0.08
const WATER_COLOR = 0x82c4cc
const ISLAND_FILL = 0xe7d6a2
const ISLAND_STROKE = 0x866f36
const LABEL_FILL = 0x2f2412

export class IslandMapPixi {
  private app: Application | null = null
  private readonly world = new Container()
  private islands: IslandDto[] = []
  private resizeObserver: ResizeObserver | null = null
  private host: HTMLElement | null = null

  public async attach(host: HTMLElement): Promise<void> {
    if (this.app) {
      return
    }
    this.host = host
    const app = new Application()
    await app.init({
      background: WATER_COLOR,
      antialias: true,
      autoDensity: true,
      resolution: window.devicePixelRatio || 1,
      resizeTo: host,
    })
    host.appendChild(app.canvas)
    app.stage.addChild(this.world)
    this.app = app
    this.resizeObserver = new ResizeObserver(() => this.fitWorld())
    this.resizeObserver.observe(host)
  }

  public render(islands: IslandDto[]): void {
    this.islands = islands
    this.world.removeChildren()
    for (const island of islands) {
      const shape = new Graphics()
      shape.roundRect(island.x, island.y, island.width, island.length, 8)
      shape.fill({ color: ISLAND_FILL })
      shape.stroke({ width: 2, color: ISLAND_STROKE })
      this.world.addChild(shape)

      const label = new Text({
        text: island.name,
        style: {
          fontFamily: 'sans-serif',
          fontSize: 20,
          fontWeight: '600',
          fill: LABEL_FILL,
          align: 'center',
        },
        anchor: 0.5,
      })
      label.position.set(island.x + island.width / 2, island.y + island.length / 2)
      label.eventMode = 'none'
      this.world.addChild(label)
    }
    this.fitWorld()
  }

  public destroy(): void {
    this.resizeObserver?.disconnect()
    this.resizeObserver = null
    this.app?.destroy(true)
    this.app = null
    this.host = null
    this.world.removeChildren()
  }

  private fitWorld(): void {
    const app = this.app
    if (!app || this.islands.length === 0) {
      return
    }

    let minX = Number.POSITIVE_INFINITY
    let minY = Number.POSITIVE_INFINITY
    let maxX = Number.NEGATIVE_INFINITY
    let maxY = Number.NEGATIVE_INFINITY
    for (const island of this.islands) {
      minX = Math.min(minX, island.x)
      minY = Math.min(minY, island.y)
      maxX = Math.max(maxX, island.x + island.width)
      maxY = Math.max(maxY, island.y + island.length)
    }

    const spanX = Math.max(1, maxX - minX)
    const spanY = Math.max(1, maxY - minY)
    const padding = Math.max(MAP_MIN_PADDING, Math.max(spanX, spanY) * MAP_PADDING_RATIO)
    const worldWidth = spanX + padding * 2
    const worldHeight = spanY + padding * 2
    const viewWidth = app.screen.width
    const viewHeight = app.screen.height
    const scale = Math.min(viewWidth / worldWidth, viewHeight / worldHeight)
    this.world.scale.set(scale)
    this.world.position.set(
      (viewWidth - worldWidth * scale) / 2 - (minX - padding) * scale,
      (viewHeight - worldHeight * scale) / 2 - (minY - padding) * scale
    )
  }
}
