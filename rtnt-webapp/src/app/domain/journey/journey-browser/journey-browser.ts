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
import { JourneyDto } from '../../../models/journey.dto'
import { JourneyService } from '../journey.service'
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
import { DatePipe } from '@angular/common'
import { MatButtonModule } from '@angular/material/button'

@Component({
  selector: 'app-journey-browser',
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
  templateUrl: './journey-browser.html',
  styleUrl: './journey-browser.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JourneyBrowser implements OnInit {
  private readonly log = LoggerFactory.getLogger(this.constructor.name)

  public data: IDataContext<JourneyDto>

  constructor(private readonly journeyService: JourneyService) {
    this.data = DataContextBuilder.start<JourneyDto>()
      .reloadOnLocalChanges()
      .localSort()
      .build(journeyService)
  }

  public ngOnInit(): void {
    this.data.start()
  }

  public createNew(event: Event): void {
    this.log.info('Create new journey requested')
  }

  public openDetail(journey: JourneyDto): void {
    this.log.info('Opening detail for journey:', journey.id)
  }

  public refresh(): void {
    if (this.data.isStarted) {
      this.log.info('Refreshing journey data...')
      this.data.reload()
    }
  }
}
