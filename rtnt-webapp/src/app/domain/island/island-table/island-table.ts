import { ChangeDetectionStrategy, Component, effect, inject, signal, untracked, viewChild } from '@angular/core'
import { MatButtonModule } from '@angular/material/button'
import { MatSort, MatSortModule } from '@angular/material/sort'
import { MatTableDataSource, MatTableModule } from '@angular/material/table'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { IslandDto } from '../../../models/island.dto'
import { IslandService } from '../island.service'

@Component({
  selector: 'app-island-table',
  imports: [ElderSinglePaneWrapperComponent, MatButtonModule, MatSortModule, MatTableModule],
  templateUrl: './island-table.html',
  styleUrl: './island-table.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IslandTable {
  private readonly islandService = inject(IslandService)
  private readonly sort = viewChild(MatSort)

  public readonly columns = [
    'name',
    'population',
    'gold',
    'rum',
    'sugar',
    'spices',
    'tobacco',
    'rumPrice',
    'sugarPrice',
    'spicesPrice',
    'tobaccoPrice',
    'x',
    'y',
  ]
  public readonly dataSource = new MatTableDataSource<IslandDto>([])
  public islands = signal<IslandDto[]>([])
  public busy = signal(false)
  public error = signal<string | null>(null)

  constructor() {
    effect(() => {
      const sort = this.sort()
      if (sort) {
        untracked(() => {
          this.dataSource.sort = sort
        })
      }
    })
    this.dataSource.sortingDataAccessor = (island, header): string | number => {
      if (header === 'gold' || header === 'rum' || header === 'sugar' || header === 'spices' || header === 'tobacco') {
        return island.inventory[header]
      }
      if (header === 'rumPrice') {
        return island.tradePrices.rum
      }
      if (header === 'sugarPrice') {
        return island.tradePrices.sugar
      }
      if (header === 'spicesPrice') {
        return island.tradePrices.spices
      }
      if (header === 'tobaccoPrice') {
        return island.tradePrices.tobacco
      }
      const value = island[header as keyof IslandDto]
      if (value == null || typeof value === 'object') {
        return ''
      }
      return value
    }
    this.refresh()
  }

  public refresh(): void {
    if (this.busy()) {
      return
    }
    this.busy.set(true)
    this.error.set(null)
    this.islandService.listIslands().subscribe({
      next: (islands) => {
        this.islands.set(islands)
        this.dataSource.data = islands
        this.busy.set(false)
      },
      error: () => {
        this.busy.set(false)
        this.error.set('Failed to load islands.')
      },
    })
  }
}
