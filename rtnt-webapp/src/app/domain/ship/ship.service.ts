import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { ShipDto } from '../../models/ship.dto'
import { environment } from '../../../environments/environment'

@Injectable({
  providedIn: 'root',
})
export class ShipService {
  private readonly baseApiUrl = `${environment.apiBaseUrl}/ships`

  constructor(private readonly httpClient: HttpClient) {}

  public listShips(): Observable<ShipDto[]> {
    return this.httpClient.get<ShipDto[]>(this.baseApiUrl)
  }
}
