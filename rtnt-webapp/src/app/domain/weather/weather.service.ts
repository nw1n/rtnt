import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { WeatherDto } from '../../models/weather.dto'
import { WeatherSampleDto } from '../../models/weather-sample.dto'
import { environment } from '../../../environments/environment'

@Injectable({
  providedIn: 'root',
})
export class WeatherService {
  private readonly baseApiUrl = `${environment.apiBaseUrl}/weather`

  constructor(private readonly httpClient: HttpClient) {}

  public get(): Observable<WeatherDto> {
    return this.httpClient.get<WeatherDto>(this.baseApiUrl)
  }

  public history(): Observable<WeatherSampleDto[]> {
    return this.httpClient.get<WeatherSampleDto[]>(`${this.baseApiUrl}/history`)
  }
}
