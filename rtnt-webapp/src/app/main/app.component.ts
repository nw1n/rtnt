import { Component, inject } from '@angular/core'
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
import { MainNavMenuComponent } from './main-nav-menu/main-nav-menu.component'

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
  constructor() {
    inject(ElderShellService).openStaticNav()
  }

  public get accountUrl(): string {
    return ''
  }

  public get principal(): Observable<any> {
    return of(null)
  }

  public login(): void {}

  public logout(): void {}
}
