import { DatePipe } from '@angular/common';
import { Component, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { formatBrl } from '../shared/money';
import { LedgerApi, errorMessage } from './ledger.api';
import { Entry, paymentLabel } from './ledger.models';

@Component({
  selector: 'app-entry-list',
  imports: [DatePipe, FormsModule],
  templateUrl: './entry-list.html',
  styleUrl: './entry-list.css',
})
export class EntryList {
  private readonly api = inject(LedgerApi);
  readonly entries = input.required<Entry[]>();
  readonly changed = output<void>();

  protected readonly formatBrl = formatBrl;
  protected readonly paymentLabel = paymentLabel;
  protected readonly reversing = signal<string | null>(null);
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);
  protected reason = '';

  protected startReversal(entry: Entry) {
    this.reversing.set(entry.id);
    this.reason = '';
    this.error.set(null);
  }

  async confirmReversal(entry: Entry) {
    this.busy.set(true);
    try {
      await this.api.reverse(entry.id, this.reason.trim() || null);
      this.reversing.set(null);
      this.changed.emit();
    } catch (e) {
      this.error.set(errorMessage(e));
    } finally {
      this.busy.set(false);
    }
  }
}
