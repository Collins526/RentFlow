import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { PropertyService, Property } from '../../core/services/property/property.service';
import { BlockService, Block } from '../../core/services/block/block.service';
import { UnitService, UnitSummary } from '../../core/services/unit/unit.service';
import { BlockFormComponent } from '../blocks/block-form.component';
import { UnitListComponent } from '../units/unit-list.component';

@Component({
  selector: 'app-property-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatCardModule,
    UnitListComponent
  ],
  template: `
    <div>
      <!-- Back Button -->
      <button mat-button routerLink="/properties" class="!mb-4 !text-gray-600">
        <mat-icon>arrow_back</mat-icon> Back to Properties
      </button>

      <!-- Property Header -->
      <div *ngIf="property()" class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">{{ property()!.name }}</h1>
        <p class="text-gray-600 mt-1">{{ property()!.address }}</p>
        <div class="flex gap-3 mt-3">
          <span class="px-2.5 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
            {{ property()!.type | titlecase }}
          </span>
          <span class="px-2.5 py-1 rounded-full text-xs font-medium"
                [ngClass]="{
                  'bg-green-100 text-green-800': property()!.status === 'ACTIVE',
                  'bg-gray-100 text-gray-800': property()!.status === 'INACTIVE',
                  'bg-orange-100 text-orange-800': property()!.status === 'UNDER_CONSTRUCTION'
                }">
            {{ property()!.status | titlecase }}
          </span>
        </div>

        <!-- Occupancy roll-up -->
        <div *ngIf="summary() as stats" class="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-6">
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 px-5 py-4">
            <p class="text-xs uppercase tracking-wide text-gray-500 font-semibold">Total Units</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.totalUnits }}</p>
          </div>
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 px-5 py-4">
            <p class="text-xs uppercase tracking-wide text-gray-500 font-semibold">Occupied</p>
            <p class="text-2xl font-bold text-blue-700 mt-1">{{ stats.occupied }}</p>
          </div>
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 px-5 py-4">
            <p class="text-xs uppercase tracking-wide text-gray-500 font-semibold">Vacant</p>
            <p class="text-2xl font-bold text-green-700 mt-1">{{ stats.vacant }}</p>
          </div>
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 px-5 py-4">
            <p class="text-xs uppercase tracking-wide text-gray-500 font-semibold">Potential Rent</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">
              {{ stats.totalPotentialRent | currency:'KES ':'symbol':'1.0-0' }}
            </p>
            <p class="text-xs text-gray-500 mt-0.5">per month at full occupancy</p>
          </div>
        </div>
      </div>

      <!-- Blocks Section -->
      <div class="flex justify-between items-center mb-6">
        <div>
          <h2 class="text-xl font-semibold text-gray-900">Blocks</h2>
          <p class="text-gray-500 text-sm">Buildings and wings within this property.</p>
        </div>
        <button mat-flat-button color="primary" class="!px-6 !py-6" (click)="openBlockForm()">
          <mat-icon>add</mat-icon> Add Block
        </button>
      </div>

      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <div *ngIf="isLoadingBlocks()" class="flex justify-center p-12">
          <mat-spinner diameter="40"></mat-spinner>
        </div>

        <table *ngIf="!isLoadingBlocks()" mat-table [dataSource]="blocks()" class="w-full">
          <!-- Name Column -->
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Name </th>
            <td mat-cell *matCellDef="let block" class="font-medium text-gray-900"> {{ block.name }} </td>
          </ng-container>

          <!-- Floors Column -->
          <ng-container matColumnDef="numberOfFloors">
            <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Floors </th>
            <td mat-cell *matCellDef="let block"> {{ block.numberOfFloors }} </td>
          </ng-container>

          <!-- Description Column -->
          <ng-container matColumnDef="description">
            <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Description </th>
            <td mat-cell *matCellDef="let block" class="text-gray-600 max-w-xs truncate"> 
              {{ block.description || '—' }} 
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let block" class="text-right">
              <button mat-icon-button (click)="navigateToBlock(block); $event.stopPropagation()"
                      aria-label="View block units">
                <mat-icon>visibility</mat-icon>
              </button>
              <button mat-icon-button color="primary" (click)="openBlockForm(block); $event.stopPropagation()">
                <mat-icon>edit</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"
              class="hover:bg-gray-50 transition-colors cursor-pointer"
              (click)="navigateToBlock(row)"></tr>

          <tr class="mat-row" *matNoDataRow>
            <td class="mat-cell text-center py-12 text-gray-500" colspan="4">
              No blocks found. Click 'Add Block' to subdivide this property.
            </td>
          </tr>
        </table>

        <mat-paginator
          *ngIf="!isLoadingBlocks()"
          [length]="totalElements()"
          [pageSize]="pageSize()"
          [pageSizeOptions]="[10, 25, 50]"
          (page)="handlePageEvent($event)"
          aria-label="Select page">
        </mat-paginator>
      </div>

      <!-- Units Section -->
      <div class="mt-12">
        <app-unit-list [propertyId]="propertyId" (unitsChanged)="loadSummary()"></app-unit-list>
      </div>
    </div>
  `
})
export class PropertyDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private propertyService = inject(PropertyService);
  private blockService = inject(BlockService);
  private unitService = inject(UnitService);
  private dialog = inject(MatDialog);

  property = signal<Property | null>(null);
  blocks = signal<Block[]>([]);
  summary = signal<UnitSummary | null>(null);
  isLoadingBlocks = signal(true);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);

  displayedColumns: string[] = ['name', 'numberOfFloors', 'description', 'actions'];

  propertyId!: string;

  ngOnInit() {
    this.propertyId = this.route.snapshot.paramMap.get('id')!;
    this.loadProperty();
    this.loadBlocks();
    this.loadSummary();
  }

  loadSummary() {
    this.unitService.getPropertyUnitSummary(this.propertyId).subscribe({
      next: (res) => {
        if (res.success) {
          this.summary.set(res.data);
        }
      }
    });
  }

  navigateToBlock(block: Block) {
    this.router.navigate(['/blocks', block.id]);
  }

  loadProperty() {
    this.propertyService.getPropertyById(this.propertyId).subscribe({
      next: (res) => {
        if (res.success) {
          this.property.set(res.data);
        }
      }
    });
  }

  loadBlocks() {
    this.isLoadingBlocks.set(true);
    this.blockService.getBlocksByPropertyId(this.propertyId, this.pageIndex(), this.pageSize()).subscribe({
      next: (res) => {
        if (res.success) {
          this.blocks.set(res.data.content);
          this.totalElements.set(res.data.totalElements);
        }
        this.isLoadingBlocks.set(false);
      },
      error: () => {
        this.isLoadingBlocks.set(false);
      }
    });
  }

  handlePageEvent(e: PageEvent) {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
    this.loadBlocks();
  }

  openBlockForm(block?: Block) {
    const dialogRef = this.dialog.open(BlockFormComponent, {
      width: '500px',
      data: { propertyId: this.propertyId, block },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadBlocks();
      }
    });
  }
}
