import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { PropertyService, Property } from '../../core/services/property/property.service';
import { BlockService, Block } from '../../core/services/block/block.service';
import { BlockFormComponent } from '../blocks/block-form.component';

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
    MatCardModule
  ],
  template: `
    <div class="p-6 max-w-7xl mx-auto">
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
              <button mat-icon-button color="primary" (click)="openBlockForm(block)">
                <mat-icon>edit</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="hover:bg-gray-50 transition-colors"></tr>

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
    </div>
  `
})
export class PropertyDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private propertyService = inject(PropertyService);
  private blockService = inject(BlockService);
  private dialog = inject(MatDialog);

  property = signal<Property | null>(null);
  blocks = signal<Block[]>([]);
  isLoadingBlocks = signal(true);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);

  displayedColumns: string[] = ['name', 'numberOfFloors', 'description', 'actions'];

  private propertyId!: string;

  ngOnInit() {
    this.propertyId = this.route.snapshot.paramMap.get('id')!;
    this.loadProperty();
    this.loadBlocks();
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
