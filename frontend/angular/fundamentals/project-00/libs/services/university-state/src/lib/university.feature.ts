import { createFeature, createReducer, createSelector, on } from '@ngrx/store';
import type {
  CourseResponse,
  LectureResponse,
  NotesResponse,
  ProfessorResponse,
  StudentResponse,
} from '@project-00/services-api';
import { AuthActions, selectIsAdmin, selectIsProfessor, selectIsStudent, selectUsername } from './auth/auth.feature';
import { UniversityActions } from './university.actions';
import {
  courseKey,
  lectureKey,
  noteKey,
  NoteLookupMode,
  professorKey,
  Resource,
  RESOURCES,
  ResourceView,
  studentKey,
  UniversityCollections,
} from './university.models';

export interface Selection<T> {
  key: string;
  detail: T | null;
}

export interface UniversityState extends UniversityCollections {
  /** Username the cached data belongs to; data is discarded when the signed-in user changes. */
  owner: string | null;
  views: Record<Resource, ResourceView>;
  selectedStudent: Selection<StudentResponse>;
  selectedProfessor: Selection<ProfessorResponse>;
  selectedCourse: Selection<CourseResponse>;
  selectedLecture: Selection<LectureResponse>;
  selectedNote: Selection<NotesResponse> & { courseCode: string };
  noteLookupMode: NoteLookupMode;
}

const emptyView: ResourceView = { filter: '', createMode: false, loading: false, error: '', message: '', choiceError: '' };
const emptySelection = { key: '', detail: null };

export const initialUniversityState: UniversityState = {
  owner: null,
  students: [],
  professors: [],
  courses: [],
  lectures: [],
  notes: [],
  views: Object.fromEntries(RESOURCES.map((resource) => [resource, emptyView])) as Record<Resource, ResourceView>,
  selectedStudent: emptySelection,
  selectedProfessor: emptySelection,
  selectedCourse: emptySelection,
  selectedLecture: emptySelection,
  selectedNote: { ...emptySelection, courseCode: '' },
  noteLookupMode: 'pending',
};

function patchView(state: UniversityState, resource: Resource, patch: Partial<ResourceView>): UniversityState {
  return { ...state, views: { ...state.views, [resource]: { ...state.views[resource], ...patch } } };
}

function reconcile<T>(selection: Selection<T>, list: T[], key: (item: T) => string): Selection<T> {
  const found = list.find((item) => key(item) === selection.key) ?? null;
  return found ? { key: selection.key, detail: found } : emptySelection;
}

export const universityFeature = createFeature({
  name: 'university',
  reducer: createReducer(
    initialUniversityState,
    on(AuthActions.userChanged, (state, { user }): UniversityState =>
      (user?.username ?? null) === state.owner ? state : { ...initialUniversityState, owner: user?.username ?? null }),
    on(UniversityActions.reset, (state): UniversityState => ({ ...initialUniversityState, owner: state.owner })),
    on(UniversityActions.open, (state, { resource }) =>
      patchView(state, resource, { filter: '', message: '', error: '', createMode: false })),
    on(UniversityActions.load, (state, { resource }) =>
      patchView(state, resource, { loading: true, error: '', choiceError: '' })),
    on(UniversityActions.loadSuccess, (state, { resource, data, choiceError }) => {
      const next: UniversityState = patchView({ ...state, ...data }, resource, { loading: false, choiceError });
      switch (resource) {
        case 'students':
          return { ...next, selectedStudent: reconcile(next.selectedStudent, next.students, studentKey) };
        case 'professors':
          return { ...next, selectedProfessor: reconcile(next.selectedProfessor, next.professors, professorKey) };
        case 'courses':
          return { ...next, selectedCourse: reconcile(next.selectedCourse, next.courses, courseKey) };
        case 'lectures':
          return { ...next, selectedLecture: reconcile(next.selectedLecture, next.lectures, lectureKey) };
        case 'notes': {
          const keep = next.notes.some((item) => noteKey(item) === next.selectedNote.key);
          return { ...next, selectedNote: { key: keep ? next.selectedNote.key : '', detail: null, courseCode: '' } };
        }
      }
    }),
    on(UniversityActions.loadFailure, (state, { resource, error }) =>
      patchView(state, resource, { loading: false, error })),
    on(UniversityActions.setFilter, (state, { resource, query }) => patchView(state, resource, { filter: query })),
    on(UniversityActions.beginCreate, (state, { resource }) => patchView(state, resource, { createMode: true })),
    on(UniversityActions.cancelCreate, (state, { resource }) => patchView(state, resource, { createMode: false })),
    on(UniversityActions.select, (state, selection): UniversityState => {
      const next = patchView(state, selection.resource, { message: '' });
      switch (selection.resource) {
        case 'students': return { ...next, selectedStudent: { key: selection.key, detail: selection.item } };
        case 'professors': return { ...next, selectedProfessor: { key: selection.key, detail: selection.item } };
        case 'courses': return { ...next, selectedCourse: { key: selection.key, detail: selection.item } };
        case 'lectures': return { ...next, selectedLecture: { key: selection.key, detail: selection.item } };
        case 'notes': return { ...next, selectedNote: { key: selection.key, detail: selection.item, courseCode: '' } };
      }
    }),
    on(UniversityActions.detailLoaded, (state, selection): UniversityState => {
      switch (selection.resource) {
        case 'students':
          return state.selectedStudent.key === selection.key ? { ...state, selectedStudent: { key: selection.key, detail: selection.item } } : state;
        case 'professors':
          return state.selectedProfessor.key === selection.key ? { ...state, selectedProfessor: { key: selection.key, detail: selection.item } } : state;
        case 'courses':
          return state.selectedCourse.key === selection.key ? { ...state, selectedCourse: { key: selection.key, detail: selection.item } } : state;
        case 'lectures':
          return state.selectedLecture.key === selection.key ? { ...state, selectedLecture: { key: selection.key, detail: selection.item } } : state;
        case 'notes':
          return state.selectedNote.key === selection.key
            ? { ...state, selectedNote: { key: selection.key, detail: selection.item, courseCode: selection.courseCode ?? '' } }
            : state;
      }
    }),
    on(UniversityActions.detailFailure, (state, { resource, key, error }) => {
      const current = {
        students: state.selectedStudent.key,
        professors: state.selectedProfessor.key,
        courses: state.selectedCourse.key,
        lectures: state.selectedLecture.key,
        notes: state.selectedNote.key,
      }[resource];
      return current === key ? patchView(state, resource, { error }) : state;
    }),
    on(UniversityActions.execute, (state, { resource }) =>
      patchView(state, resource, { loading: true, error: '', message: '' })),
    on(UniversityActions.executeSuccess, (state, { resource, success, closeCreate }) =>
      patchView(state, resource, {
        loading: false,
        message: success,
        ...(closeCreate ? { createMode: false } : {}),
      })),
    on(UniversityActions.executeFailure, (state, { resource, error }) =>
      patchView(state, resource, { loading: false, error })),
    on(UniversityActions.setNoteLookupMode, (state, { mode }): UniversityState => ({ ...state, noteLookupMode: mode })),
    on(UniversityActions.findNotes, (state): UniversityState => ({
      ...patchView(state, 'notes', { loading: true, error: '', message: '' }),
      selectedNote: { key: '', detail: null, courseCode: '' },
    })),
    on(UniversityActions.findNotesSuccess, (state, { notes }): UniversityState => ({
      ...patchView(state, 'notes', {
        loading: false,
        message: `${notes.length} note${notes.length === 1 ? '' : 's'} found.`,
      }),
      notes,
    })),
    on(UniversityActions.findNotesFailure, (state, { error }): UniversityState => ({
      ...patchView(state, 'notes', { loading: false, error }),
      notes: [],
    })),
  ),
  extraSelectors: ({
    selectViews,
    selectStudents,
    selectProfessors,
    selectCourses,
    selectLectures,
    selectNotes,
    selectSelectedStudent,
    selectSelectedProfessor,
    selectSelectedCourse,
    selectSelectedLecture,
    selectSelectedNote,
  }) => {
    const filtered = <T>(resource: Resource, list: (state: object) => T[], text: (item: T) => string) =>
      createSelector(list, selectViews, (items, views) => {
        const query = views[resource].filter.trim().toLowerCase();
        return items.filter((item) => text(item).toLowerCase().includes(query));
      });
    const current = <T>(selection: (state: object) => Selection<T>, list: (state: object) => T[], key: (item: T) => string) =>
      createSelector(selection, list, ({ key: selected, detail }, items) =>
        detail && key(detail) === selected ? detail : items.find((item) => key(item) === selected) ?? null);
    return {
      selectFilteredStudents: filtered('students', selectStudents, (item) => `${item.studentId} ${item.name} ${item.enrollmentYear}`),
      selectFilteredProfessors: filtered('professors', selectProfessors, (item) => `${item.employeeId} ${item.name} ${item.department}`),
      selectFilteredCourses: filtered('courses', selectCourses, (item) => `${item.courseCode} ${item.title} ${item.credits}`),
      selectFilteredLectures: filtered('lectures', selectLectures, (item) => `${item.courseCode} ${item.employeeId} ${item.topic} ${item.room} ${item.date}`),
      selectFilteredNotes: filtered('notes', selectNotes, (item) => `${item.studentId} ${item.lectureId} ${item.content} ${item.grade}`),
      selectCurrentStudent: current(selectSelectedStudent, selectStudents, studentKey),
      selectCurrentProfessor: current(selectSelectedProfessor, selectProfessors, professorKey),
      selectCurrentCourse: current(selectSelectedCourse, selectCourses, courseKey),
      selectCurrentLecture: current(selectSelectedLecture, selectLectures, lectureKey),
      selectCurrentNote: current(selectSelectedNote, selectNotes, noteKey),
      selectSelectedNoteCourseCode: createSelector(selectSelectedNote, (selection) => selection.courseCode),
    };
  },
});

export const selectResourceView = (resource: Resource) =>
  createSelector(universityFeature.selectViews, (views) => views[resource]);

/** Lectures the signed-in student attended (notes can only be written for these). */
export const selectAttendedLectures = createSelector(universityFeature.selectLectures, selectUsername, (lectures, username) =>
  lectures.filter((lecture) => lecture.id != null && (lecture.attendeeIds || []).includes(username)));

/** Courses the signed-in professor teaches. */
export const selectSchedulableCourses = createSelector(
  universityFeature.selectCourses,
  selectUsername,
  selectIsProfessor,
  (courses, username, isProfessor) => isProfessor ? courses.filter((course) => course.teacherIds?.includes(username)) : [],
);

export const selectNotesHeading = createSelector(selectIsStudent, selectIsAdmin, (isStudent, isAdmin) =>
  isStudent ? 'My notes' : isAdmin ? 'Note records' : 'Pending review');

export const selectNotesEmptyMessage = createSelector(selectIsStudent, selectIsAdmin, (isStudent, isAdmin) => {
  if (isStudent) return 'You have not written any notes yet.';
  if (isAdmin) return 'Choose a student or lecture lookup to find notes.';
  return 'No notes are waiting for review. You can also search by student or lecture.';
});
