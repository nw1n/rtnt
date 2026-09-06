import { Injectable } from '@angular/core'
import { HttpClient, HttpParams } from '@angular/common/http'
import { Observable } from 'rxjs'
import { WeatherAnalysisDto } from '../../models/weather-analysis.dto'
import { WeatherDto } from '../../models/weather.dto'
import { WeatherSampleDto } from '../../models/weather-sample.dto'
import { environment } from '../../../environments/environment'

export interface WeatherRange {
  fromTick?: number | null
  toTick?: number | null
}

@Injectable({
  providedIn: 'root',
})
export class WeatherService {
  private readonly baseApiUrl = `${environment.apiBaseUrl}/weather`

  constructor(private readonly httpClient: HttpClient) {}

  public get(): Observable<WeatherDto> {
    return this.httpClient.get<WeatherDto>(this.baseApiUrl)
  }

  public history(range: WeatherRange = {}): Observable<WeatherSampleDto[]> {
    return this.httpClient.get<WeatherSampleDto[]>(`${this.baseApiUrl}/history`, {
      params: this.rangeParams(range),
    })
  }

  public analysis(range: WeatherRange = {}): Observable<WeatherAnalysisDto> {
    return this.httpClient.get<WeatherAnalysisDto>(`${this.baseApiUrl}/analysis`, {
      params: this.rangeParams(range),
    })
  }

  private rangeParams(range: WeatherRange): HttpParams {
    let params = new HttpParams()
    if (range.fromTick != null) {
      params = params.set('fromTick', String(range.fromTick))
    }
    if (range.toTick != null) {
      params = params.set('toTick', String(range.toTick))
    }
    return params
  }
}
