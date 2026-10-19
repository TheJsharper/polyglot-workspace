import { inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { concatLatestFrom } from '@ngrx/operators';
import { Store } from '@ngrx/store';
import { UniversitySession } from '@project-00/services-session';
import { catchError, exhaustMap, from, map, mergeMap, of, switchMap } from 'rxjs';
import { UniversityActions } from './university.actions';
import { universityFeature } from './university.feature';
import { LoadContext, loadResource, runOperation } from './university.loaders';
import { errorMessage } from './university.models';

// Effects read identity synchronously from UniversitySession (the authority) so a screen opened
// right after login never races the store's auth mirror.
export const openResource = createEffect(
  (actions$ = inject(Actions), session = inject(UniversitySession)) => actions$.pipe(
    ofType(UniversityActions.open),
    mergeMap(({ resource }) => {
      const load = UniversityActions.load({ resource });
      if (resource !== 'notes') return of(load);
      const mode = session.user()?.role === 'ADMIN' ? 'student' : 'pending';
      return of(UniversityActions.setNoteLookupMode({ mode }), load);
    }),
  ),
  { functional: true },
);

export const loadResourceData = createEffect(
  (actions$ = inject(Actions), store = inject(Store), session = inject(UniversitySession)) => actions$.pipe(
    ofType(UniversityActions.load),
    concatLatestFrom(() => store.select(universityFeature.selectCourses)),
    mergeMap(([{ resource }, cachedCourses]) => {
      const user = session.user();
      const ctx: LoadContext = {
        session, api: session.api, username: user?.username || '', role: user?.role, cachedCourses,
      };
      return from(loadResource(resource, ctx)).pipe(
        map(({ data, choiceErrors }) =>
          UniversityActions.loadSuccess({ resource, data, choiceError: choiceErrors.join('; ') })),
        catchError((reason) => of(UniversityActions.loadFailure({ resource, error: errorMessage(reason) }))),
      );
    }),
  ),
  { functional: true },
);

export const loadSelectionDetail = createEffect(
  (actions$ = inject(Actions), session = inject(UniversitySession)) => actions$.pipe(
    ofType(UniversityActions.select),
    mergeMap((selection) => {
      const api = session.api;
      const fetch = async (): Promise<ReturnType<typeof UniversityActions.detailLoaded> | null> => {
        switch (selection.resource) {
          case 'students':
            return selection.item.studentId
              ? UniversityActions.detailLoaded({ ...selection, item: await api.getStudent(selection.item.studentId) }) : null;
          case 'professors':
            return selection.item.employeeId
              ? UniversityActions.detailLoaded({ ...selection, item: await api.getProfessor(selection.item.employeeId) }) : null;
          case 'courses':
            return selection.item.courseCode
              ? UniversityActions.detailLoaded({ ...selection, item: await api.getCourse(selection.item.courseCode) }) : null;
          case 'lectures':
            return selection.item.id != null
              ? UniversityActions.detailLoaded({ ...selection, item: await api.getLecture(selection.item.id) }) : null;
          case 'notes': {
            if (selection.item.id == null) return null;
            const note = await api.getNote(selection.item.id);
            const lecture = session.user()?.role === 'PROFESSOR' && note.lectureId != null
              ? await api.getLecture(note.lectureId) : null;
            return UniversityActions.detailLoaded({ ...selection, item: note, courseCode: lecture?.courseCode || '' });
          }
        }
      };
      return from(session.run(fetch)).pipe(
        mergeMap((action) => action ? of(action) : of()),
        catchError((reason) => of(UniversityActions.detailFailure({
          resource: selection.resource, key: selection.key, error: errorMessage(reason),
        }))),
      );
    }),
  ),
  { functional: true },
);

export const executeOperation = createEffect(
  (actions$ = inject(Actions), session = inject(UniversitySession)) => actions$.pipe(
    ofType(UniversityActions.execute),
    exhaustMap(({ resource, operation, success, closeCreate }) =>
      from(session.run(() => runOperation(session.api, operation))).pipe(
        mergeMap(() => of(
          UniversityActions.executeSuccess({ resource, success, closeCreate: !!closeCreate }),
          UniversityActions.load({ resource }),
        )),
        catchError((reason) => of(UniversityActions.executeFailure({ resource, error: errorMessage(reason) }))),
      )),
  ),
  { functional: true },
);

export const findNotes = createEffect(
  (actions$ = inject(Actions), session = inject(UniversitySession)) => actions$.pipe(
    ofType(UniversityActions.findNotes),
    switchMap(({ mode, studentId, lectureId }) => {
      if (mode === 'pending' && session.user()?.role !== 'PROFESSOR') {
        return of(UniversityActions.findNotesFailure({ error: 'The pending-review endpoint is only available to professors.' }));
      }
      return from(session.run(() => {
        if (mode === 'pending') return session.api.listPendingReviewNotes();
        if (mode === 'student') return session.api.listNotesByStudent(studentId.trim());
        return session.api.listNotesByLecture(lectureId as number);
      })).pipe(
        map((notes) => UniversityActions.findNotesSuccess({ notes })),
        catchError((reason) => of(UniversityActions.findNotesFailure({ error: errorMessage(reason) }))),
      );
    }),
  ),
  { functional: true },
);
