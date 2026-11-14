import { HttpClient, HttpErrorResponse, httpResource } from '@angular/common/http';
import { Injectable, Signal, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { NewEntry, Statement } from './ledger.models';

/** The only place that knows the Ledger's HTTP API. */
@Injectable({ providedIn: 'root' })
export class LedgerApi {
  private readonly http = inject(HttpClient);

  /** Reloads by itself whenever the period changes. */
  statement(period: Signal<{ from: string; to: string }>) {
    return httpResource<Statement>(() => ({ url: '/api/entries', params: period() }));
  }

  async record(entry: NewEntry): Promise<void> {
    await firstValueFrom(this.http.post('/api/entries', entry));
  }

  async reverse(id: string, reason: string | null): Promise<void> {
    await firstValueFrom(this.http.post(`/api/entries/${id}/reversal`, { reason }));
  }
}

/** Business rules come back as ProblemDetail with a message written for the user. */
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 422 && error.error?.detail) return error.error.detail;
    if (error.status === 400) return 'Preencha tipo, valor, forma de pagamento e data.';
    if (error.status === 0) return 'Sem conexão com o servidor.';
  }
  return 'Algo deu errado. Tente de novo.';
}
