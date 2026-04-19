/** Request body for the rtnt-ai-connector worker (Gemini-style). */
export interface AiConnectorRequestBody {
  contents: Array<{
    parts: Array<{ text: string }>
  }>
}

/** Best-effort parse of a typical Gemini-style JSON response. */
export function textFromAiConnectorResponse(response: unknown): string | null {
  if (!response || typeof response !== 'object') {
    return null
  }
  const candidates = (response as { candidates?: unknown }).candidates
  if (!Array.isArray(candidates) || candidates.length === 0) {
    return null
  }
  const first = candidates[0] as { content?: { parts?: unknown } }
  const parts = first.content?.parts
  if (!Array.isArray(parts) || parts.length === 0) {
    return null
  }
  const text = (parts[0] as { text?: unknown }).text
  return typeof text === 'string' ? text : null
}
