import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core'
import { LoggerFactory } from '@elderbyte/ts-logger'
import {
  DataContextBuilder,
  ElderDataCommonModule,
  ElderSearchModule,
  ElderTableModule,
  IDataContext,
} from '@elderbyte/ngx-starter'
import { ShipDto } from '../../../models/ship.dto'
import { ShipService } from '../ship.service'
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

@Component({
  selector: 'app-ship-browser',
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
  templateUrl: './ship-browser.html',
  styleUrl: './ship-browser.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShipBrowser implements OnInit {
  private readonly log = LoggerFactory.getLogger(this.constructor.name)

  public data: IDataContext<ShipDto>

  constructor(private readonly shipService: ShipService) {
    this.data = DataContextBuilder.start<ShipDto>()
      .reloadOnLocalChanges()
      .localSort()
      .build(shipService)
  }

  public ngOnInit(): void {
    this.data.start()
  }

  public createNew(event: Event): void {
    // TODO: Implement create new ship functionality if needed
    this.log.info('Create new ship requested')
  }

  public openDetail(ship: ShipDto): void {
    // TODO: Implement detail view navigation if needed
    this.log.info('Opening detail for ship:', ship.id)
  }

  public refresh(): void {
    if (this.data.isStarted) {
      this.log.info('Refreshing ship data...')
      this.data.reload()
    }
  }
}
