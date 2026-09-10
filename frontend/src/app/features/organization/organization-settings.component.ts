import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatMenuModule } from '@angular/material/menu';
import { OrganizationService, Organization } from '../../core/services/organization/organization.service';
import { AuthService } from '../../core/services/auth.service';
import { Role } from '../../core/auth/roles';
import { resolveApiMessage } from '../../core/services/toast.service';

@Component({
  selector: 'app-organization-settings',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatMenuModule
  ],
  template: `
    <div *ngIf="auth.hasRole(Role.PlatformAdmin)" class="max-w-6xl mx-auto">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Organizations</h1>
        <p class="text-gray-600 mt-2">View and manage every organization in the system.</p>
      </div>

      <div *ngIf="isLoadingData()" class="flex justify-center p-12">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <div *ngIf="!isLoadingData()" class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 text-left text-xs uppercase tracking-wide text-gray-500">
            <tr>
              <th class="px-5 py-4 font-medium">Organization</th>
              <th class="px-5 py-4 font-medium">Contact</th>
              <th class="px-5 py-4 font-medium">Status</th>
              <th class="px-5 py-4 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr *ngFor="let organization of orgs()"
              class="hover:bg-indigo-50 cursor-pointer"
              (click)="openOrganizationProperties(organization.id)">
              <td class="px-5 py-4">
                <p class="font-semibold text-gray-900">{{ organization.name }}</p>
                <p class="text-xs text-gray-500">{{ organization.address || 'No address provided' }}</p>
              </td>
              <td class="px-5 py-4 text-gray-600">{{ organization.email || organization.phone || 'No contact provided' }}</td>
              <td class="px-5 py-4">
                <span class="rounded-full px-2.5 py-1 text-xs font-medium"
                      [class.bg-emerald-100]="organization.status !== 'SUSPENDED'"
                      [class.text-emerald-700]="organization.status !== 'SUSPENDED'"
                      [class.bg-amber-100]="organization.status === 'SUSPENDED'"
                      [class.text-amber-700]="organization.status === 'SUSPENDED'">
                  {{ organization.status === 'SUSPENDED' ? 'Suspended' : 'Active' }}
                </span>
              </td>
              <td class="px-5 py-4 text-right" (click)="$event.stopPropagation()">
                <button mat-icon-button [matMenuTriggerFor]="organizationMenu" aria-label="Organization actions">
                  <mat-icon>more_vert</mat-icon>
                </button>
                <mat-menu #organizationMenu="matMenu">
                  <button mat-menu-item (click)="toggleOrganizationStatus(organization)">
                    <mat-icon>{{ organization.status === 'SUSPENDED' ? 'play_arrow' : 'pause' }}</mat-icon>
                    <span>{{ organization.status === 'SUSPENDED' ? 'Reactivate' : 'Suspend' }}</span>
                  </button>
                  <button mat-menu-item (click)="deleteOrganization(organization)">
                    <mat-icon class="text-red-600">delete</mat-icon>
                    <span class="text-red-600">Delete</span>
                  </button>
                </mat-menu>
              </td>
            </tr>
            <tr *ngIf="!orgs()?.length">
              <td colspan="4" class="px-5 py-12 text-center text-gray-500">No organizations found.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div *ngIf="!auth.hasRole(Role.PlatformAdmin)" class="max-w-4xl mx-auto">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Organization Settings</h1>
        <p class="text-gray-600 mt-2">Manage your company's profile and contact information.</p>
      </div>

      <div *ngIf="isLoadingData()" class="flex justify-center p-12">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <mat-card *ngIf="!isLoadingData()" class="!rounded-2xl !shadow-sm border border-gray-100">
        <mat-card-content class="p-6">
          <div *ngIf="orgs()" class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">Organization</label>
            <select class="w-full p-2 border rounded" [value]="selectedOrgId()" (change)="onOrgSelect($event)">
              <option *ngFor="let o of orgs()" [value]="o.id">{{ o.name }}</option>
            </select>
          </div>
          <form [formGroup]="orgForm" (ngSubmit)="onSubmit()" class="space-y-6">
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Name -->
              <mat-form-field appearance="outline" class="w-full">
                <mat-label>Organization Name</mat-label>
                <input matInput type="text" formControlName="name" required>
                <mat-icon matSuffix>business</mat-icon>
                <mat-error *ngIf="orgForm.get('name')?.hasError('required')">Name is required</mat-error>
                <mat-error *ngIf="orgForm.get('name')?.hasError('maxlength')">Max 100 characters</mat-error>
              </mat-form-field>

              <!-- Email -->
              <mat-form-field appearance="outline" class="w-full">
                <mat-label>Contact Email</mat-label>
                <input matInput type="email" formControlName="email">
                <mat-icon matSuffix>email</mat-icon>
                <mat-error *ngIf="orgForm.get('email')?.hasError('email')">Invalid email format</mat-error>
              </mat-form-field>

              <!-- Phone -->
              <mat-form-field appearance="outline" class="w-full">
                <mat-label>Phone Number</mat-label>
                <input matInput type="text" formControlName="phone">
                <mat-icon matSuffix>phone</mat-icon>
                <mat-error *ngIf="orgForm.get('phone')?.hasError('maxlength')">Max 20 characters</mat-error>
              </mat-form-field>

              <!-- Logo URL -->
              <mat-form-field appearance="outline" class="w-full">
                <mat-label>Logo URL</mat-label>
                <input matInput type="text" formControlName="logoUrl">
                <mat-icon matSuffix>image</mat-icon>
                <mat-error *ngIf="orgForm.get('logoUrl')?.hasError('maxlength')">URL too long</mat-error>
              </mat-form-field>
            </div>

            <!-- Address -->
            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Physical Address</mat-label>
              <textarea matInput formControlName="address" rows="3"></textarea>
              <mat-error *ngIf="orgForm.get('address')?.hasError('maxlength')">Max 255 characters</mat-error>
            </mat-form-field>

            <div class="flex justify-end pt-4 border-t border-gray-100">
              <button mat-flat-button color="primary" type="submit" class="!px-8 !py-6" [disabled]="orgForm.invalid || isSaving()">
                <span *ngIf="!isSaving()" class="text-base">Save Changes</span>
                <mat-spinner *ngIf="isSaving()" diameter="20" color="accent"></mat-spinner>
              </button>
            </div>

            <div *ngIf="auth.hasRole(Role.PlatformAdmin) && selectedOrgId()"
                 class="mt-6 border-t border-red-100 pt-5 flex items-center justify-between gap-4">
              <div>
                <p class="font-medium text-red-700">Delete organization</p>
                <p class="text-sm text-gray-500">This removes it and its properties from active system data.</p>
              </div>
              <button mat-stroked-button color="warn" type="button" (click)="deleteSelectedOrganization()" [disabled]="isSaving()">
                <mat-icon>delete</mat-icon>
                Delete
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `
})
export class OrganizationSettingsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private orgService = inject(OrganizationService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  protected auth = inject(AuthService);
  protected Role = Role;

  isLoadingData = signal(true);
  isSaving = signal(false);
  orgs = signal<Organization[] | null>(null);
  selectedOrgId = signal<string | null>(null);

  orgForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    phone: ['', Validators.maxLength(20)],
    logoUrl: ['', Validators.maxLength(500)],
    address: ['', Validators.maxLength(255)]
  });

  ngOnInit() {
    this.loadOrganization();
  }

  openOrganizationProperties(organizationId: string): void {
    this.router.navigate(['/properties'], { queryParams: { organizationId } });
  }

  loadOrganization() {
    // Platform admins may manage any registered organization; load list
    if (this.auth.hasRole(Role.PlatformAdmin)) {
      this.orgService.getAllOrganizations().subscribe({
        next: (res) => {
          this.orgs.set(res.data?.content ?? []);
          this.isLoadingData.set(false);
        },
        error: () => {
          this.snackBar.open('Failed to load organizations', 'Close', { duration: 3000 });
          this.isLoadingData.set(false);
        }
      });
      return;
    }

    this.orgService.getMyOrganization().subscribe({
      next: (res) => {
        if (res.data) {
          this.orgForm.patchValue({
            name: res.data.name || '',
            email: res.data.email || '',
            phone: res.data.phone || '',
            logoUrl: res.data.logoUrl || '',
            address: res.data.address || ''
          });
        }
        this.isLoadingData.set(false);
      },
      error: () => {
        this.snackBar.open('Failed to load organization settings', 'Close', { duration: 3000 });
        this.isLoadingData.set(false);
      }
    });
  }

  loadOrganizationById(id: string) {
    this.isLoadingData.set(true);
    this.orgService.getOrganizationById(id).subscribe({
      next: (res) => {
        if (res.data) {
          this.orgForm.patchValue({
            name: res.data.name || '',
            email: res.data.email || '',
            phone: res.data.phone || '',
            logoUrl: res.data.logoUrl || '',
            address: res.data.address || ''
          });
        }
        this.isLoadingData.set(false);
      },
      error: () => {
        this.snackBar.open('Failed to load organization', 'Close', { duration: 3000 });
        this.isLoadingData.set(false);
      }
    });
  }

  onOrgSelect(event: Event) {
    const id = (event.target as HTMLSelectElement).value;
    this.selectedOrgId.set(id);
    this.loadOrganizationById(id);
  }

  onSubmit() {
    if (this.orgForm.valid) {
      this.isSaving.set(true);
      if (this.auth.hasRole(Role.PlatformAdmin) && this.selectedOrgId()) {
        this.orgService.updateOrganizationById(this.selectedOrgId()!, this.orgForm.value).subscribe({
          next: () => {
            this.isSaving.set(false);
            this.snackBar.open('Organization updated successfully', 'Close', { 
              duration: 3000,
              panelClass: ['bg-green-600', 'text-white']
            });
          },
          error: () => {
            this.isSaving.set(false);
            this.snackBar.open('Failed to update organization', 'Close', { 
              duration: 3000,
              panelClass: ['bg-red-600', 'text-white']
            });
          }
        });
      } else {
        this.orgService.updateMyOrganization(this.orgForm.value).subscribe({
          next: () => {
            this.isSaving.set(false);
            this.snackBar.open('Organization updated successfully', 'Close', { 
              duration: 3000,
              panelClass: ['bg-green-600', 'text-white']
            });
          },
          error: () => {
            this.isSaving.set(false);
            this.snackBar.open('Failed to update organization', 'Close', { 
              duration: 3000,
              panelClass: ['bg-red-600', 'text-white']
            });
          }
        });
      }
    }
  }

  deleteSelectedOrganization() {
    const id = this.selectedOrgId();
    const organization = this.orgs()?.find(org => org.id === id);
    if (!id || !organization || !window.confirm(
      `Delete ${organization.name} and all of its properties? This action cannot be undone.`)) {
      return;
    }

    this.isSaving.set(true);
    this.orgService.deleteOrganizationById(id).subscribe({
      next: () => {
        const remaining = (this.orgs() ?? []).filter(org => org.id !== id);
        this.orgs.set(remaining);
        this.selectedOrgId.set(remaining[0]?.id ?? null);
        this.orgForm.reset();
        if (remaining[0]) {
          this.loadOrganizationById(remaining[0].id);
        } else {
          this.isSaving.set(false);
          this.isLoadingData.set(false);
        }
        this.snackBar.open('Organization and properties deleted', 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.isSaving.set(false);
        this.snackBar.open(resolveApiMessage(err, 'Failed to delete organization'), 'Close', { duration: 5000 });
      }
    });
  }

  toggleOrganizationStatus(organization: Organization): void {
    const suspended = organization.status === 'SUSPENDED';
    const action = suspended ? 'reactivate' : 'suspend';
    if (!window.confirm(`${suspended ? 'Reactivate' : 'Suspend'} ${organization.name}?`)) {
      return;
    }

    const request = suspended
      ? this.orgService.activateOrganization(organization.id)
      : this.orgService.suspendOrganization(organization.id);
    request.subscribe({
      next: () => {
        organization.status = suspended ? 'ACTIVE' : 'SUSPENDED';
        this.snackBar.open(`Organization ${action}d successfully`, 'Close', { duration: 3000 });
      },
      error: (err) => this.snackBar.open(resolveApiMessage(err, `Failed to ${action} organization`), 'Close', { duration: 5000 })
    });
  }

  deleteOrganization(organization: Organization): void {
    if (!window.confirm(`Delete ${organization.name} and its properties? This cannot be undone.`)) {
      return;
    }

    this.orgService.deleteOrganizationById(organization.id).subscribe({
      next: () => {
        this.orgs.update(organizations => (organizations ?? []).filter(item => item.id !== organization.id));
        this.snackBar.open('Organization deleted successfully', 'Close', { duration: 3000 });
      },
      error: (err) => this.snackBar.open(resolveApiMessage(err, 'Failed to delete organization'), 'Close', { duration: 5000 })
    });
  }
}
