import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Property, PropertyService } from '../../core/services/property/property.service';
import { UnitListComponent } from './unit-list.component';

@Component({
  selector: 'app-unit-overview',
  standalone: true,
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    UnitListComponent
  ],
  template: `
    <div>
      <div class="mb-6">
        <h1 class="text-3xl font-bold text-gray-900">Units</h1>
        <p class="mt-2 text-gray-600">View and manage units by property.</p>
      </div>

      <div *ngIf="isLoading()" class="flex justify-center rounded-2xl border border-gray-100 bg-white p-12">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <ng-container *ngIf="!isLoading()">
        <div *ngIf="properties().length > 0" class="mb-6 flex flex-wrap items-center gap-4">
          <mat-form-field appearance="outline" subscriptSizing="dynamic" class="min-w-64">
            <mat-label>Property</mat-label>
            <mat-select [value]="selectedProperty()?.id" (selectionChange)="selectProperty($event.value)">
              <mat-option *ngFor="let property of properties()" [value]="property.id">
                {{ property.name }}
              </mat-option>
            </mat-select>
          </mat-form-field>

          <mat-paginator
            [length]="totalElements()"
            [pageSize]="pageSize()"
            [pageIndex]="pageIndex()"
            [pageSizeOptions]="[10, 25, 50]"
            (page)="handlePageEvent($event)"
            aria-label="Select property page">
          </mat-paginator>
        </div>

        <ng-container *ngIf="selectedProperty() as property">
          @for (unitProperty of [property]; track unitProperty.id) {
            <app-unit-list [propertyId]="unitProperty.id"></app-unit-list>
          }
        </ng-container>

        <div *ngIf="properties().length === 0" class="rounded-2xl border border-gray-100 bg-white p-8 text-center text-gray-600">
          No properties found. Add a property before managing its units.
        </div>
      </ng-container>
    </div>
  `
})
export class UnitOverviewComponent implements OnInit {
  private propertyService = inject(PropertyService);
  private snackBar = inject(MatSnackBar);

  properties = signal<Property[]>([]);
  selectedProperty = signal<Property | null>(null);
  isLoading = signal(true);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);

  ngOnInit(): void {
    this.loadProperties();
  }

  selectProperty(propertyId: string): void {
    const property = this.properties().find(item => item.id === propertyId);
    if (property) {
      this.selectedProperty.set(property);
    }
  }

  handlePageEvent(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadProperties();
  }

  private loadProperties(): void {
    this.isLoading.set(true);
    this.propertyService.getAllProperties(this.pageIndex(), this.pageSize()).subscribe({
      next: response => {
        if (!response.success) {
          this.snackBar.open(response.message || 'Failed to load properties', 'Close', { duration: 4000 });
          this.isLoading.set(false);
          return;
        }

        this.properties.set(response.data.content);
        this.totalElements.set(response.data.totalElements);
        this.selectedProperty.set(response.data.content[0] ?? null);
        this.isLoading.set(false);
      },
      error: error => {
        this.snackBar.open(error?.error?.message || 'Failed to load properties', 'Close', { duration: 4000 });
        this.isLoading.set(false);
      }
    });
  }
}
