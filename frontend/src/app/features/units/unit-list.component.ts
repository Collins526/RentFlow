import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  UnitService,
  Unit,
  OccupancyStatus,
  ASSIGNABLE_OCCUPANCY_STATUSES,
  formatEnumLabel
} from '../../core/services/unit/unit.service';
import { UnitFormComponent } from './unit-form.component';
import { BulkUnitFormComponent } from './bulk-unit-form.component';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog.component';

@Component({
  selector: 'app-unit-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatSelectModule,
    MatFormFieldModule,
    MatDialogModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="flex flex-wrap justify-between items-center gap-3 mb-6">
      <div>
        <h2 class="text-xl font-semibold text-gray-900">Units</h2>
        <p class="text-gray-500 text-sm">Individual rentable spaces.</p>
      </div>

      <div class="flex items-center gap-3">
        <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-52">
          <mat-label>Occupancy</mat-label>
          <mat-select [value]="occupancyFilter()" (valueChange)="applyFilter($event)">
            <mat-option [value]="null">All units</mat-option>
            <mat-option *ngFor="let status of allStatuses" [value]="status">{{ label(status) }}</mat-option>
          </mat-select>
        </mat-form-field>

        <button mat-stroked-button (click)="openBulkForm()">
          <mat-icon>library_add</mat-icon> Bulk Add
        </button>
        <button mat-flat-button color="primary" class="!px-6 !py-6" (click)="openUnitForm()">
          <mat-icon>add</mat-icon> Add Unit
        </button>
      </div>
    </div>

    <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
      <div *ngIf="isLoading()" class="flex justify-center p-12">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <table *ngIf="!isLoading()" mat-table [dataSource]="units()" class="w-full">
        <ng-container matColumnDef="unitNumber">
          <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Unit </th>
          <td mat-cell *matCellDef="let unit" class="font-medium text-gray-900"> {{ unit.unitNumber }} </td>
        </ng-container>

        <ng-container matColumnDef="type">
          <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Type </th>
          <td mat-cell *matCellDef="let unit" class="text-gray-600"> {{ label(unit.type) }} </td>
        </ng-container>

        <ng-container matColumnDef="blockName">
          <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Block </th>
          <td mat-cell *matCellDef="let unit" class="text-gray-600"> {{ unit.blockName || '—' }} </td>
        </ng-container>

        <ng-container matColumnDef="floorNumber">
          <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Floor </th>
          <td mat-cell *matCellDef="let unit" class="text-gray-600">
            {{ unit.floorNumber !== null ? unit.floorNumber : '—' }}
          </td>
        </ng-container>

        <ng-container matColumnDef="rentAmount">
          <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Rent </th>
          <td mat-cell *matCellDef="let unit" class="text-gray-900 font-medium">
            {{ unit.rentAmount | currency:'KES ':'symbol':'1.0-0' }}
          </td>
        </ng-container>

        <ng-container matColumnDef="occupancyStatus">
          <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Occupancy </th>
          <td mat-cell *matCellDef="let unit">
            <span class="px-2.5 py-1 rounded-full text-xs font-medium"
                  [ngClass]="{
                    'bg-green-100 text-green-800': unit.occupancyStatus === 'VACANT',
                    'bg-blue-100 text-blue-800': unit.occupancyStatus === 'OCCUPIED',
                    'bg-amber-100 text-amber-800': unit.occupancyStatus === 'RESERVED',
                    'bg-orange-100 text-orange-800': unit.occupancyStatus === 'UNDER_MAINTENANCE'
                  }">
              {{ label(unit.occupancyStatus) }}
            </span>
          </td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let unit" class="text-right">
            <button mat-icon-button color="primary" (click)="openUnitForm(unit)" aria-label="Edit unit">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button [matMenuTriggerFor]="menu" aria-label="More actions">
              <mat-icon>more_vert</mat-icon>
            </button>
            <mat-menu #menu="matMenu">
              <button mat-menu-item
                      *ngFor="let status of assignableStatuses"
                      [disabled]="unit.occupancyStatus === 'OCCUPIED' || unit.occupancyStatus === status"
                      (click)="changeOccupancy(unit, status)">
                Mark as {{ label(status) }}
              </button>
              <button mat-menu-item
                      class="!text-red-600"
                      [disabled]="unit.occupancyStatus === 'OCCUPIED'"
                      (click)="confirmDelete(unit)">
                <mat-icon class="!text-red-600">delete</mat-icon>
                <span>Delete unit</span>
              </button>
            </mat-menu>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="hover:bg-gray-50 transition-colors"></tr>

        <tr class="mat-row" *matNoDataRow>
          <td class="mat-cell text-center py-12 text-gray-500" [attr.colspan]="displayedColumns.length">
            {{ occupancyFilter()
                ? 'No units match this occupancy filter.'
                : "No units yet. Use 'Add Unit' or 'Bulk Add' to create them." }}
          </td>
        </tr>
      </table>

      <mat-paginator
        *ngIf="!isLoading()"
        [length]="totalElements()"
        [pageSize]="pageSize()"
        [pageIndex]="pageIndex()"
        [pageSizeOptions]="[10, 25, 50]"
        (page)="handlePageEvent($event)"
        aria-label="Select page">
      </mat-paginator>
    </div>
  `
})
export class UnitListComponent implements OnInit {
  private unitService = inject(UnitService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  propertyId = input.required<string>();
  /** When set, the list is scoped to a single block. */
  blockId = input<string | undefined>(undefined);

  /** Emitted whenever the underlying data changed, so parents can refresh summaries. */
  unitsChanged = output<void>();

  units = signal<Unit[]>([]);
  isLoading = signal(true);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);
  occupancyFilter = signal<OccupancyStatus | null>(null);

  allStatuses: OccupancyStatus[] = ['VACANT', 'RESERVED', 'OCCUPIED', 'UNDER_MAINTENANCE'];
  assignableStatuses = ASSIGNABLE_OCCUPANCY_STATUSES;
  label = formatEnumLabel;

  get displayedColumns(): string[] {
    // Inside a block view the Block column is redundant.
    return this.blockId()
      ? ['unitNumber', 'type', 'floorNumber', 'rentAmount', 'occupancyStatus', 'actions']
      : ['unitNumber', 'type', 'blockName', 'floorNumber', 'rentAmount', 'occupancyStatus', 'actions'];
  }

  ngOnInit() {
    this.loadUnits();
  }

  loadUnits() {
    this.isLoading.set(true);

    const blockId = this.blockId();
    const request$ = blockId
      ? this.unitService.getUnitsByBlockId(blockId, this.pageIndex(), this.pageSize())
      : this.unitService.getUnitsByPropertyId(
          this.propertyId(), this.pageIndex(), this.pageSize(), this.occupancyFilter());

    request$.subscribe({
      next: (res) => {
        if (res.success) {
          this.units.set(res.data.content);
          this.totalElements.set(res.data.totalElements);
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  applyFilter(status: OccupancyStatus | null) {
    this.occupancyFilter.set(status);
    this.pageIndex.set(0);
    this.loadUnits();
  }

  handlePageEvent(e: PageEvent) {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
    this.loadUnits();
  }

  openUnitForm(unit?: Unit) {
    const dialogRef = this.dialog.open(UnitFormComponent, {
      width: '700px',
      data: { propertyId: this.propertyId(), unit, blockId: this.blockId() },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.refresh();
      }
    });
  }

  openBulkForm() {
    const dialogRef = this.dialog.open(BulkUnitFormComponent, {
      width: '760px',
      data: { propertyId: this.propertyId(), blockId: this.blockId() },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.refresh();
      }
    });
  }

  changeOccupancy(unit: Unit, status: OccupancyStatus) {
    this.unitService.updateOccupancyStatus(unit.id, status).subscribe({
      next: () => {
        this.snackBar.open(`Unit ${unit.unitNumber} marked as ${this.label(status)}`, 'Close', {
          duration: 3000, panelClass: ['bg-green-600', 'text-white']
        });
        this.refresh();
      },
      error: (err) => {
        this.snackBar.open(err?.error?.message || 'Failed to update occupancy', 'Close', {
          duration: 4000, panelClass: ['bg-red-600', 'text-white']
        });
      }
    });
  }

  confirmDelete(unit: Unit) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '440px',
      data: {
        title: `Delete unit ${unit.unitNumber}?`,
        message: 'The unit is archived rather than erased, so its history stays intact. It will no longer appear in listings.',
        confirmLabel: 'Delete',
        destructive: true
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.unitService.deleteUnit(unit.id).subscribe({
        next: () => {
          this.snackBar.open(`Unit ${unit.unitNumber} deleted`, 'Close', {
            duration: 3000, panelClass: ['bg-green-600', 'text-white']
          });
          this.refresh();
        },
        error: (err) => {
          this.snackBar.open(err?.error?.message || 'Failed to delete unit', 'Close', {
            duration: 4000, panelClass: ['bg-red-600', 'text-white']
          });
        }
      });
    });
  }

  private refresh() {
    this.loadUnits();
    this.unitsChanged.emit();
  }
}
