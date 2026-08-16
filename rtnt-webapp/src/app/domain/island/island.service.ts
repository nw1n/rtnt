import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { IslandDto } from '../../models/island.dto'
import { environment } from '../../../environments/environment'

@Injectable({
  providedIn: 'root',
})
export class IslandService {
  private readonly baseApiUrl = `${environment.apiBaseUrl}/islands`

  constructor(private readonly httpClient: HttpClient) {}

  public listIslands(): Observable<IslandDto[]> {
    return this.httpClient.get<IslandDto[]>(this.baseApiUrl)
  }
}
