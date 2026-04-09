import { ChangeDetectionStrategy, Component } from '@angular/core'
import { ElderNavListComponent } from '@elderbyte/ngx-starter'
import { ElderNavLinkComponent } from '@elderbyte/ngx-starter'

@Component({
  selector: 'app-main-nav-menu',
  templateUrl: './main-nav-menu.component.html',
  imports: [ElderNavListComponent, ElderNavLinkComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MainNavMenuComponent {}
