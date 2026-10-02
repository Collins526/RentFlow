import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { LandingComponent } from './landing.component';

describe('LandingComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LandingComponent],
      providers: [provideRouter([]), provideNoopAnimations()]
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