export type EntryType = 'INCOME' | 'EXPENSE';
export type PaymentMethod = 'CASH' | 'PIX' | 'DEBIT_CARD' | 'CREDIT_CARD' | 'BANK_SLIP';

export interface Entry {
  id: string;
  type: EntryType;
  /** Decimal reais, e.g. 12.5 */
  amount: number;
  description: string;
  category: string;
  paymentMethod: PaymentMethod;
  occurredOn: string;
  reversalOf: string | null;
  reversed: boolean;
}

export interface Statement {
  from: string;
  to: string;
  balance: { income: number; expense: number; net: number };
  entries: Entry[];
}

export interface NewEntry {
  type: EntryType;
  amount: string;
  description: string;
  category: string;
  paymentMethod: PaymentMethod;
  occurredOn: string;
}

export const PAYMENT_METHODS: { value: PaymentMethod; label: string }[] = [
  { value: 'PIX', label: 'Pix' },
  { value: 'CASH', label: 'Dinheiro' },
  { value: 'DEBIT_CARD', label: 'Cartão de débito' },
  { value: 'CREDIT_CARD', label: 'Cartão de crédito' },
  { value: 'BANK_SLIP', label: 'Boleto' },
];

export const paymentLabel = (method: PaymentMethod) =>
  PAYMENT_METHODS.find((m) => m.value === method)?.label ?? method;

export const SUGGESTED_CATEGORIES = {
  INCOME: ['Vendas', 'Serviços', 'Outras entradas'],
  EXPENSE: ['Fornecedores', 'Aluguel', 'Contas', 'Salários', 'Impostos', 'Taxas', 'Outras saídas'],
} satisfies Record<EntryType, string[]>;
