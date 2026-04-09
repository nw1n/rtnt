import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { RestClientList } from '@elderbyte/ngx-starter'
import { IslandDto } from '../../models/island.dto'
import { environment } from '../../../environments/environment'
import { Observable } from 'rxjs'

@Injectable({
  providedIn: 'root',
})
export class IslandService extends RestClientList<IslandDto, string> {
  public static readonly IdField = 'id'
  private readonly baseApiUrl = `${environment.apiBaseUrl}/islands`

  constructor(private readonly httpClient: HttpClient) {
    super(`${environment.apiBaseUrl}/islands`, httpClient, {
      idField: IslandService.IdField,
    })
  }

  public listIslands(): Observable<IslandDto[]> {
    return this.httpClient.get<IslandDto[]>(this.baseApiUrl)
  }
}
