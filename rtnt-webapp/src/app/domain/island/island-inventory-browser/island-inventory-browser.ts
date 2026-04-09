import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core'
import { LoggerFactory } from '@elderbyte/ts-logger'
import {
  DataContextBuilder,
  ElderDataCommonModule,
  ElderSearchModule,
  ElderSinglePaneWrapperComponent,
  ElderTableModule,
  IDataContext,
} from '@elderbyte/ngx-starter'
import { IslandDto } from '../../../models/island.dto'
import { IslandService } from '../island.service'
import { TranslateModule } from '@ngx-translate/core'
import { MatIcon, MatIconModule } from '@angular/material/icon'
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
} from '@angular/material/table'
import { MatSort, MatSortHeader } from '@angular/material/sort'
import { MatButtonModule } from '@angular/material/button'

@Component({
  selector: 'app-island-inventory-browser',
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
    TranslateModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './island-inventory-browser.html',
  styleUrl: './island-inventory-browser.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IslandInventoryBrowser implements OnInit {
  private readonly log = LoggerFactory.getLogger(this.constructor.name)

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

  public openDetail(island: IslandDto): void {
    this.log.info('Opening inventory detail for island:', island.id)
  }

  public refresh(): void {
    if (this.data.isStarted) {
      this.log.info('Refreshing island inventory data...')
      this.data.reload()
    }
  }
}
