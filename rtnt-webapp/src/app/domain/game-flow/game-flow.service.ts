import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { GameFlowDto } from '../../models/game-flow.dto'
import { environment } from '../../../environments/environment'

@Injectable({
  providedIn: 'root',
})
export class GameFlowService {
  private readonly baseApiUrl = `${environment.apiBaseUrl}/game-flow`

  constructor(private readonly httpClient: HttpClient) {}

  public get(): Observable<GameFlowDto> {
    return this.httpClient.get<GameFlowDto>(this.baseApiUrl)
  }

  public pause(): Observable<GameFlowDto> {
    return this.httpClient.post<GameFlowDto>(`${this.baseApiUrl}/pause`, null)
  }

  public resume(): Observable<GameFlowDto> {
    return this.httpClient.post<GameFlowDto>(`${this.baseApiUrl}/resume`, null)
  }

  public setMode(mode: GameFlowDto['mode']): Observable<GameFlowDto> {
    return this.httpClient.post<GameFlowDto>(`${this.baseApiUrl}/mode`, { mode })
  }

  public advance(ticks: number): Observable<GameFlowDto> {
    return this.httpClient.post<GameFlowDto>(`${this.baseApiUrl}/advance`, { ticks })
  }
}
