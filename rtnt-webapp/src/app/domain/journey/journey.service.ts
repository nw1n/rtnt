import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { RestClientList } from '@elderbyte/ngx-starter'
import { environment } from '../../../environments/environment'
import { JourneyDto } from '../../models/journey.dto'

@Injectable({
  providedIn: 'root',
})
export class JourneyService extends RestClientList<JourneyDto, string> {
  public static readonly IdField = 'id'

  constructor(httpClient: HttpClient) {
    super(`${environment.apiBaseUrl}/journeys`, httpClient, {
      idField: JourneyService.IdField,
    })
  }
}
