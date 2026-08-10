import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { PropertyService, Property } from '../../core/services/property/property.service';
import { PropertyFormComponent } from './property-form.component';

@Component({
  selector: 'app-property-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatChipsModule
  ],
  template: `
    <div>
      <div class="flex justify-between items-center mb-8">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">Properties</h1>
          <p class="text-gray-600 mt-2">Manage your organization's properties.</p>
        </div>
        <button mat-flat-button color="primary" class="!px-6 !py-6" (click)="openPropertyForm()">
          <mat-icon>add</mat-icon> Add Property
        </button>
      </div>

      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        
        <div *ngIf="isLoading()" class="flex justify-center p-12">
          <mat-spinner diameter="40"></mat-spinner>
        </div>

        <table *ngIf="!isLoading()" mat-table [dataSource]="properties()" class="w-full">
          <!-- Name Column -->
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Name </th>
            <td mat-cell *matCellDef="let property" class="font-medium text-gray-900"> {{property.name}} </td>
          </ng-container>

          <!-- Type Column -->
          <ng-container matColumnDef="type">
            <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Type </th>
            <td mat-cell *matCellDef="let property"> 
              <mat-chip-set>
                <mat-chip [disableRipple]="true" class="!text-xs">
                  {{property.type}}
                </mat-chip>
              </mat-chip-set>
            </td>
          </ng-container>

          <!-- Address Column -->
          <ng-container matColumnDef="address">
            <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Address </th>
            <td mat-cell *matCellDef="let property" class="text-gray-600"> 
              {{property.address}}<br>
              <span class="text-xs text-gray-400">{{property.city}}, {{property.state}}</span>
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef class="font-semibold text-gray-600"> Status </th>
            <td mat-cell *matCellDef="let property"> 
              <span class="px-2.5 py-1 rounded-full text-xs font-medium"
                    [ngClass]="{
                      'bg-green-100 text-green-800': property.status === 'ACTIVE',
                      'bg-gray-100 text-gray-800': property.status === 'INACTIVE',
                      'bg-orange-100 text-orange-800': property.status === 'UNDER_CONSTRUCTION'
                    }">
                {{property.status | titlecase}}
              </span>
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let property" class="text-right">
              <button mat-icon-button (click)="navigateToProperty(property); $event.stopPropagation()">
                <mat-icon>visibility</mat-icon>
              </button>
              <button mat-icon-button color="primary" (click)="openPropertyForm(property); $event.stopPropagation()">
                <mat-icon>edit</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="hover:bg-gray-50 transition-colors cursor-pointer" (click)="navigateToProperty(row)"></tr>
          
          <tr class="mat-row" *matNoDataRow>
            <td class="mat-cell text-center py-12 text-gray-500" colspan="5">
              No properties found. Click 'Add Property' to get started.
            </td>
          </tr>
        </table>

        <mat-paginator 
          *ngIf="!isLoading()"
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
export class PropertyListComponent implements OnInit {
  private propertyService = inject(PropertyService);
  private dialog = inject(MatDialog);
  private router = inject(Router);

  properties = signal<Property[]>([]);
  isLoading = signal(true);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);

  displayedColumns: string[] = ['name', 'type', 'address', 'status', 'actions'];

  ngOnInit() {
    this.loadProperties();
  }

  loadProperties() {
    this.isLoading.set(true);
    this.propertyService.getAllProperties(this.pageIndex(), this.pageSize()).subscribe({
      next: (res) => {
        if (res.success) {
          this.properties.set(res.data.content);
          this.totalElements.set(res.data.totalElements);
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  handlePageEvent(e: PageEvent) {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
    this.loadProperties();
  }

  openPropertyForm(property?: Property) {
    const dialogRef = this.dialog.open(PropertyFormComponent, {
      width: '600px',
      data: { property },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadProperties();
      }
    });
  }

  navigateToProperty(property: Property) {
    this.router.navigate(['/properties', property.id]);
  }
}
