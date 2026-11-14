import { describe, expect, it } from 'vitest';
import { formatBrl, parseBrl } from './money';

describe('parseBrl', () => {
  it.each([
    ['12,50', '12.50'],
    ['1.234,56', '1234.56'],
    ['R$ 10', '10'],
    ['12.5', '12.5'],
    [' 7,0 ', '7.0'],
  ])('reads %s as %s', (input, expected) => {
    expect(parseBrl(input)).toBe(expected);
  });

  it.each(['', 'abc', '12,345', '-5', '1,2,3'])('rejects %s', (input) => {
    expect(parseBrl(input)).toBeNull();
  });
});

describe('formatBrl', () => {
  it('formats as Brazilian reais', () => {
    expect(formatBrl(1234.5).replace(/\s/g, ' ')).toBe('R$ 1.234,50');
  });
});
