import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { GameLogDto } from '../../models/game-log.dto'
import { environment } from '../../../environments/environment'

@Injectable({
  providedIn: 'root',
})
export class GameLogService {
  private readonly baseApiUrl = `${environment.apiBaseUrl}/logs`

  constructor(private readonly httpClient: HttpClient) {}

  public listLogs(): Observable<GameLogDto[]> {
    return this.httpClient.get<GameLogDto[]>(this.baseApiUrl)
  }
}
