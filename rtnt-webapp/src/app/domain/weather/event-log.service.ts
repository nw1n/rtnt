import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { WorldEventDto } from '../../models/world-event.dto'
import { environment } from '../../../environments/environment'

@Injectable({
  providedIn: 'root',
})
export class EventLogService {
  private readonly baseApiUrl = `${environment.apiBaseUrl}/events`

  constructor(private readonly httpClient: HttpClient) {}

  public listEvents(): Observable<WorldEventDto[]> {
    return this.httpClient.get<WorldEventDto[]>(this.baseApiUrl)
  }
}
