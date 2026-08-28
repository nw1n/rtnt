import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { ClockDto } from '../../models/clock.dto'
import { environment } from '../../../environments/environment'

@Injectable({
  providedIn: 'root',
})
export class ClockService {
  private readonly baseApiUrl = `${environment.apiBaseUrl}/clock`

  constructor(private readonly httpClient: HttpClient) {}

  public getClock(): Observable<ClockDto> {
    return this.httpClient.get<ClockDto>(this.baseApiUrl)
  }

  public pause(): Observable<ClockDto> {
    return this.httpClient.post<ClockDto>(`${this.baseApiUrl}/pause`, null)
  }

  public resume(): Observable<ClockDto> {
    return this.httpClient.post<ClockDto>(`${this.baseApiUrl}/resume`, null)
  }

  public setMode(mode: ClockDto['mode']): Observable<ClockDto> {
    return this.httpClient.post<ClockDto>(`${this.baseApiUrl}/mode`, { mode })
  }

  public advance(ticks: number): Observable<ClockDto> {
    return this.httpClient.post<ClockDto>(`${this.baseApiUrl}/advance`, { ticks })
  }
}
