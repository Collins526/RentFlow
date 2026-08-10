import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../auth.service';
import { Page } from '../property/property.service';

export type UnitType =
  | 'BEDSITTER'
  | 'SINGLE_ROOM'
  | 'STUDIO'
  | 'ONE_BEDROOM'
  | 'TWO_BEDROOM'
  | 'THREE_BEDROOM'
  | 'FOUR_BEDROOM'
  | 'MAISONETTE'
  | 'BUNGALOW'
  | 'SHOP'
  | 'OFFICE'
  | 'WAREHOUSE'
  | 'GODOWN'
  | 'OTHER';

export type OccupancyStatus = 'VACANT' | 'RESERVED' | 'OCCUPIED' | 'UNDER_MAINTENANCE';

export const UNIT_TYPES: UnitType[] = [
  'BEDSITTER',
  'SINGLE_ROOM',
  'STUDIO',
  'ONE_BEDROOM',
  'TWO_BEDROOM',
  'THREE_BEDROOM',
  'FOUR_BEDROOM',
  'MAISONETTE',
  'BUNGALOW',
  'SHOP',
  'OFFICE',
  'WAREHOUSE',
  'GODOWN',
  'OTHER'
];

/** Statuses a user may set directly. OCCUPIED is owned by the Tenancy module. */
export const ASSIGNABLE_OCCUPANCY_STATUSES: OccupancyStatus[] = [
  'VACANT',
  'RESERVED',
  'UNDER_MAINTENANCE'
];

/** Turns an enum constant such as TWO_BEDROOM into "Two Bedroom" for display. */
export function formatEnumLabel(value: string | null | undefined): string {
  if (!value) {
    return '';
  }
  return value
    .split('_')
    .map(word => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ');
}

export interface Unit {
  id: string;
  propertyId: string;
  blockId: string | null;
  blockName: string | null;
  unitNumber: string;
  type: UnitType;
  floorNumber: number | null;
  bedrooms: number;
  bathrooms: number;
  sizeSqFt: number | null;
  rentAmount: number;
  depositAmount: number | null;
  occupancyStatus: OccupancyStatus;
  furnished: boolean;
  waterMeterNumber: string | null;
  electricityMeterNumber: string | null;
  description: string | null;
  status: string;
}

export interface UnitRequest {
  unitNumber: string;
  type: UnitType;
  blockId?: string | null;
  floorNumber?: number | null;
  bedrooms?: number;
  bathrooms?: number;
  sizeSqFt?: number | null;
  rentAmount: number;
  depositAmount?: number | null;
  furnished?: boolean;
  waterMeterNumber?: string | null;
  electricityMeterNumber?: string | null;
  description?: string | null;
  status?: string;
}

export interface BulkUnitRequest {
  prefix?: string;
  startNumber: number;
  count: number;
  numberPadding?: number;
  blockId?: string | null;
  type: UnitType;
  floorNumber?: number | null;
  bedrooms?: number;
  bathrooms?: number;
  sizeSqFt?: number | null;
  rentAmount: number;
  depositAmount?: number | null;
  furnished?: boolean;
  description?: string | null;
}

export interface UnitSummary {
  totalUnits: number;
  vacant: number;
  reserved: number;
  occupied: number;
  underMaintenance: number;
  totalPotentialRent: number;
  currentContractedRent: number;
}

@Injectable({
  providedIn: 'root'
})
export class UnitService {
  constructor(private http: HttpClient) {}

  getUnitsByPropertyId(
    propertyId: string,
    page: number = 0,
    size: number = 10,
    occupancyStatus?: OccupancyStatus | null
  ): Observable<ApiResponse<Page<Unit>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (occupancyStatus) {
      params = params.set('occupancyStatus', occupancyStatus);
    }

    return this.http.get<ApiResponse<Page<Unit>>>(`/api/v1/properties/${propertyId}/units`, { params });
  }

  getUnitsByBlockId(blockId: string, page: number = 0, size: number = 10): Observable<ApiResponse<Page<Unit>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<Page<Unit>>>(`/api/v1/blocks/${blockId}/units`, { params });
  }

  getPropertyUnitSummary(propertyId: string): Observable<ApiResponse<UnitSummary>> {
    return this.http.get<ApiResponse<UnitSummary>>(`/api/v1/properties/${propertyId}/units/summary`);
  }

  getUnitById(id: string): Observable<ApiResponse<Unit>> {
    return this.http.get<ApiResponse<Unit>>(`/api/v1/units/${id}`);
  }

  createUnit(propertyId: string, data: UnitRequest): Observable<ApiResponse<Unit>> {
    return this.http.post<ApiResponse<Unit>>(`/api/v1/properties/${propertyId}/units`, data);
  }

  bulkCreateUnits(propertyId: string, data: BulkUnitRequest): Observable<ApiResponse<Unit[]>> {
    return this.http.post<ApiResponse<Unit[]>>(`/api/v1/properties/${propertyId}/units/bulk`, data);
  }

  updateUnit(id: string, data: UnitRequest): Observable<ApiResponse<Unit>> {
    return this.http.put<ApiResponse<Unit>>(`/api/v1/units/${id}`, data);
  }

  updateOccupancyStatus(id: string, occupancyStatus: OccupancyStatus): Observable<ApiResponse<Unit>> {
    return this.http.patch<ApiResponse<Unit>>(`/api/v1/units/${id}/occupancy`, { occupancyStatus });
  }

  deleteUnit(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`/api/v1/units/${id}`);
  }
}
