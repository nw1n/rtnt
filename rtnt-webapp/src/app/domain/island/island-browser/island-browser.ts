import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core'
import { LoggerFactory } from '@elderbyte/ts-logger'
import {
  DataContextBuilder,
  ElderDataCommonModule,
  ElderSearchModule,
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
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { DatePipe } from '@angular/common'
import { MatButtonModule } from '@angular/material/button'
import { MatDialog } from '@angular/material/dialog'
import { IslandInteractionDialog } from '../island-interaction-dialog/island-interaction-dialog'

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
    TranslateModule,
    DatePipe,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './island-browser.html',
  styleUrl: './island-browser.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IslandBrowser implements OnInit {
  /***************************************************************************
   *                                                                         *
   * Fields                                                                  *
   *                                                                         *
   **************************************************************************/

  private readonly log = LoggerFactory.getLogger(this.constructor.name)

  public data: IDataContext<IslandDto>

  /***************************************************************************
   *                                                                         *
   * Constructor                                                             *
   *                                                                         *
   **************************************************************************/

  constructor(
    private readonly islandService: IslandService,
    private readonly dialog: MatDialog
  ) {
    this.data = DataContextBuilder.start<IslandDto>()
      .reloadOnLocalChanges()
      .localSort()
      .build(islandService)
  }

  /***************************************************************************
   *                                                                         *
   * Lifecycle                                                               *
   *                                                                         *
   **************************************************************************/

  public ngOnInit(): void {
    this.data.start()
  }

  /***************************************************************************
   *                                                                         *
   * Public API                                                              *
   *                                                                         *
   **************************************************************************/

  public createNew(event: Event): void {
    // TODO: Implement create new island functionality if needed
    this.log.info('Create new island requested')
  }

  public openInteractionDialog(island: IslandDto): void {
    const dialogRef = this.dialog.open(IslandInteractionDialog, {
      data: { island },
      width: '36rem',
      maxWidth: '96vw',
    })
    dialogRef.afterClosed().subscribe((startedJourney) => {
      if (startedJourney) {
        this.refresh()
      }
    })
  }

  public refresh(): void {
    if (this.data.isStarted) {
      this.log.info('Refreshing island data...')
      this.data.reload()
    }
  }
}
