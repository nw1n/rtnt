import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { WorldSnapshotDto } from '../../models/world-snapshot.dto'
import { environment } from '../../../environments/environment'

@Injectable({
  providedIn: 'root',
})
export class WorldSnapshotService {
  private readonly baseApiUrl = `${environment.apiBaseUrl}/world-snapshots`

  constructor(private readonly httpClient: HttpClient) {}

  public listSnapshots(): Observable<WorldSnapshotDto[]> {
    return this.httpClient.get<WorldSnapshotDto[]>(this.baseApiUrl)
  }
}
