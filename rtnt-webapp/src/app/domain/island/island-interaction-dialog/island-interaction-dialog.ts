import { ChangeDetectionStrategy, Component, Inject, OnInit } from '@angular/core'
import { CommonModule } from '@angular/common'
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms'
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog'
import { MatButtonModule } from '@angular/material/button'
import { finalize } from 'rxjs'
import { IslandDto } from '../../../models/island.dto'
import { ShipDto } from '../../../models/ship.dto'
import { ShipService } from '../../ship/ship.service'
import { PlayerService } from '../../player/player.service'

interface IslandInteractionDialogData {
  island: IslandDto
}

@Component({
  selector: 'app-island-interaction-dialog',
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule],
  templateUrl: './island-interaction-dialog.html',
  styleUrl: './island-interaction-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IslandInteractionDialog implements OnInit {
  public isLoading = false
  public isSubmitting = false
  public errorMessage: string | null = null
  public playerShips: ShipDto[] = []
  private selectedShip: ShipDto | null = null

  public readonly form = this.formBuilder.nonNullable.group({
    targetIslandId: [this.data.island.id, Validators.required],
  })

  constructor(
    @Inject(MAT_DIALOG_DATA) public readonly data: IslandInteractionDialogData,
    private readonly formBuilder: FormBuilder,
    private readonly playerService: PlayerService,
    private readonly shipService: ShipService,
    public readonly dialogRef: MatDialogRef<IslandInteractionDialog>
  ) {}

  public ngOnInit(): void {
    this.loadData()
  }

  public submit(): void {
    if (this.form.invalid || this.isSubmitting) {
      this.form.markAllAsTouched()
      return
    }

    const selectedPlayerId = this.playerService.getSelectedPlayerId()
    if (!selectedPlayerId) {
      this.errorMessage = 'Please select or create a player first.'
      return
    }

    this.isSubmitting = true
    this.errorMessage = null
    const raw = this.form.getRawValue()
    const selectedShip = this.selectedShip
    if (!selectedShip) {
      this.errorMessage = 'No available player ship.'
      this.isSubmitting = false
      return
    }

    this.shipService
      .startJourney(selectedShip.id, {
        playerId: selectedPlayerId,
        targetIslandId: raw.targetIslandId,
      })
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: () => this.dialogRef.close(true),
        error: (error) => {
          this.errorMessage = error?.error?.message ?? 'Could not start journey.'
        },
      })
  }

  private loadData(): void {
    const selectedPlayerId = this.playerService.getSelectedPlayerId()
    if (!selectedPlayerId) {
      this.errorMessage = 'Please select or create a player first.'
      return
    }

    this.isLoading = true
    this.errorMessage = null

    this.shipService
      .listShips({
        playerId: selectedPlayerId,
      })
      .subscribe({
        next: (ships) => {
          this.playerShips = ships.filter((ship) => !ship.journey?.active && !!ship.islandId)
          this.selectedShip = this.playerShips[0] ?? null
          if (!this.selectedShip) {
            this.errorMessage = 'No available player ship.'
            this.isLoading = false
            return
          }
          this.isLoading = false
        },
        error: (error) => {
          this.errorMessage = error?.error?.message ?? 'Could not load ships for this island.'
          this.isLoading = false
        },
      })
  }
}
