import { ChangeDetectionStrategy, Component, effect, inject, signal, untracked, viewChild } from '@angular/core'
import { MatButtonModule } from '@angular/material/button'
import { MatSort, MatSortModule } from '@angular/material/sort'
import { MatTableDataSource, MatTableModule } from '@angular/material/table'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { ShipDto } from '../../../models/ship.dto'
import { ShipService } from '../ship.service'

@Component({
  selector: 'app-ship-table',
  imports: [ElderSinglePaneWrapperComponent, MatButtonModule, MatSortModule, MatTableModule],
  templateUrl: './ship-table.html',
  styleUrl: './ship-table.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShipTable {
  private readonly shipService = inject(ShipService)
  private readonly sort = viewChild(MatSort)

  public readonly columns = [
    'name',
    'islandName',
    'journeyActive',
    'destination',
    'playerId',
    'speed',
    'gold',
    'food',
    'rum',
    'sugar',
    'spices',
    'tobacco',
    'cargoCapacity',
  ]
  public readonly dataSource = new MatTableDataSource<ShipDto>([])
  public ships = signal<ShipDto[]>([])
  public busy = signal(false)
  public error = signal<string | null>(null)

  constructor() {
    this.dataSource.sortingDataAccessor = (ship, header): string | number => {
      if (header === 'journeyActive') {
        return ship.journey?.active ? 1 : 0
      }
      if (header === 'destination') {
        return ship.journey?.targetIslandName ?? ''
      }
      if (
        header === 'gold' ||
        header === 'food' ||
        header === 'rum' ||
        header === 'sugar' ||
        header === 'spices' ||
        header === 'tobacco'
      ) {
        return ship.inventory[header]
      }
      const value = ship[header as keyof ShipDto]
      if (value == null || typeof value === 'object') {
        return ''
      }
      return value
    }
    effect(() => {
      const sort = this.sort()
      if (sort) {
        untracked(() => {
          this.dataSource.sort = sort
        })
      }
    })
    this.refresh()
  }

  public refresh(): void {
    if (this.busy()) {
      return
    }
    this.busy.set(true)
    this.error.set(null)
    this.shipService.listShips().subscribe({
      next: (ships) => {
        this.ships.set(ships)
        this.dataSource.data = ships
        this.busy.set(false)
      },
      error: () => {
        this.busy.set(false)
        this.error.set('Failed to load ships.')
      },
    })
  }
}
