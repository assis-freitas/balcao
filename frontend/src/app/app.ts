import { Component, computed, inject, signal } from '@angular/core';
import { EntryForm } from './ledger/entry-form';
import { EntryList } from './ledger/entry-list';
import { LedgerApi } from './ledger/ledger.api';
import { formatBrl } from './shared/money';

const monthName = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' });
const iso = (d: Date) => d.toLocaleDateString('en-CA');

@Component({
  selector: 'app-root',
  imports: [EntryForm, EntryList],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly api = inject(LedgerApi);
  protected readonly formatBrl = formatBrl;

  /** First day of the month on screen. */
  private readonly month = signal(new Date(new Date().getFullYear(), new Date().getMonth(), 1));

  protected readonly title = computed(() => {
    const text = monthName.format(this.month()); // "novembro de 2026"
    return text.charAt(0).toUpperCase() + text.slice(1);
  });
  protected readonly isCurrentMonth = computed(() => {
    const now = new Date();
    return (
      this.month().getFullYear() === now.getFullYear() && this.month().getMonth() === now.getMonth()
    );
  });

  private readonly period = computed(() => {
    const first = this.month();
    return { from: iso(first), to: iso(new Date(first.getFullYear(), first.getMonth() + 1, 0)) };
  });

  protected readonly statement = this.api.statement(this.period);

  protected moveMonth(delta: number) {
    this.month.update((m) => new Date(m.getFullYear(), m.getMonth() + delta, 1));
  }

  protected reload() {
    this.statement.reload();
  }
}
