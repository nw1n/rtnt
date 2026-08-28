export interface ClockDto {
  tick: number
  mode: 'LIVE' | 'BATCH'
  paused: boolean
}
