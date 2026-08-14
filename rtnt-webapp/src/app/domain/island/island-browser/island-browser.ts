import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core'
import {
  DataContextBuilder,
  ElderDataCommonModule,
  ElderSearchModule,
  ElderTableModule,
  IDataContext,
} from '@elderbyte/ngx-starter'
import { IslandDto } from '../../../models/island.dto'
import { IslandService } from '../island.service'
import { MatIcon, MatIconModule } from '@angular/material/icon'
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
} from '@angular/material/table'
import { MatSort, MatSortHeader } from '@angular/material/sort'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { MatButtonModule } from '@angular/material/button'

@Component({
  selector: 'app-island-browser',
  imports: [
    ElderSinglePaneWrapperComponent,
    ElderTableModule,
    MatSort,
    ElderDataCommonModule,
    ElderSearchModule,
    MatColumnDef,
    MatHeaderCellDef,
    MatHeaderCell,
    MatCellDef,
    MatCell,
    MatIcon,
    MatSortHeader,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './island-browser.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IslandBrowser implements OnInit {
  public data: IDataContext<IslandDto>

  constructor(private readonly islandService: IslandService) {
    this.data = DataContextBuilder.start<IslandDto>()
      .reloadOnLocalChanges()
      .localSort()
      .build(islandService)
  }

  public ngOnInit(): void {
    this.data.start()
  }

  public createNew(): void {
    this.islandService.createIsland().subscribe(() => this.refresh())
  }

  public refresh(): void {
    if (this.data.isStarted) {
      this.data.reload()
    }
  }
}
