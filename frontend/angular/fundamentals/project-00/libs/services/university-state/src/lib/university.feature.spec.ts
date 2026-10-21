import { AuthActions, authFeature, toAuthUser } from './auth/auth.feature';
import { UniversityActions } from './university.actions';
import {
  initialUniversityState,
  selectAttendedLectures,
  selectNotesHeading,
  selectSchedulableCourses,
  universityFeature,
  UniversityState,
} from './university.feature';

const reduce = (state: UniversityState, ...actions: Parameters<typeof universityFeature.reducer>[1][]) =>
  actions.reduce((current, action) => universityFeature.reducer(current, action), state);

const rootState = (university: UniversityState, username = 'P1', role = 'PROFESSOR') => ({
  [authFeature.name]: { user: { username, role } },
  [universityFeature.name]: university,
});

describe('universityFeature reducer', () => {
  it('stores loaded data, filters it and reconciles the selection', () => {
    const students = [{ studentId: 'S-1', name: 'Ada' }, { studentId: 'S-2', name: 'Bob' }];
    let state = reduce(initialUniversityState,
      UniversityActions.load({ resource: 'students' }),
      UniversityActions.loadSuccess({ resource: 'students', data: { students }, choiceError: '' }),
      UniversityActions.select({ resource: 'students', key: 'S-2', item: students[1] }),
      UniversityActions.setFilter({ resource: 'students', query: 'ada' }),
    );
    const root = rootState(state);
    expect(state.views.students.loading).toBe(false);
    expect(universityFeature.selectFilteredStudents(root)).toEqual([students[0]]);
    expect(universityFeature.selectCurrentStudent(root)).toEqual(students[1]);

    state = reduce(state, UniversityActions.loadSuccess({ resource: 'students', data: { students: [students[0]] }, choiceError: '' }));
    expect(state.selectedStudent).toEqual({ key: '', detail: null });
  });

  it('tracks operation progress, success messages and create-mode closing', () => {
    const state = reduce(initialUniversityState,
      UniversityActions.beginCreate({ resource: 'courses' }),
      UniversityActions.execute({
        resource: 'courses', success: 'Course created.', closeCreate: true,
        operation: { type: 'createCourse', request: { courseCode: 'C1', title: 'T' } },
      }),
    );
    expect(state.views.courses).toMatchObject({ loading: true, createMode: true, message: '' });
    const done = reduce(state, UniversityActions.executeSuccess({ resource: 'courses', success: 'Course created.', closeCreate: true }));
    expect(done.views.courses).toMatchObject({ loading: false, createMode: false, message: 'Course created.' });
    const failed = reduce(state, UniversityActions.executeFailure({ resource: 'courses', error: 'Duplicate' }));
    expect(failed.views.courses).toMatchObject({ loading: false, createMode: true, error: 'Duplicate', message: '' });
  });

  it('ignores stale detail responses and keeps the note course code', () => {
    const note = { id: 7, studentId: 'S-1', lectureId: 3 };
    const state = reduce(initialUniversityState,
      UniversityActions.select({ resource: 'notes', key: '7', item: note }),
      UniversityActions.detailLoaded({ resource: 'notes', key: '8', item: { id: 8 } }),
      UniversityActions.detailLoaded({ resource: 'notes', key: '7', item: { ...note, content: 'full' }, courseCode: 'CS101' }),
    );
    expect(state.selectedNote).toEqual({ key: '7', detail: { ...note, content: 'full' }, courseCode: 'CS101' });
  });

  it('reports find-notes results', () => {
    const state = reduce(initialUniversityState, UniversityActions.findNotesSuccess({ notes: [{ id: 1 }, { id: 2 }] }));
    expect(state.views.notes.message).toBe('2 notes found.');
    expect(state.notes).toHaveLength(2);
  });

  it('discards cached data when a different user signs in', () => {
    const owned = reduce(initialUniversityState,
      AuthActions.userChanged({ user: toAuthUser({ username: 'S-1', role: 'STUDENT' }) }),
      UniversityActions.loadSuccess({ resource: 'students', data: { students: [{ studentId: 'S-1' }] }, choiceError: '' }),
    );
    expect(reduce(owned, AuthActions.userChanged({ user: toAuthUser({ username: 'S-1', role: 'STUDENT' }) })).students).toHaveLength(1);
    const next = reduce(owned, AuthActions.userChanged({ user: null }));
    expect(next).toEqual({ ...initialUniversityState, owner: null });
  });
});

describe('role-aware selectors', () => {
  it('derives attended lectures, schedulable courses and the notes heading', () => {
    const state = {
      ...initialUniversityState,
      lectures: [{ id: 1, attendeeIds: ['S-1'] }, { id: 2, attendeeIds: [] }],
      courses: [{ courseCode: 'A', teacherIds: ['P1'] }, { courseCode: 'B', teacherIds: ['P2'] }],
    };
    expect(selectAttendedLectures(rootState(state, 'S-1', 'STUDENT')).map((l) => l.id)).toEqual([1]);
    expect(selectSchedulableCourses(rootState(state)).map((c) => c.courseCode)).toEqual(['A']);
    expect(selectSchedulableCourses(rootState(state, 'admin', 'ADMIN'))).toEqual([]);
    expect(selectNotesHeading(rootState(state, 'admin', 'ADMIN'))).toBe('Note records');
    expect(selectNotesHeading(rootState(state))).toBe('Pending review');
  });
});
