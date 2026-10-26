import { ChangeDetectionStrategy, Component, computed, inject, input, OnInit, ViewEncapsulation } from '@angular/core';
import { Store } from '@ngrx/store';
import {
  Resource,
  selectDisplayName,
  selectRole,
  UniversityActions,
  universityFeature,
} from '@project-00/services-university-state';

/**
 * Shared chrome for every University remote: page heading, account block, store-driven notices and the
 * filter toolbar. Remote pages project their workspace grid into it. Opening the frame dispatches
 * `UniversityActions.open`, which loads the resource into NgRx.
 */
@Component({
  selector: 'app-university-frame',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  styleUrl: './university-frame.component.scss',
  template: `
    <main class="university">
      <header class="page-heading">
        <div>
          <p class="eyebrow">University workspace</p>
          <h1>Resources</h1>
          <p>Manage university records and course activity.</p>
        </div>
        <div class="account">
          <span class="account-chip">{{ displayName() }} · {{ role() }}</span>
        </div>
      </header>

      @if (view().message) { <p class="notice success" role="status">{{ view().message }}</p> }
      @if (view().error) { <p class="notice failure" role="alert">{{ view().error }}</p> }
      @if (view().choiceError) { <p class="notice failure" role="alert">Some selection data could not be loaded: {{ view().choiceError }}</p> }
      @if (view().loading) { <p class="loading" role="status">Loading {{ resource() }}…</p> }

      <section class="toolbar" aria-label="Resource filters">
        <label class="search">Filter {{ resource() }}
          <input type="search" placeholder="Search by name, ID, title…" [value]="view().filter" (input)="setFilter($event)" />
        </label>
        <button type="button" class="quiet" (click)="refresh()">Refresh</button>
      </section>

      <ng-content />
    </main>
  `,
})
export class UniversityFrameComponent implements OnInit {
  readonly resource = input.required<Resource>();

  private readonly store = inject(Store);
  protected readonly displayName = this.store.selectSignal(selectDisplayName);
  protected readonly role = this.store.selectSignal(selectRole);
  private readonly views = this.store.selectSignal(universityFeature.selectViews);
  protected readonly view = computed(() => this.views()[this.resource()]);

  ngOnInit(): void {
    this.store.dispatch(UniversityActions.open({ resource: this.resource() }));
  }

  protected setFilter(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.store.dispatch(UniversityActions.setFilter({ resource: this.resource(), query }));
  }

  protected refresh(): void {
    this.store.dispatch(UniversityActions.load({ resource: this.resource() }));
  }
}
