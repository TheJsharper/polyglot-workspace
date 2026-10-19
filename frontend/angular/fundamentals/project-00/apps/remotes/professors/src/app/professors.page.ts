import { ChangeDetectionStrategy, Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import type { ProfessorResponse } from '@project-00/services-api';
import {
  professorKey,
  selectIsAdmin,
  selectResourceView,
  UniversityActions,
  universityFeature,
} from '@project-00/services-university-state';
import { UniversityFrameComponent } from '@project-00/views-university';

@Component({
  selector: 'app-professors-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, UniversityFrameComponent],
  template: `
    <app-university-frame resource="professors">
      <section class="workspace-grid">
        <div class="panel">
          <div class="panel-heading"><h2>Professors <span class="count">{{ filtered().length }}</span></h2>
            @if (isAdmin()) { <button type="button" (click)="beginCreate()">Add professor</button> }
          </div>
          @if (!professors().length && !view().loading) { <p class="empty">No professor records are available.</p> }
          <ul class="record-list">
            @for (professor of filtered(); track professorKey(professor)) {
              <li><button type="button" class="record" [class.selected]="selectedKey() === professorKey(professor)" (click)="select(professor)">
                <strong>{{ professor.name || 'Unnamed professor' }}</strong><span>{{ professor.employeeId || 'No employee ID' }} · {{ professor.department || 'No department' }}</span>
              </button></li>
            }
          </ul>
          @if (view().createMode) {
            <form class="form" (ngSubmit)="create()">
              <h3>New professor</h3>
              <label>Employee ID <input name="newProfessorId" [(ngModel)]="newId" required /></label>
              <label>Full name <input name="newProfessorName" [(ngModel)]="newName" required /></label>
              <label>Department <input name="newProfessorDepartment" [(ngModel)]="newDepartment" required /></label>
              <label>Temporary password <input name="newProfessorPassword" [(ngModel)]="newPassword" type="password" required minlength="8" autocomplete="new-password" /></label>
              <div class="actions"><button type="submit" [disabled]="view().loading">Create professor</button><button type="button" class="quiet" (click)="cancelCreate()">Cancel</button></div>
            </form>
          }
        </div>
        <article class="panel detail">
          @if (current(); as professor) {
            <h2>{{ professor.name || 'Professor details' }}</h2>
            <dl><dt>Employee ID</dt><dd>{{ professor.employeeId || '—' }}</dd><dt>Department</dt><dd>{{ professor.department || '—' }}</dd><dt>Record number</dt><dd>{{ professor.id ?? '—' }}</dd></dl>
            @if (isAdmin()) {
              <form class="form" (ngSubmit)="update(professor)">
                <h3>Edit professor</h3>
                <label>Full name <input name="professorEditName" [(ngModel)]="editName" required /></label>
                <label>Department <input name="professorEditDepartment" [(ngModel)]="editDepartment" required /></label>
                <div class="actions"><button type="submit" [disabled]="view().loading">Save changes</button>
                  <button type="button" class="danger" (click)="remove(professor)">Delete professor</button>
                </div>
              </form>
            }
          } @else { <p class="empty">Select a professor to view details.</p> }
        </article>
      </section>
    </app-university-frame>
  `,
})
export class ProfessorsPage {
  private readonly store = inject(Store);
  protected readonly professorKey = professorKey;
  protected readonly view = this.store.selectSignal(selectResourceView('professors'));
  protected readonly professors = this.store.selectSignal(universityFeature.selectProfessors);
  protected readonly filtered = this.store.selectSignal(universityFeature.selectFilteredProfessors);
  protected readonly current = this.store.selectSignal(universityFeature.selectCurrentProfessor);
  private readonly selection = this.store.selectSignal(universityFeature.selectSelectedProfessor);
  protected readonly selectedKey = computed(() => this.selection().key);
  protected readonly isAdmin = this.store.selectSignal(selectIsAdmin);

  protected readonly newId = signal('');
  protected readonly newName = signal('');
  protected readonly newDepartment = signal('');
  protected readonly newPassword = signal('');
  protected readonly editName = signal('');
  protected readonly editDepartment = signal('');

  constructor() {
    effect(() => {
      const professor = this.current();
      untracked(() => {
        this.editName.set(professor?.name || '');
        this.editDepartment.set(professor?.department || '');
      });
    });
    // Never keep the temporary password around once the create form closes.
    effect(() => {
      if (!this.view().createMode) untracked(() => this.newPassword.set(''));
    });
  }

  protected select(item: ProfessorResponse): void {
    this.store.dispatch(UniversityActions.select({ resource: 'professors', key: professorKey(item), item }));
  }

  protected beginCreate(): void {
    this.newId.set('');
    this.newName.set('');
    this.newDepartment.set('');
    this.newPassword.set('');
    this.store.dispatch(UniversityActions.beginCreate({ resource: 'professors' }));
  }

  protected cancelCreate(): void {
    this.store.dispatch(UniversityActions.cancelCreate({ resource: 'professors' }));
  }

  protected create(): void {
    this.store.dispatch(UniversityActions.execute({
      resource: 'professors',
      success: 'Professor record created.',
      closeCreate: true,
      operation: {
        type: 'createProfessor',
        request: {
          employeeId: this.newId().trim(),
          name: this.newName().trim(),
          department: this.newDepartment().trim(),
          password: this.newPassword(),
        },
      },
    }));
  }

  protected update(item: ProfessorResponse): void {
    if (!item.employeeId) return;
    this.store.dispatch(UniversityActions.execute({
      resource: 'professors',
      success: 'Professor record updated.',
      operation: {
        type: 'updateProfessor',
        employeeId: item.employeeId,
        request: { name: this.editName().trim(), department: this.editDepartment().trim() },
      },
    }));
  }

  protected remove(item: ProfessorResponse): void {
    if (!item.employeeId || !globalThis.confirm(`Delete professor ${item.employeeId}? This may affect assigned courses and lectures.`)) return;
    this.store.dispatch(UniversityActions.execute({
      resource: 'professors',
      success: 'Professor record deleted.',
      operation: { type: 'deleteProfessor', employeeId: item.employeeId },
    }));
  }
}
