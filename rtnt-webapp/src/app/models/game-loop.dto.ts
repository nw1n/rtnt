export interface GameLoopDto {
  tick: number
  mode: 'LIVE' | 'BATCH'
  paused: boolean
}
