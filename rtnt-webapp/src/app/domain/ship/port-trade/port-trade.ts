import {
  afterNextRender,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  inject,
  Injector,
  OnInit,
} from '@angular/core'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { CommonModule } from '@angular/common'
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms'
import { RouterLink } from '@angular/router'
import { MatButtonModule } from '@angular/material/button'
import { MatFormFieldModule } from '@angular/material/form-field'
import { MatInputModule } from '@angular/material/input'
import { MatSelectModule } from '@angular/material/select'
import { MatDividerModule } from '@angular/material/divider'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { forkJoin, finalize } from 'rxjs'
import { IslandDto } from '../../../models/island.dto'
import { ShipDto } from '../../../models/ship.dto'
import { IslandService } from '../../island/island.service'
import { PlayerService } from '../../player/player.service'
import { ShipService, TradeGoodType } from '../ship.service'

type TradeInvKey = 'rum' | 'sugar' | 'spices' | 'tobacco'

interface TradeGoodRow {
  readonly type: TradeGoodType
  readonly label: string
  readonly invKey: TradeInvKey
}

function cloneIslandForPort(island: IslandDto): IslandDto {
  return {
    ...island,
    footprint: { ...island.footprint },
    inventory: { ...island.inventory },
    tradePrices: { ...island.tradePrices },
  }
}

@Component({
  selector: 'app-port-trade',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDividerModule,
    ElderSinglePaneWrapperComponent,
  ],
  templateUrl: './port-trade.html',
  styleUrl: './port-trade.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class PortTrade implements OnInit {
  private readonly cdr = inject(ChangeDetectorRef)
  private readonly destroyRef = inject(DestroyRef)
  private readonly injector = inject(Injector)

  public isLoading = false
  public loadError: string | null = null
  public tradeError: string | null = null
  public tradeBusy = false

  public dockedShips: ShipDto[] = []
  public portIsland: IslandDto | null = null

  private allIslands: IslandDto[] = []

  public readonly tradeGoods: TradeGoodRow[] = [
    { type: 'RUM', label: 'Rum', invKey: 'rum' },
    { type: 'SUGAR', label: 'Sugar', invKey: 'sugar' },
    { type: 'SPICES', label: 'Spices', invKey: 'spices' },
    { type: 'TOBACCO', label: 'Tobacco', invKey: 'tobacco' },
  ]

  public readonly shipControl = this.formBuilder.control<string | null>(null)

  public readonly tradeAmounts = this.formBuilder.nonNullable.group({
    rum: [1, [Validators.required, Validators.min(1)]],
    sugar: [1, [Validators.required, Validators.min(1)]],
    spices: [1, [Validators.required, Validators.min(1)]],
    tobacco: [1, [Validators.required, Validators.min(1)]],
  })

  constructor(
    private readonly formBuilder: FormBuilder,
    public readonly playerService: PlayerService,
    private readonly shipService: ShipService,
    private readonly islandService: IslandService
  ) {}

  public ngOnInit(): void {
    this.shipControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.syncPortContext()
      this.cdr.markForCheck()
    })

    // Route + OnPush shell (elder-shell / elder-single-pane-wrapper): running reload synchronously in
    // ngOnInit can finish HTTP before the view is consistently attached, so Async updates may not run
    // change detection for this subtree. Defer to after first render, then nudge CD after each load.
    afterNextRender(
      () => {
        this.reload()
      },
      { injector: this.injector }
    )
  }

  public reload(): void {
    const playerId = this.playerService.getSelectedPlayerId()
    if (!playerId) {
      this.isLoading = false
      this.loadError = null
      this.dockedShips = []
      this.portIsland = null
      this.allIslands = []
      this.shipControl.setValue(null, { emitEvent: false })
      this.cdr.markForCheck()
      return
    }

    this.isLoading = true
    this.loadError = null
    this.cdr.markForCheck()

    forkJoin({
      ships: this.shipService.listShips({ playerId }),
      islands: this.islandService.listIslands(),
    })
      .pipe(
        finalize(() => {
          this.isLoading = false
          this.cdr.markForCheck()
        })
      )
      .subscribe({
        next: ({ ships, islands }) => {
          this.allIslands = islands
          this.dockedShips = ships.filter((s) => !s.journey?.active && !!s.islandId)
          const preferredId = this.shipControl.value
          const nextId =
            preferredId && this.dockedShips.some((s) => s.id === preferredId)
              ? preferredId
              : (this.dockedShips[0]?.id ?? null)
          this.shipControl.setValue(nextId, { emitEvent: false })
          this.syncPortContext()
        },
        error: (error) => {
          this.loadError = error?.error?.message ?? 'Could not load ships or islands.'
          this.dockedShips = []
          this.portIsland = null
        },
      })
  }

  public get selectedShip(): ShipDto | null {
    const id = this.shipControl.value
    if (!id) {
      return null
    }
    return this.dockedShips.find((s) => s.id === id) ?? null
  }

  public amountFor(row: TradeGoodRow): number {
    const raw = this.tradeAmounts.get(row.invKey)?.value
    const n = typeof raw === 'number' ? raw : Number(raw)
    return Number.isFinite(n) && n >= 1 ? Math.floor(n) : 0
  }

  public unitPrice(row: TradeGoodRow): number {
    return this.portIsland?.tradePrices[row.invKey] ?? 0
  }

  public totalPrice(row: TradeGoodRow): number {
    return this.unitPrice(row) * this.amountFor(row)
  }

  public canBuy(row: TradeGoodRow): boolean {
    const ship = this.selectedShip
    const island = this.portIsland
    if (!ship || !island || this.tradeBusy) {
      return false
    }
    const amt = this.amountFor(row)
    if (amt < 1) {
      return false
    }
    const total = this.totalPrice(row)
    return ship.inventory.gold >= total && island.inventory[row.invKey] >= amt
  }

  public canSell(row: TradeGoodRow): boolean {
    const ship = this.selectedShip
    const island = this.portIsland
    if (!ship || !island || this.tradeBusy) {
      return false
    }
    const amt = this.amountFor(row)
    if (amt < 1) {
      return false
    }
    const total = this.totalPrice(row)
    return ship.inventory[row.invKey] >= amt && island.inventory.gold >= total
  }

  public trade(side: 'buy' | 'sell', row: TradeGoodRow): void {
    const ship = this.selectedShip
    if (!ship || !this.portIsland || this.tradeBusy) {
      return
    }
    const amount = this.amountFor(row)
    if (amount < 1) {
      this.tradeError = 'Enter an amount of at least 1.'
      return
    }

    this.tradeBusy = true
    this.tradeError = null
    const request = { goodType: row.type, amount }
    const call =
      side === 'buy'
        ? this.shipService.buyFromIsland(ship.id, request)
        : this.shipService.sellToIsland(ship.id, request)

    call.pipe(finalize(() => (this.tradeBusy = false))).subscribe({
      next: () => {
        this.tradeError = null
        window.location.reload()
      },
      error: (error) => {
        this.tradeError = error?.error?.message ?? 'Trade failed.'
      },
    })
  }

  private syncPortContext(): void {
    const ship = this.selectedShip
    if (!ship?.islandId) {
      this.portIsland = null
      return
    }
    const found = this.allIslands.find((i) => i.id === ship.islandId)
    this.portIsland = found ? cloneIslandForPort(found) : null
  }

  private patchDockedShip(updated: ShipDto): void {
    this.dockedShips = this.dockedShips.map((s) => (s.id === updated.id ? updated : s))
  }

  private applyIslandTradeDelta(side: 'buy' | 'sell', invKey: TradeInvKey, amount: number): void {
    const island = this.portIsland
    if (!island) {
      return
    }
    const inv = island.inventory
    const unit = island.tradePrices[invKey]
    const total = unit * amount
    if (side === 'buy') {
      inv[invKey] -= amount
      inv.gold += total
    } else {
      inv[invKey] += amount
      inv.gold -= total
    }
  }
}
