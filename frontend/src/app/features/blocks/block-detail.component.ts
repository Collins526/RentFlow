import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BlockService, Block } from '../../core/services/block/block.service';
import { UnitListComponent } from '../units/unit-list.component';

@Component({
  selector: 'app-block-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    UnitListComponent
  ],
  template: `
    <div>
      <div *ngIf="isLoading()" class="flex justify-center p-12">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <ng-container *ngIf="!isLoading() && block() as currentBlock">
        <button mat-button [routerLink]="['/properties', currentBlock.propertyId]" class="!mb-4 !text-gray-600">
          <mat-icon>arrow_back</mat-icon> Back to Property
        </button>

        <div class="mb-8">
          <h1 class="text-3xl font-bold text-gray-900">{{ currentBlock.name }}</h1>
          <p class="text-gray-600 mt-1">{{ currentBlock.description || 'No description provided.' }}</p>
          <span class="inline-block mt-3 px-2.5 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
            {{ currentBlock.numberOfFloors }} {{ currentBlock.numberOfFloors === 1 ? 'floor' : 'floors' }}
          </span>
        </div>

        <app-unit-list [propertyId]="currentBlock.propertyId" [blockId]="currentBlock.id"></app-unit-list>
      </ng-container>

      <div *ngIf="!isLoading() && !block()" class="text-center py-16 text-gray-500">
        Block not found.
      </div>
    </div>
  `
})
export class BlockDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private blockService = inject(BlockService);

  block = signal<Block | null>(null);
  isLoading = signal(true);

  ngOnInit() {
    const blockId = this.route.snapshot.paramMap.get('id')!;

    this.blockService.getBlockById(blockId).subscribe({
      next: (res) => {
        if (res.success) {
          this.block.set(res.data);
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }
}
