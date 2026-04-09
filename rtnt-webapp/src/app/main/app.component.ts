import { Component, inject } from '@angular/core'
import { Router } from '@angular/router'
import { LoggerFactory } from '@elderbyte/ts-logger'
import { Observable, of } from 'rxjs'
import {
  ElderDataTransferModule,
  ElderExpandToggleButtonModule,
  ElderNavModule,
  ElderPanelModule,
  ElderRailNavDirective,
  ElderShellModule,
  ElderShellService,
  ElderShellStaticNavSlotDirective,
  ElderStaticNavToggleComponent,
  ElderThemeModule,
  ElderThemeToggleComponent,
  ElderTogglePanelComponent,
  ElderToolbarModule,
} from '@elderbyte/ngx-starter'
import { TranslateService } from '@ngx-translate/core'
import { PlayerService } from '../domain/player/player.service'
import { MainNavMenuComponent } from './main-nav-menu/main-nav-menu.component'
import { PlayerStatusComponent } from './player-status/player-status'

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  imports: [
    ElderShellModule,
    ElderToolbarModule,
    ElderNavModule,
    ElderThemeToggleComponent,
    ElderStaticNavToggleComponent,
    MainNavMenuComponent,
    PlayerStatusComponent,
    ElderTogglePanelComponent,
    ElderDataTransferModule,
    ElderExpandToggleButtonModule,
    ElderPanelModule,
    ElderThemeModule,
    ElderShellStaticNavSlotDirective,
    ElderRailNavDirective,
  ],
})
export class AppComponent {
  /***************************************************************************
   *                                                                         *
   * Fields                                                                  *
   *                                                                         *
   **************************************************************************/

  private readonly log = LoggerFactory.getLogger(this.constructor.name)

  /***************************************************************************
   *                                                                         *
   * Constructor                                                             *
   *                                                                         *
   **************************************************************************/

  constructor(private router: Router) {
    const translate = inject(TranslateService)
    const shellService = inject(ElderShellService)
    const playerService = inject(PlayerService)

    playerService.ensureStoredPlayerIdValid().subscribe()

    translate.addLangs(['de', 'en'])
    translate.setDefaultLang('de')

    // start with static nav open
    shellService.openStaticNav()
  }

  /***************************************************************************
   *                                                                         *
   * Account                                                                 *
   *                                                                         *
   **************************************************************************/

  public get accountUrl(): string {
    return ''
  }

  public get principal(): Observable<any> {
    return of(null)
  }

  public login(): void {}

  public logout(): void {}

  /***************************************************************************
   *                                                                         *
   * Public API                                                              *
   *                                                                         *
   **************************************************************************/
}
