export interface GameFlowDto {
  tick: number
  mode: 'LIVE' | 'BATCH'
  paused: boolean
  liveIntervalMs: number
}
