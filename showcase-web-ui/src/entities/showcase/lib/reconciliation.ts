// SPDX-License-Identifier: MIT
import type { Showcase } from '../types';

/** Returns true when the read model reflects a showcase's expected state. */
export type ShowcasePredicate = (showcases: Showcase[]) => boolean;

/** The dependencies and tuning for a {@link ReconciliationController}. */
export interface ReconciliationOptions {
  /** Refetches the showcase list and resolves with its current data. */
  refresh: () => Promise<Showcase[]>;
  /** Waits between settle attempts; defaults to a timeout-backed sleep. */
  sleep?: (ms: number) => Promise<void>;
  /** How long the first event waits for a burst to arrive, in milliseconds. */
  debounceMs?: number;
  /** The delay between settle attempts, in milliseconds. */
  settleMs?: number;
  /** How many refetches a pending state is given before it is abandoned. */
  attempts?: number;
}

/** The default number of milliseconds an event waits for a burst to arrive before the first flush. */
export const DEFAULT_DEBOUNCE_MS = 200;
const DEFAULT_SETTLE_MS = 500;
const DEFAULT_ATTEMPTS = 5;

interface PendingReconciliation {
  predicate: ShowcasePredicate;
  remaining: number;
}

function timeoutSleep(ms: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

/**
 * Coalesces live-event reconciliations into a single debounced, bounded refetch loop.
 *
 * <p>Events register an expected read-model state keyed by showcase, so a burst spanning several showcases collapses
 * into one flush and repeated events for one showcase keep only the newest state. A flush refetches the list and
 * re-checks every pending state, repeating on the settle interval until all are satisfied or each has spent its
 * refetch budget.
 */
export class ReconciliationController {
  private readonly refresh: () => Promise<Showcase[]>;
  private readonly sleep: (ms: number) => Promise<void>;
  private readonly debounceMs: number;
  private readonly settleMs: number;
  private readonly attempts: number;
  private readonly pending = new Map<string, PendingReconciliation>();
  private timer: ReturnType<typeof setTimeout> | null = null;
  private running = false;

  /**
   * Creates a controller.
   *
   * @param options the refresh callback and the optional debounce/settle tuning
   */
  constructor(options: ReconciliationOptions) {
    this.refresh = options.refresh;
    this.sleep = options.sleep ?? timeoutSleep;
    this.debounceMs = options.debounceMs ?? DEFAULT_DEBOUNCE_MS;
    this.settleMs = options.settleMs ?? DEFAULT_SETTLE_MS;
    this.attempts = options.attempts ?? DEFAULT_ATTEMPTS;
  }

  /**
   * Registers a showcase's expected read-model state, superseding any pending state for it.
   *
   * @param showcaseId the showcase to reconcile
   * @param predicate returns true once the read model reflects the expected state
   */
  reconcile(showcaseId: string, predicate: ShowcasePredicate): void {
    this.pending.set(showcaseId, { predicate, remaining: this.attempts });
    if (!this.running) {
      this.schedule();
    }
  }

  private schedule(): void {
    if (this.timer !== null) {
      clearTimeout(this.timer);
    }
    this.timer = setTimeout(() => {
      this.timer = null;
      void this.flush();
    }, this.debounceMs);
  }

  private async flush(): Promise<void> {
    this.running = true;
    try {
      let settling = false;
      let failed = false;
      while (this.pending.size > 0 && !failed) {
        if (settling) {
          await this.sleep(this.settleMs);
        }
        settling = true;
        try {
          this.observe(await this.refresh());
        } catch {
          failed = true;
        }
      }
    } finally {
      this.running = false;
    }
  }

  private observe(showcases: Showcase[]): void {
    for (const [showcaseId, entry] of this.pending) {
      if (entry.predicate(showcases)) {
        this.pending.delete(showcaseId);
        continue;
      }
      entry.remaining -= 1;
      if (entry.remaining <= 0) {
        this.pending.delete(showcaseId);
      }
    }
  }
}
