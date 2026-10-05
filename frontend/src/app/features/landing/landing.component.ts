import { Component, HostListener, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { SalesInquiryService } from '../../core/services/sales-inquiry.service';

/** Anchors offered in the header and the in-page nav. Kept in one place so the
 *  mobile drawer and the desktop bar cannot drift apart. */
interface NavLink {
  label: string;
  target: string;
}

interface Feature {
  icon: string;
  tone: string;
  title: string;
  body: string;
}

interface Step {
  icon: string;
  title: string;
  body: string;
}

interface Plan {
  name: string;
  price: number | null;
  priceLabel: string;
  cadence: string;
  blurb: string;
  features: string[];
  featured?: boolean;
  cta: string;
}

interface Faq {
  question: string;
  answer: string;
}

interface Testimonial {
  quote: string;
  name: string;
  role: string;
  initials: string;
  tone: string;
}

/**
 * Public marketing page.
 *
 * Renders outside the authenticated shell, so it carries its own header and
 * footer. Interactions (mobile drawer, FAQ accordion, header elevation) are
 * local signals, with sales inquiries submitted to the public inquiry endpoint.
 */
@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, MatButtonModule, MatIconModule],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss'
})
export class LandingComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly salesInquiryService = inject(SalesInquiryService);

  private readonly currentYear = new Date().getFullYear();

  readonly year = this.currentYear;

  /** Drives the header's border and shadow once the hero has scrolled away. */
  readonly scrolled = signal(false);

  /** Collapses the nav into a drawer below the `lg` breakpoint. */
  readonly menuOpen = signal(false);

  /** Controls the sales inquiry dialog. */
  readonly salesFormOpen = signal(false);
  readonly salesSubmitting = signal(false);
  readonly salesSuccess = signal('');
  readonly salesError = signal('');
  readonly salesForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    message: ['', [Validators.required, Validators.maxLength(4000)]]
  });

  /** Index of the expanded FAQ entry. Only one is open at a time. */
  readonly openFaq = signal<number | null>(0);

  readonly navLinks: NavLink[] = [
    { label: 'Features', target: 'features' },
    { label: 'How it works', target: 'how-it-works' },
    { label: 'Modules', target: 'modules' },
    { label: 'For tenants', target: 'tenants' },
    { label: 'Pricing', target: 'pricing' },
    { label: 'FAQ', target: 'faq' }
  ];

  readonly features: Feature[] = [
    {
      icon: 'apartment',
      tone: 'bg-indigo-50 text-indigo-600',
      title: 'Portfolio structure',
      body: 'Properties, blocks, floors and units modelled the way they are actually built. Bulk-create a whole block, then track occupancy unit by unit.'
    },
    {
      icon: 'receipt_long',
      tone: 'bg-sky-50 text-sky-600',
      title: 'Automated rent invoicing',
      body: 'Generate monthly invoices from active tenancies instead of retyping last month\'s rent. Issue, send and reconcile from one screen.'
    },
    {
      icon: 'smartphone',
      tone: 'bg-emerald-50 text-emerald-600',
      title: 'M-Pesa collections',
      body: 'STK push straight to a tenant\'s phone, with the Daraja callback reconciling the payment against the invoice automatically.'
    },
    {
      icon: 'assignment_ind',
      tone: 'bg-violet-50 text-violet-600',
      title: 'Tenancies and leases',
      body: 'Move-in and move-out, lease terms, deposits and rent schedules per tenancy. No overlapping tenancies on a single unit.'
    },
    {
      icon: 'build',
      tone: 'bg-amber-50 text-amber-600',
      title: 'Maintenance tracking',
      body: 'Raise a request, attach photos, track it to completion and see what is outstanding across the portfolio.'
    },
    {
      icon: 'description',
      tone: 'bg-rose-50 text-rose-600',
      title: 'Document vault',
      body: 'Leases, receipts and inspection reports stored against the tenant or unit they belong to, with an audit trail on every record.'
    }
  ];

  readonly steps: Step[] = [
    {
      icon: 'business',
      title: 'Register your organization',
      body: 'Create your account and name your company. You become the organization owner and can invite managers, accountants and staff.'
    },
    {
      icon: 'add_home',
      title: 'Load your properties',
      body: 'Add properties, blocks and units with rent and deposit amounts. Bulk entry keeps a fifty-unit building to a few minutes.'
    },
    {
      icon: 'groups',
      title: 'Place tenants and collect',
      body: 'Create tenancies, issue the first invoice and let tenants pay by M-Pesa. The ledger and dashboard update as money lands.'
    }
  ];

  readonly plans: Plan[] = [
    {
      name: 'Starter',
      price: 2500,
      priceLabel: '',
      cadence: 'per month',
      blurb: 'For a single landlord or a small building.',
      features: [
        'Up to 25 units',
        'Properties, blocks and units',
        'Rent invoicing and ledger',
        'M-Pesa STK push',
        'Tenant portal',
        'Email support'
      ],
      cta: 'Start free trial'
    },
    {
      name: 'Growth',
      price: 6500,
      priceLabel: '',
      cadence: 'per month',
      blurb: 'For property managers running a real portfolio.',
      features: [
        'Up to 150 units',
        'Everything in Starter',
        'Multiple properties',
        'Leases and move-in/out inspections',
        'Maintenance workflow',
        'Document vault',
        'Reporting and dashboard'
      ],
      featured: true,
      cta: 'Start free trial'
    },
    {
      name: 'Portfolio',
      price: null,
      priceLabel: 'Custom',
      cadence: '',
      blurb: 'For agencies and institutional landlords.',
      features: [
        'Unlimited units',
        'Everything in Growth',
        'Multiple organizations',
        'Role and permission control',
        'Platform administration',
        'Dedicated onboarding',
        'Priority support'
      ],
      cta: 'Talk to sales'
    }
  ];

  readonly testimonials: Testimonial[] = [
    {
      quote: 'Reconciling 60 units used to eat the first week of every month. Now the rent roll tells me who has paid before I ask.',
      name: 'Wanjiru Kamau',
      role: 'Property manager, Nairobi',
      initials: 'WK',
      tone: 'bg-indigo-100 text-indigo-700'
    },
    {
      quote: 'My tenants pay from their phone and I get a receipt the moment it lands. The chasing has genuinely stopped.',
      name: 'Daniel Otieno',
      role: 'Landlord, Mombasa',
      initials: 'DO',
      tone: 'bg-emerald-100 text-emerald-700'
    },
    {
      quote: 'The tenant portal removed about forty WhatsApp messages a week. Everyone can see their own balance.',
      name: 'Aisha Noor',
      role: 'Managing partner, Coast Property Group',
      initials: 'AN',
      tone: 'bg-violet-100 text-violet-700'
    }
  ];

  readonly faqs: Faq[] = [
    {
      question: 'Do my tenants need to download an app?',
      answer: 'No. Tenants get a web login to the tenant portal where they can see their balance, pay by M-Pesa, download receipts and raise maintenance requests. Payment happens on their phone through the normal M-Pesa prompt.'
    },
    {
      question: 'How does M-Pesa payment reconciliation work?',
      answer: 'When you push a payment to a tenant\'s phone, Safaricom calls a callback endpoint on your server with the result. RentFlow matches that callback to the invoice and writes the payment, the ledger entry and the receipt in one step, so balances stay correct without manual entry.'
    },
    {
      question: 'Can I use it for more than one property?',
      answer: 'Yes. Properties, blocks and units nest under your organization, and every screen filters to the portfolio you are viewing. Managers and accountants can be given access to only the properties they need.'
    },
    {
      question: 'What happens to my data if I stop paying?',
      answer: 'Your portfolio is never deleted for non-payment. You lose access to the dashboard until you resume, and your records remain exportable. We would rather keep your data intact than hold it hostage.'
    },
    {
      question: 'Is there an API?',
      answer: 'Yes. The backend is documented with OpenAPI and every endpoint sits under /api/v1 with JWT bearer authentication, so integrating an existing accounting or listing system is a matter of calling the endpoints you need.'
    }
  ];

  readonly securityPoints: string[] = [
    'JWT authentication with refresh tokens and role-based access control',
    'Every organization\'s data isolated from every other organization\'s',
    'Six roles from platform admin down to tenant, each scoped to its own permissions',
    'Soft deletes and an audit trail on every record, so nothing disappears silently'
  ];

  @HostListener('window:scroll')
  onWindowScroll(): void {
    this.scrolled.set(window.scrollY > 12);
  }

  toggleMenu(): void {
    this.menuOpen.update(open => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  openSalesForm(): void {
    this.salesError.set('');
    this.salesFormOpen.set(true);
  }

  closeSalesForm(): void {
    this.salesFormOpen.set(false);
  }

  submitSalesForm(): void {
    if (this.salesForm.invalid || this.salesSubmitting()) {
      this.salesForm.markAllAsTouched();
      return;
    }

    this.salesSubmitting.set(true);
    this.salesError.set('');
    this.salesSuccess.set('');
    this.salesInquiryService.submit(this.salesForm.getRawValue()).subscribe({
      next: () => {
        this.salesSubmitting.set(false);
        this.salesForm.reset();
        this.salesFormOpen.set(false);
        this.salesSuccess.set('Thanks. Your message has been sent to our sales team.');
      },
      error: () => {
        this.salesSubmitting.set(false);
        this.salesError.set('Your message could not be sent. Please try again.');
      }
    });
  }

  @HostListener('window:keydown.escape')
  onEscapeKey(): void {
    this.closeSalesForm();
  }

  toggleFaq(index: number): void {
    this.openFaq.update(current => (current === index ? null : index));
  }

  isFaqOpen(index: number): boolean {
    return this.openFaq() === index;
  }

  /**
   * Scrolls to a section with a manual offset, because `scroll-mt` cannot clear a
   * fixed header on its own and the browser's native anchor jump lands the
   * heading underneath it.
   */
  scrollTo(target: string, event?: Event): void {
    event?.preventDefault();
    this.closeMenu();

    const element = document.getElementById(target);
    if (!element) {
      return;
    }

    const headerOffset = 72;
    const top = element.getBoundingClientRect().top + window.scrollY - headerOffset;
    window.scrollTo({ top, behavior: 'smooth' });
  }
}