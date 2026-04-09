import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core'

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms'
import { Router } from '@angular/router'
import { MatFormFieldModule } from '@angular/material/form-field'
import { MatInputModule } from '@angular/material/input'
import { MatButtonModule } from '@angular/material/button'
import { MatCardModule } from '@angular/material/card'
import { ShipService } from '../../ship/ship.service'
import { ElderSinglePaneWrapperComponent } from '@elderbyte/ngx-starter'
import { MatIcon } from '@angular/material/icon'
import { MatRadioModule } from '@angular/material/radio'
import { MatSelectModule } from '@angular/material/select'
import { PlayerService } from '../player.service'
import { PlayerDto } from '../../../models/player.dto'
import { finalize } from 'rxjs'

interface CreatedPlayerControlledShipViewModel {
  ship: {
    id: string
    name: string
    playerId: string | null
  }
  player: {
    id: string
    name: string
    hexColor: string
  }
}

type PlayerMode = 'create' | 'existing'

@Component({
  selector: 'app-player',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    ElderSinglePaneWrapperComponent,
    MatIcon,
    MatRadioModule,
    MatSelectModule,
  ],
  templateUrl: './player.html',
  styleUrl: './player.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Player implements OnInit {
  public created: CreatedPlayerControlledShipViewModel | null = null
  public errorMessage: string | null = null
  public isSubmitting = false
  public isLoadingPlayers = false
  public players: PlayerDto[] = []

  public readonly modes: Record<PlayerMode, PlayerMode> = {
    create: 'create',
    existing: 'existing',
  }

  public readonly form = this.formBuilder.nonNullable.group({
    mode: this.formBuilder.nonNullable.control<PlayerMode>(this.modes.create, {
      validators: [Validators.required],
    }),
    shipName: [''],
    playerName: [''],
    playerId: [''],
  })

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly shipService: ShipService,
    private readonly playerService: PlayerService,
    private readonly router: Router
  ) {}

  public ngOnInit(): void {
    this.applyModeValidators(this.form.controls.mode.value)
    this.form.controls.mode.valueChanges.subscribe((mode) => this.applyModeValidators(mode))
    this.loadPlayers()
  }

  public submit(): void {
    if (this.form.invalid || this.isSubmitting) {
      this.form.markAllAsTouched()
      return
    }

    this.isSubmitting = true
    this.errorMessage = null
    this.created = null

    const mode = this.form.controls.mode.value
    const raw = this.form.getRawValue()
    if (mode === this.modes.existing) {
      const selectedPlayer = this.players.find((player) => player.id === raw.playerId)
      if (!selectedPlayer) {
        this.errorMessage = 'Selected player could not be found.'
        this.isSubmitting = false
        return
      }

      this.playerService.setSelectedPlayerId(selectedPlayer.id)
      this.created = {
        player: selectedPlayer,
        ship: {
          id: '-',
          name: 'No new ship created',
          playerId: selectedPlayer.id,
        },
      }
      this.isSubmitting = false
      this.router.navigateByUrl('/ship-browser')
      return
    }

    this.shipService
      .createPlayerControlledShip({
        shipName: raw.shipName,
        playerName: raw.playerName,
      })
      .subscribe({
        next: (created) => {
          this.playerService.setSelectedPlayerId(created.player.id)
          this.created = created
          this.isSubmitting = false
          this.router.navigateByUrl('/ship-browser')
        },
        error: (error) => {
          this.errorMessage = error?.error?.message ?? 'Could not create player-controlled ship.'
          this.isSubmitting = false
        },
      })
  }

  private loadPlayers(): void {
    this.isLoadingPlayers = true
    this.playerService
      .listPlayers()
      .pipe(finalize(() => (this.isLoadingPlayers = false)))
      .subscribe({
        next: (players) => {
          this.players = players
          const selected = this.playerService.getSelectedPlayerId()
          if (selected && players.some((player) => player.id === selected)) {
            this.form.controls.playerId.setValue(selected)
            this.form.controls.mode.setValue(this.modes.existing)
          }
        },
        error: () => {
          this.errorMessage = 'Could not load existing players.'
        },
      })
  }

  private applyModeValidators(mode: PlayerMode): void {
    if (mode === this.modes.existing) {
      this.form.controls.playerId.setValidators([Validators.required])
      this.form.controls.shipName.clearValidators()
      this.form.controls.playerName.clearValidators()
    } else {
      this.form.controls.shipName.setValidators([Validators.required, Validators.minLength(2)])
      this.form.controls.playerName.setValidators([Validators.required, Validators.minLength(2)])
      this.form.controls.playerId.clearValidators()
    }

    this.form.controls.shipName.updateValueAndValidity({ emitEvent: false })
    this.form.controls.playerName.updateValueAndValidity({ emitEvent: false })
    this.form.controls.playerId.updateValueAndValidity({ emitEvent: false })
  }
}
