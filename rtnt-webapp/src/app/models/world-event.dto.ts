export interface WorldEventDto {
  tick: number
  type: string
  payload: Record<string, unknown>
}
