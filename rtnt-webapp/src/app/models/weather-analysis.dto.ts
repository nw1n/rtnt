export interface WeatherAnalysisDto {
  fromTick: number
  toTick: number
  startTemperature: number
  endTemperature: number
  averageTemperature: number
  minTemperature: number
  maxTemperature: number
  changeCount: number
  largeChangeCount: number
  reversals: number
  burstCount: number
  largeDeltaThreshold: number
  burstWindowTicks: number
}
