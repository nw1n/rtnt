import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
  signal,
} from '@angular/core'
import { CommonModule, DatePipe } from '@angular/common'
import { MatIconButton } from '@angular/material/button'
import { MatIconModule } from '@angular/material/icon'
import { MatMenuModule } from '@angular/material/menu'
import { MatDividerModule } from '@angular/material/divider'
import { firstValueFrom } from 'rxjs'
import { PlayerDto } from '../../models/player.dto'
import { ShipDto } from '../../models/ship.dto'
import { PlayerService } from '../../domain/player/player.service'
import { ShipService } from '../../domain/ship/ship.service'

export type PlayerStatusPayload =
  | { kind: 'idle' }
  | { kind: 'no-selection' }
  | { kind: 'error'; message: string }
  | { kind: 'ready'; player: PlayerDto; ships: ShipDto[] }

async function abortable<T>(signal: AbortSignal, promise: Promise<T>): Promise<T> {
  if (signal.aborted) {
    throw new DOMException('Aborted', 'AbortError')
  }
  return await Promise.race([
    promise,
    new Promise<T>((_, reject) => {
      signal.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), {
        once: true,
      })
    }),
  ])
}

@Component({
  selector: 'app-player-status',
  imports: [CommonModule, MatIconButton, MatIconModule, MatMenuModule, MatDividerModule, DatePipe],
  templateUrl: './player-status.html',
  styleUrl: './player-status.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlayerStatusComponent {
  private readonly playerService = inject(PlayerService)
  private readonly shipService = inject(ShipService)

  /** Incremented each time the menu opens so `resource` reloads. */
  private readonly menuGeneration = signal(0)

  readonly playerStatus = resource({
    params: () => ({ gen: this.menuGeneration() }),
    loader: async ({ params, abortSignal }): Promise<PlayerStatusPayload> => {
      if (params.gen === 0) {
        return { kind: 'idle' }
      }

      const selectedId = this.playerService.getSelectedPlayerId()
      if (!selectedId) {
        return { kind: 'no-selection' }
      }

      try {
        const players = await abortable(
          abortSignal,
          firstValueFrom(this.playerService.listPlayers())
        )
        abortSignal.throwIfAborted()
        const player = players.find((p) => p.id === selectedId) ?? null
        if (!player) {
          return { kind: 'error', message: 'Selected player was not found.' }
        }

        const ships = await abortable(
          abortSignal,
          firstValueFrom(this.shipService.listShips({ playerId: player.id }))
        )
        return { kind: 'ready', player, ships }
      } catch (e) {
        if (e instanceof DOMException && e.name === 'AbortError') {
          throw e
        }
        const message = e instanceof Error ? e.message : 'Could not load player status.'
        return { kind: 'error', message }
      }
    },
    defaultValue: { kind: 'idle' } satisfies PlayerStatusPayload,
  })

  readonly payload = computed(() => this.playerStatus.value())

  readonly readyPayload = computed(() => {
    const p = this.playerStatus.value()
    return p.kind === 'ready' ? p : null
  })

  readonly payloadErrorMessage = computed(() => {
    const p = this.playerStatus.value()
    return p.kind === 'error' ? p.message : null
  })

  readonly resourceErrorMessage = computed(() => this.playerStatus.error()?.message ?? null)

  readonly triggerIconColor = computed(() => {
    const ready = this.readyPayload()
    return ready ? ready.player.hexColor : null
  })

  onMenuOpened(): void {
    this.menuGeneration.update((g) => g + 1)
  }
}
