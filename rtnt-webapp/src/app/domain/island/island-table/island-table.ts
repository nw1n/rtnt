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

  public readonly columns = ['name', 'population', 'x', 'y']
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
