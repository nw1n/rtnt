export interface Environment {
  version: string
  apiBaseUrl: string
}

type RuntimeEnv = {
  version?: string
  API_BASE_URL?: string
}

const runtimeEnv: RuntimeEnv = (window as any).__env || {}

export const environment: Environment = {
  version: runtimeEnv.version ?? 'local',
  apiBaseUrl: runtimeEnv.API_BASE_URL ?? 'http://localhost:18080/api',
}
