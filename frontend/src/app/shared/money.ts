const brl = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

/** Formats an amount that arrives from the API as decimal reais. */
export function formatBrl(value: number | string): string {
  return brl.format(Number(value));
}

/**
 * Turns what a person types ("1.234,56", "12,5", "12.50") into the decimal string the API expects,
 * or null when it is not a number. Strings all the way: no floating point between the input and the
 * server.
 */
export function parseBrl(input: string): string | null {
  let text = input
    .trim()
    .replace(/^R\$\s*/, '')
    .replace(/\s/g, '');
  if (text.includes(',')) {
    text = text.replace(/\./g, '').replace(',', '.');
  }
  return /^\d+(\.\d{1,2})?$/.test(text) ? text : null;
}
