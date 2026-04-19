import { HttpClient } from '@angular/common/http'
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  OnInit,
  signal,
} from '@angular/core'
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
import { MatButtonModule } from '@angular/material/button'
import { MatCardModule } from '@angular/material/card'
import { MatIconModule } from '@angular/material/icon'
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { finalize, Observable } from 'rxjs'
import {
  AiConnectorRequestBody,
  textFromAiConnectorResponse,
} from '../../../models/ai-connector.dto'
import { IslandDto } from '../../../models/island.dto'
import { IslandService } from '../../island/island.service'
import { LoggerFactory } from '@elderbyte/ts-logger'

const DIALOG_AI_SERVICE_URL = 'https://rtnt-ai-connector.nw1n.workers.dev/'

@Component({
  selector: 'app-tavern',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    ElderSinglePaneWrapperComponent,
  ],
  templateUrl: './tavern.html',
  styleUrl: './tavern.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Tavern implements OnInit {
  private readonly log = LoggerFactory.getLogger(this.constructor.name)
  private readonly http = inject(HttpClient)
  private readonly islandService = inject(IslandService)
  private readonly destroyRef = inject(DestroyRef)

  protected readonly infoRequested = signal<Date | null>(null)
  protected readonly islands = signal<IslandDto[] | null>(null)
  protected readonly loading = signal(false)
  protected readonly dialogLoading = signal(false)
  protected readonly error = signal<string | null>(null)

  protected readonly dialogText = signal<string | null>(null)

  /** Shown while island or tavern-keeper (AI) news is loading. */
  protected readonly newsLoadingMessage =
    'Ah, you are curious about the latest news of the Caribbean? Let me think…'

  protected readonly isNewsLoading = computed(() => this.loading() || this.dialogLoading())

  constructor() {
    effect(() => {
      const islands = this.islands()
      if (islands) {
        this.log.info('Islands:', islands)
      }
      if (!this.islands()?.length) {
        return
      }
      this.fetchDialogText()
    })
  }

  private getDialogPromptRules(): string {
    return `
You are an old sailor sitting in a smoky tavern in the Caribbean during the golden age of piracy.

    Your job is to gossip about the current state of the Caribbean based on the world data provided.
    
    Speak like a rough sailor telling rumors over rum. The tone should feel like tavern gossip or pirate rumors.
    
    Rules:
    - Write SHORT lines (1 sentence each)
    - Generate exactly 3 rumors
    - Focus on interesting things such as:
      - islands that are very rich
      - goods that have unusually high or low prices
    - Mention island names and goods when possible
    - Use sailor/pirate style language (arr, mate, rum, gold, merchants, captains, etc.)
    - Do NOT explain the data
    - Do NOT output JSON
    - Separate each rumor with two newlines
    
    WORLD DATA:
`
  }

  private fetchDialogText(): void {
    const islands = this.islands()
    if (!islands?.length) {
      return
    }
    const lines = islands.map(
      (i) =>
        `${i.name}: rum ${i.tradePrices.rum}, sugar ${i.tradePrices.sugar}, spices ${i.tradePrices.spices}, tobacco ${i.tradePrices.tobacco}`
    )
    const prompt = this.getDialogPromptRules() + '\n\n' + lines.join('\n')

    this.dialogLoading.set(true)
    this.fetchAiConnectorContents(prompt)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.dialogLoading.set(false))
      )
      .subscribe({
        next: (response) => {
          const text = textFromAiConnectorResponse(response) ?? JSON.stringify(response)
          this.dialogText.set(text)
        },
        error: (error: { error?: { message?: string } }) => {
          this.dialogText.set(error?.error?.message ?? 'AI connector request failed.')
        },
      })
  }

  public fetchAiConnectorContents(prompt: string): Observable<unknown> {
    const body: AiConnectorRequestBody = {
      contents: [{ parts: [{ text: prompt }] }],
    }
    const model = 'gemini-flash-latest'
    const url = new URL(DIALOG_AI_SERVICE_URL)
    url.searchParams.set('model', model)
    return this.http.post<unknown>(url.toString(), body, {
      headers: { 'Content-Type': 'application/json' },
    })
  }

  protected submitInfoRequest(): void {
    if (this.infoRequested()) {
      return
    }
    this.infoRequested.set(new Date())
    this.dialogText.set(null)
    this.error.set(null)
    this.fetchAllIslandPrices()
  }

  public ngOnInit(): void {
    //this.fetchAllIslandPrices()
  }

  public fetchAllIslandPrices(): void {
    this.loading.set(true)
    this.error.set(null)
    this.islandService
      .listIslands()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (rows) => this.islands.set(rows),
        error: (error: { error?: { message?: string }; message?: string }) => {
          this.error.set(error?.error?.message ?? 'Could not load island prices.')
        },
      })
  }

  public totalTradePrices(island: IslandDto): number {
    const p = island.tradePrices
    return p.rum + p.sugar + p.spices + p.tobacco
  }
}
