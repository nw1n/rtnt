import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core'
import { MatButtonModule } from '@angular/material/button'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { IslandService } from '../domain/island/island.service'

@Component({
  selector: 'app-debug-page',
  imports: [ElderSinglePaneWrapperComponent, MatButtonModule],
  templateUrl: './debug-page.html',
  styleUrl: './debug-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DebugPage {
  private readonly islandService = inject(IslandService)

  public busy = signal(false)
  public status = signal<string | null>(null)

  public recreateIslands(): void {
    if (this.busy()) {
      return
    }
    this.busy.set(true)
    this.status.set(null)
    this.islandService.recreateIslands().subscribe({
      next: () => {
        this.busy.set(false)
        this.status.set('Islands recreated.')
      },
      error: () => {
        this.busy.set(false)
        this.status.set('Failed to recreate islands.')
      },
    })
  }
}
