import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideEffects } from '@ngrx/effects';
import { provideStore, Store } from '@ngrx/store';
import type { MeResponse } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';
import { selectUsername } from './auth/auth.feature';
import { provideUniversityAuth } from './auth/provide-university-auth';
import { provideUniversityState } from './provide-university-state';
import { UniversityActions } from './university.actions';
import { universityFeature } from './university.feature';

const settle = () => new Promise((resolve) => setTimeout(resolve));

function setup(user: MeResponse | null, api: Record<string, ReturnType<typeof vi.fn>>) {
  const current = signal<MeResponse | null>(user);
  TestBed.configureTestingModule({
    providers: [
      provideStore(),
      provideEffects(),
      provideUniversityAuth(),
      provideUniversityState(),
      {
        provide: UniversitySession,
        useValue: { api, user: current, run: (action: () => Promise<unknown>) => action() },
      },
    ],
  });
  return { store: TestBed.inject(Store), user: current };
}

describe('university effects', () => {
  it('mirrors the session user into the auth feature', () => {
    const { store, user } = setup({ username: 'admin', role: 'ADMIN' }, {});
    expect(store.selectSignal(selectUsername)()).toBe('admin');
    user.set(null);
    TestBed.tick();
    expect(store.selectSignal(selectUsername)()).toBe('');
  });

  it('loads only the signed-in student record', async () => {
    const getStudent = vi.fn().mockResolvedValue({ studentId: 'S-1', name: 'Ada' });
    const { store } = setup({ username: 'S-1', role: 'STUDENT' }, { getStudent });
    store.dispatch(UniversityActions.open({ resource: 'students' }));
    await settle();
    expect(getStudent).toHaveBeenCalledWith('S-1');
    expect(store.selectSignal(universityFeature.selectStudents)()).toEqual([{ studentId: 'S-1', name: 'Ada' }]);
  });

  it('runs an operation, reports success and reloads the resource', async () => {
    const createCourse = vi.fn().mockResolvedValue({});
    const listCourses = vi.fn().mockResolvedValue([{ courseCode: 'C1' }]);
    const { store } = setup({ username: 'admin', role: 'ADMIN' }, {
      createCourse, listCourses, listProfessors: vi.fn().mockResolvedValue([]), listStudents: vi.fn().mockResolvedValue([]),
    });
    store.dispatch(UniversityActions.execute({
      resource: 'courses', success: 'Course created.', closeCreate: true,
      operation: { type: 'createCourse', request: { courseCode: 'C1', title: 'T' } },
    }));
    await settle();
    await settle();
    expect(createCourse).toHaveBeenCalledWith({ courseCode: 'C1', title: 'T' });
    expect(listCourses).toHaveBeenCalled();
    expect(store.selectSignal(universityFeature.selectViews)().courses).toMatchObject({ message: 'Course created.', loading: false });
  });

  it('surfaces operation failures without a success message', async () => {
    const { store } = setup({ username: 'admin', role: 'ADMIN' }, {
      deleteStudent: vi.fn().mockRejectedValue(new Error('Forbidden')),
    });
    store.dispatch(UniversityActions.execute({
      resource: 'students', success: 'Student record deleted.', operation: { type: 'deleteStudent', studentId: 'S-1' },
    }));
    await settle();
    expect(store.selectSignal(universityFeature.selectViews)().students).toMatchObject({ error: 'Forbidden', message: '' });
  });

  it('defaults admins to the student note lookup and refuses the pending queue', async () => {
    const { store } = setup({ username: 'admin', role: 'ADMIN' }, {});
    store.dispatch(UniversityActions.open({ resource: 'notes' }));
    await settle();
    expect(store.selectSignal(universityFeature.selectNoteLookupMode)()).toBe('student');
    store.dispatch(UniversityActions.findNotes({ mode: 'pending', studentId: '', lectureId: null }));
    await settle();
    expect(store.selectSignal(universityFeature.selectViews)().notes.error).toContain('only available to professors');
  });
});
