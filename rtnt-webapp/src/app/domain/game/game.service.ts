import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { environment } from '../../../environments/environment'

export interface GameTicksDto {
  tickCount: number
}

@Injectable({
  providedIn: 'root',
})
export class GameService {
  private readonly gameApiUrl = `${environment.apiBaseUrl}/game`

  constructor(private readonly httpClient: HttpClient) {}

  public getTicks(): Observable<GameTicksDto> {
    return this.httpClient.get<GameTicksDto>(`${this.gameApiUrl}/ticks`)
  }
}
