import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable, catchError, map, of, tap } from 'rxjs'
import { environment } from '../../../environments/environment'
import { PlayerDto } from '../../models/player.dto'

@Injectable({
  providedIn: 'root',
})
export class PlayerService {
  public static readonly SelectedPlayerStorageKey = 'rtnt.selectedPlayerId'
  private readonly baseApiUrl = `${environment.apiBaseUrl}/players`
  private selectedPlayerId: string | null

  constructor(private readonly httpClient: HttpClient) {
    const raw = localStorage.getItem(PlayerService.SelectedPlayerStorageKey)
    const trimmed = raw?.trim() ?? ''
    if (trimmed.length > 0) {
      this.selectedPlayerId = trimmed
    } else {
      this.selectedPlayerId = null
      if (raw !== null) {
        localStorage.removeItem(PlayerService.SelectedPlayerStorageKey)
      }
    }
  }

  public listPlayers(): Observable<PlayerDto[]> {
    return this.httpClient.get<PlayerDto[]>(this.baseApiUrl)
  }

  public getSelectedPlayerId(): string | null {
    return this.selectedPlayerId
  }

  public setSelectedPlayerId(playerId: string): void {
    const normalized = playerId.trim()
    this.selectedPlayerId = normalized
    localStorage.setItem(PlayerService.SelectedPlayerStorageKey, normalized)
  }

  public clearSelectedPlayerId(): void {
    this.selectedPlayerId = null
    localStorage.removeItem(PlayerService.SelectedPlayerStorageKey)
  }

  /**
   * If a player id is stored, checks the server player list. Clears storage when that id is missing.
   * On request failure, storage is left unchanged.
   */
  public ensureStoredPlayerIdValid(): Observable<void> {
    const id = this.selectedPlayerId
    if (!id) {
      return of(undefined)
    }
    return this.listPlayers().pipe(
      tap((players) => {
        if (!players.some((player) => player.id === id)) {
          this.clearSelectedPlayerId()
        }
      }),
      map((): void => undefined),
      catchError(() => of(undefined))
    )
  }
}
