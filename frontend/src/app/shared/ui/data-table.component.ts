import { Component, Directive, Input, TemplateRef, computed, contentChildren, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

export interface DataTableColumn<T = any> {
  /** Unique key, also used as the matColumnDef name. */
  key: string;
  header: string;
  /**
   * Cell text. Omit it and supply an `rfCell` template with the same key when the
   * cell needs markup rather than a string.
   */
  value?: (row: T) => string | number | null | undefined;
  cellClass?: string;
  headerClass?: string;
  align?: 'left' | 'right';
}

/**
 * Marks a template as the renderer for one column, addressed by key:
 * `<ng-template rfCell="status" let-row>…</ng-template>`.
 */
@Directive({ selector: '[rfCell]', standalone: true })
export class DataTableCellDirective {
  @Input({ required: true, alias: 'rfCell' }) key!: string;

  constructor(public template: TemplateRef<{ $implicit: any }>) {}
}

/**
 * Paginated table shared by the feature lists.
 *
 * Columns are declarative, but any column can opt out into an `rfCell` template,
 * so the component never has to grow a flag for one screen's formatting. Loading,
 * error and empty states live here so every list behaves the same way.
 */
@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatButtonModule
  ],
  template: `
    <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">

      <div *ngIf="error()" class="flex items-center gap-3 px-6 py-4 bg-red-50 border-b border-red-100">
        <mat-icon class="text-red-600">error_outline</mat-icon>
        <span class="text-sm text-red-800 flex-1">{{ error() }}</span>
        <button mat-stroked-button color="warn" (click)="retry.emit()">Retry</button>
      </div>

      <div *ngIf="loading()" class="flex flex-col items-center justify-center gap-3 py-16">
        <mat-spinner diameter="36"></mat-spinner>
        <p class="text-sm text-gray-500">{{ loadingLabel() }}</p>
      </div>

      <ng-container *ngIf="!loading()">
        <div *ngIf="isEmpty()" class="flex flex-col items-center gap-2 py-16 px-6 text-center">
          <mat-icon class="!h-10 !w-10 !text-4xl text-gray-300">{{ emptyIcon() }}</mat-icon>
          <p class="text-gray-900 font-medium">{{ emptyTitle() }}</p>
          <p *ngIf="emptyMessage()" class="text-sm text-gray-500 max-w-sm">{{ emptyMessage() }}</p>
        </div>

        <div *ngIf="!isEmpty()">
          <div class="overflow-x-auto">
            <table mat-table [dataSource]="rows()" class="w-full">
              <ng-container *ngFor="let column of columns()" [matColumnDef]="column.key">
                <th mat-header-cell *matHeaderCellDef
                    class="font-semibold text-gray-600"
                    [class.text-right]="column.align === 'right'"
                    [ngClass]="column.headerClass">
                  {{ column.header }}
                </th>
                <td mat-cell *matCellDef="let row"
                    [class.text-right]="column.align === 'right'"
                    [ngClass]="column.cellClass">
                  <ng-container *ngIf="cellTemplates().get(column.key) as tpl; else plainCell">
                    <ng-container
                      [ngTemplateOutlet]="tpl"
                      [ngTemplateOutletContext]="{ $implicit: row }">
                    </ng-container>
                  </ng-container>
                  <ng-template #plainCell>{{ column.value ? column.value(row) : '' }}</ng-template>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="columnKeys()"></tr>
              <tr mat-row *matRowDef="let row; columns: columnKeys()"
                  class="transition-colors hover:bg-gray-50"
                  [class.cursor-pointer]="rowClickable()"
                  (click)="onRowClick(row)"></tr>
            </table>
          </div>

          <mat-paginator
            *ngIf="showPaginator()"
            [length]="totalElements()"
            [pageSize]="pageSize()"
            [pageIndex]="pageIndex()"
            [pageSizeOptions]="pageSizeOptions()"
            (page)="page.emit($event)"
            aria-label="Select page">
          </mat-paginator>
        </div>
      </ng-container>
    </div>
  `
})
export class DataTableComponent<T = any> {
  columns = input.required<DataTableColumn<T>[]>();
  rows = input.required<T[]>();

  loading = input(false);
  loadingLabel = input('Loading…');
  error = input<string | null>(null);

  emptyIcon = input('inbox');
  emptyTitle = input('Nothing here yet');
  emptyMessage = input('');

  totalElements = input(0);
  pageSize = input(10);
  pageIndex = input(0);
  pageSizeOptions = input<number[]>([10, 25, 50]);
  showPaginator = input(true);

  rowClickable = input(false);

  page = output<PageEvent>();
  rowClick = output<T>();
  retry = output<void>();

  /**
   * `descendants` is required: the templates sit inside the consumer's markup
   * rather than as direct children of this component's content.
   */
  private cells = contentChildren(DataTableCellDirective, { descendants: true });

  protected cellTemplates = computed(
    () => new Map(this.cells().map(cell => [cell.key, cell.template])));

  protected columnKeys = computed(() => this.columns().map(column => column.key));

  protected isEmpty(): boolean {
    return !this.error() && this.rows().length === 0;
  }

  protected onRowClick(row: T): void {
    if (this.rowClickable()) {
      this.rowClick.emit(row);
    }
  }
}
