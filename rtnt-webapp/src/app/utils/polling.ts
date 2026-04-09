import { EMPTY, Observable, timer } from 'rxjs'
import { catchError, exhaustMap } from 'rxjs/operators'

const POLLING_INTERVAL_MS = 1000

export function pollEverySecond<T>(request: () => Observable<T>): Observable<T> {
  return timer(0, POLLING_INTERVAL_MS).pipe(
    exhaustMap(() => request()),
    catchError(() => {
      console.log('An error occurred')
      return EMPTY
    })
  )
}
