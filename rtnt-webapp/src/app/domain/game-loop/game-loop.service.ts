import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { GameLoopDto } from '../../models/game-loop.dto'
import { environment } from '../../../environments/environment'

@Injectable({
  providedIn: 'root',
})
export class GameLoopService {
  private readonly baseApiUrl = `${environment.apiBaseUrl}/game-loop`

  constructor(private readonly httpClient: HttpClient) {}

  public get(): Observable<GameLoopDto> {
    return this.httpClient.get<GameLoopDto>(this.baseApiUrl)
  }

  public pause(): Observable<GameLoopDto> {
    return this.httpClient.post<GameLoopDto>(`${this.baseApiUrl}/pause`, null)
  }

  public resume(): Observable<GameLoopDto> {
    return this.httpClient.post<GameLoopDto>(`${this.baseApiUrl}/resume`, null)
  }

  public setMode(mode: GameLoopDto['mode']): Observable<GameLoopDto> {
    return this.httpClient.post<GameLoopDto>(`${this.baseApiUrl}/mode`, { mode })
  }

  public advance(ticks: number): Observable<GameLoopDto> {
    return this.httpClient.post<GameLoopDto>(`${this.baseApiUrl}/advance`, { ticks })
  }
}
