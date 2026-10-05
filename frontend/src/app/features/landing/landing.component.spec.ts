import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { SalesInquiryService } from '../../core/services/sales-inquiry.service';
import { LandingComponent } from './landing.component';

describe('LandingComponent', () => {
  let submitInquiry: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    submitInquiry = vi.fn(() => of({
      success: true,
      message: 'Inquiry sent to sales',
      data: { id: 'inquiry-1', name: 'Sam Example', email: 'sam@example.com', message: 'Portfolio details', createdAt: '' },
      errors: []
    }));
    await TestBed.configureTestingModule({
      imports: [LandingComponent],
      providers: [
        provideRouter([]),
        provideNoopAnimations(),
        { provide: SalesInquiryService, useValue: { submit: submitInquiry } }
      ]
    }).compileComponents();
  });

  afterEach(() => TestBed.resetTestingModule());

  it('creates', () => {
    const fixture = TestBed.createComponent(LandingComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders the hero and the primary calls to action', () => {
    const fixture = TestBed.createComponent(LandingComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('h1')?.textContent).toContain('Rent collection');
    expect(compiled.querySelector('a[href="/register"]')).toBeTruthy();
    expect(compiled.querySelector('a[href="/login"]')).toBeTruthy();
  });

  it('routes paid plan trial buttons to their selected checkout plans', () => {
    const fixture = TestBed.createComponent(LandingComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('a[href="/checkout/STARTER"]')?.textContent).toContain('Start free trial');
    expect(compiled.querySelector('a[href="/checkout/GROWTH"]')?.textContent).toContain('Start free trial');
  });

  it('opens a sales inquiry form from the Portfolio plan', () => {
    const fixture = TestBed.createComponent(LandingComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    const salesButton = Array.from(compiled.querySelectorAll('button'))
      .find(button => button.textContent?.trim() === 'Talk to sales');
    expect(salesButton).toBeTruthy();
    salesButton?.click();
    fixture.detectChanges();

    const dialog = compiled.querySelector('[role="dialog"]');
    expect(dialog).toBeTruthy();
    expect(dialog?.querySelector('input[name="name"]')).toBeTruthy();
    expect(dialog?.querySelector('input[name="email"]')).toBeTruthy();
    expect(dialog?.querySelector('textarea[name="message"]')).toBeTruthy();

    (dialog?.querySelector('[aria-label="Close sales inquiry form"]') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(compiled.querySelector('[role="dialog"]')).toBeNull();
  });

  it('sends the inquiry fields and shows confirmation after success', () => {
    const fixture = TestBed.createComponent(LandingComponent);
    const component = fixture.componentInstance;
    component.salesForm.setValue({
      name: 'Sam Example',
      email: 'sam@example.com',
      message: 'Portfolio details'
    });

    component.submitSalesForm();
    fixture.detectChanges();

    expect(submitInquiry).toHaveBeenCalledWith({
      name: 'Sam Example',
      email: 'sam@example.com',
      message: 'Portfolio details'
    });
    expect(component.salesSuccess()).toContain('sent to our sales team');
    expect(component.salesFormOpen()).toBe(false);
  });

  it('gives every nav link an anchor target that exists on the page', () => {
    const fixture = TestBed.createComponent(LandingComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    for (const link of fixture.componentInstance.navLinks) {
      expect(compiled.querySelector(`#${link.target}`))
        .toBeTruthy();
    }
  });

  it('starts with the first FAQ open and collapses it when toggled', () => {
    const fixture = TestBed.createComponent(LandingComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.isFaqOpen(0)).toBe(true);

    component.toggleFaq(0);
    fixture.detectChanges();
    expect(component.isFaqOpen(0)).toBe(false);

    // Opening one entry closes whichever was open, so the list never doubles up.
    component.toggleFaq(2);
    fixture.detectChanges();
    expect(component.isFaqOpen(0)).toBe(false);
    expect(component.isFaqOpen(2)).toBe(true);
  });

  it('closes the mobile drawer when the menu toggle is used twice', () => {
    const fixture = TestBed.createComponent(LandingComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.menuOpen()).toBe(false);

    component.toggleMenu();
    fixture.detectChanges();
    expect(component.menuOpen()).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#landing-mobile-nav')).toBeTruthy();

    component.closeMenu();
    fixture.detectChanges();
    expect(component.menuOpen()).toBe(false);
    expect(fixture.nativeElement.querySelector('#landing-mobile-nav')).toBeNull();
  });

  it('marks the header as scrolled once the page has moved', () => {
    const fixture = TestBed.createComponent(LandingComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.scrolled()).toBe(false);

    component.onWindowScroll();
    fixture.detectChanges();
    expect(component.scrolled()).toBe(window.scrollY > 12);
  });
});