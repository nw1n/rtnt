import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { RestClientList } from '@elderbyte/ngx-starter'
import { ShipDto } from '../../models/ship.dto'
import { environment } from '../../../environments/environment'
import { Observable } from 'rxjs'
import { HttpParams } from '@angular/common/http'

export interface CreatePlayerControlledShipRequest {
  shipName: string
  playerName: string
}

export interface CreateShipForExistingPlayerRequest {
  shipName: string
  playerId: string
}

export interface StartJourneyRequest {
  playerId: string
  targetIslandId: string
}

export type TradeGoodType = 'RUM' | 'SUGAR' | 'SPICES' | 'TOBACCO'

export interface TradeRequest {
  goodType: TradeGoodType
  amount: number
}

export interface CreatePlayerControlledShipResponse {
  ship: ShipDto
  player: {
    id: string
    name: string
    hexColor: string
  }
}

@Injectable({
  providedIn: 'root',
})
export class ShipService extends RestClientList<ShipDto, string> {
  public static readonly IdField = 'id'
  private readonly baseApiUrl = `${environment.apiBaseUrl}/ships`

  constructor(private readonly httpClient: HttpClient) {
    super(`${environment.apiBaseUrl}/ships`, httpClient, {
      idField: ShipService.IdField,
    })
  }

  public createPlayerControlledShip(
    request: CreatePlayerControlledShipRequest
  ): Observable<CreatePlayerControlledShipResponse> {
    return this.httpClient.post<CreatePlayerControlledShipResponse>(
      `${this.baseApiUrl}/player-controlled`,
      request
    )
  }

  public createShipForExistingPlayer(
    request: CreateShipForExistingPlayerRequest
  ): Observable<CreatePlayerControlledShipResponse> {
    return this.httpClient.post<CreatePlayerControlledShipResponse>(
      `${this.baseApiUrl}/player-controlled/existing`,
      request
    )
  }

  public listShips(filter?: { islandId?: string; playerId?: string }): Observable<ShipDto[]> {
    let params = new HttpParams()
    if (filter?.islandId) {
      params = params.set('islandId', filter.islandId)
    }
    if (filter?.playerId) {
      params = params.set('playerId', filter.playerId)
    }
    return this.httpClient.get<ShipDto[]>(this.baseApiUrl, { params })
  }

  public startJourney(shipId: string, request: StartJourneyRequest): Observable<ShipDto> {
    return this.httpClient.post<ShipDto>(`${this.baseApiUrl}/${shipId}/journeys`, request)
  }

  public buyFromIsland(shipId: string, request: TradeRequest): Observable<ShipDto> {
    return this.httpClient.post<ShipDto>(`${this.baseApiUrl}/${shipId}/trade/buy`, request)
  }

  public sellToIsland(shipId: string, request: TradeRequest): Observable<ShipDto> {
    return this.httpClient.post<ShipDto>(`${this.baseApiUrl}/${shipId}/trade/sell`, request)
  }
}
