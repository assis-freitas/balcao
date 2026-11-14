import { Component, computed, inject, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { parseBrl } from '../shared/money';
import { LedgerApi, errorMessage } from './ledger.api';
import { EntryType, PAYMENT_METHODS, PaymentMethod, SUGGESTED_CATEGORIES } from './ledger.models';

export const today = () => new Date().toLocaleDateString('en-CA'); // YYYY-MM-DD, local time

@Component({
  selector: 'app-entry-form',
  imports: [ReactiveFormsModule],
  templateUrl: './entry-form.html',
  styleUrl: './entry-form.css',
})
export class EntryForm {
  private readonly api = inject(LedgerApi);
  readonly recorded = output<void>();

  protected readonly methods = PAYMENT_METHODS;
  protected readonly today = today();
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group({
    type: ['INCOME' as EntryType],
    amount: ['', Validators.required],
    description: ['', [Validators.required, Validators.maxLength(140)]],
    category: ['Vendas', [Validators.required, Validators.maxLength(40)]],
    paymentMethod: ['PIX' as PaymentMethod],
    occurredOn: [today(), Validators.required],
  });

  private readonly type = toSignal(this.form.controls.type.valueChanges, {
    initialValue: 'INCOME' as EntryType,
  });
  protected readonly categories = computed(() => SUGGESTED_CATEGORIES[this.type()]);

  protected setType(type: EntryType) {
    this.form.controls.type.setValue(type);
    // Swap the default category, but keep one the user typed.
    const current = this.form.controls.category.value;
    const defaults = Object.values(SUGGESTED_CATEGORIES).map((list) => list[0]);
    if (!current || defaults.includes(current)) {
      this.form.controls.category.setValue(SUGGESTED_CATEGORIES[type][0]);
    }
  }

  async submit() {
    this.error.set(null);
    const amount = parseBrl(this.form.controls.amount.value);
    if (amount === null) {
      this.error.set('Valor inválido. Use, por exemplo, 12,50.');
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Preencha valor, descrição e categoria.');
      return;
    }

    this.saving.set(true);
    try {
      await this.api.record({ ...this.form.getRawValue(), amount });
      const { type, paymentMethod, category, occurredOn } = this.form.getRawValue();
      this.form.reset({ type, paymentMethod, category, occurredOn, amount: '', description: '' });
      this.recorded.emit();
    } catch (e) {
      this.error.set(errorMessage(e));
    } finally {
      this.saving.set(false);
    }
  }
}
