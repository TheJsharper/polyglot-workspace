import { computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import type { MeResponse } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';
import { render, screen, userEvent } from '@ng-native/testing';
import { expect, test, vi } from 'vitest';
import { resolveApiBaseUrl } from './api-config.ts';
import { App } from './app.ts';
import { provideAppRouter } from './app.routes.ts';

/* eslint-disable @typescript-eslint/no-explicit-any */
function fakeSession(initial: MeResponse | null, api: Record<string, any> = {}) {
  const current = signal<MeResponse | null>(initial);
  return {
    user: current.asReadonly(),
    authenticated: computed(() => !!current()),
    api,
    login: vi.fn(async (credentials: { username: string }) => {
      const user: MeResponse = { username: credentials.username, role: 'STUDENT' };
      current.set(user);
      return user;
    }),
    register: vi.fn(async () => ({})),
    logout: vi.fn(() => current.set(null)),
    run: <T>(action: () => Promise<T>) => action(),
  };
}

async function open(url: string, session: ReturnType<typeof fakeSession>) {
  const { componentRef } = await render(App, {
    providers: [provideAppRouter(), { provide: UniversitySession, useValue: session }],
  });
  await componentRef.injector.get(Router).navigateByUrl(url);
  return userEvent.setup();
}

const COURSE = { id: 1, courseCode: 'CS101', title: 'Intro to CS', credits: 6, teacherIds: ['P1'], enrolledStudentIds: [] };

test('resolves the API base URL from Expo config or the emulator default', () => {
  expect(resolveApiBaseUrl('android')).toBe('http://10.0.2.2:8080');
  expect(resolveApiBaseUrl('ios')).toBe('http://localhost:8080');
  expect(resolveApiBaseUrl('android', ' http://192.168.1.20:8080/ ')).toBe('http://192.168.1.20:8080');
});

test('redirects an anonymous visitor of University to sign in', async () => {
  await open('/university', fakeSession(null));
  expect(await screen.findByText('Sign in with your student id or employee id.')).toBeTruthy();
});

test('signs in and lands on University', async () => {
  const session = fakeSession(null);
  const user = await open('/login', session);

  await user.type(screen.getByLabelText('Username'), 'S1');
  await user.type(screen.getByLabelText('Password'), 'secret');
  await user.press(screen.getByRole('button', { name: 'Sign in' }));

  expect(session.login).toHaveBeenCalledWith({ username: 'S1', password: 'secret' });
  expect(await screen.findByText('S1 · STUDENT')).toBeTruthy();
  expect(screen.getByText('My profile')).toBeTruthy();
});

test('shows the server problem when sign in fails', async () => {
  const session = fakeSession(null);
  session.login.mockRejectedValueOnce(new Error('Bad credentials'));
  const user = await open('/login', session);

  await user.type(screen.getByLabelText('Username'), 'S1');
  await user.type(screen.getByLabelText('Password'), 'nope');
  await user.press(screen.getByRole('button', { name: 'Sign in' }));

  expect(await screen.findByText('Bad credentials')).toBeTruthy();
});

test('registers a student and signs them in', async () => {
  const session = fakeSession(null);
  const user = await open('/register', session);

  await user.type(screen.getByLabelText('Student id'), 'S9');
  await user.type(screen.getByLabelText('Full name'), 'Sam');
  await user.type(screen.getByLabelText('Password'), 'pw');
  await user.press(screen.getByRole('button', { name: 'Register' }));

  expect(session.register).toHaveBeenCalledWith(expect.objectContaining({ role: 'STUDENT', id: 'S9', name: 'Sam' }));
  expect(session.login).toHaveBeenCalledWith({ username: 'S9', password: 'pw' });
  expect(await screen.findByText('S9 · STUDENT')).toBeTruthy();
});

test('lists students for an admin with a create action', async () => {
  const session = fakeSession({ username: 'root', role: 'ADMIN' }, {
    listStudents: vi.fn(async () => [{ id: 1, studentId: 'S1', name: 'Ada', enrollmentYear: 2024 }]),
  });
  await open('/university/students', session);

  expect(await screen.findByText('Ada')).toBeTruthy();
  expect(screen.getByRole('button', { name: 'New student' })).toBeTruthy();
});

test('loads only the signed-in student rather than the staff-only list', async () => {
  const api = { getStudent: vi.fn(async () => ({ id: 1, studentId: 'S1', name: 'Ada' })) };
  await open('/university/students', fakeSession({ username: 'S1', role: 'STUDENT' }, api));
  expect(await screen.findByText('Ada')).toBeTruthy();
  expect(api.getStudent).toHaveBeenCalledWith('S1');
});

test('only admins can create or edit courses', async () => {
  const api = {
    listCourses: vi.fn(async () => [COURSE]),
    getCourse: vi.fn(async () => COURSE),
    listLecturesByCourse: vi.fn(async () => []),
    listProfessors: vi.fn(async () => []),
  };
  await open('/university/courses', fakeSession({ username: 'P1', role: 'PROFESSOR' }, api));
  expect(await screen.findByText('Intro to CS')).toBeTruthy();
  expect(screen.queryByRole('button', { name: 'New course' })).toBeNull();
  await open('/university/courses/CS101', fakeSession({ username: 'P1', role: 'PROFESSOR' }, api));
  expect(await screen.findByRole('button', { name: 'Schedule lecture' })).toBeTruthy();
  expect(screen.queryByRole('button', { name: 'Edit course' })).toBeNull();
});

test('hides admin actions from a student viewing a course and lets them enroll', async () => {
  const enrolled = { ...COURSE, enrolledStudentIds: ['S1'] };
  const api = {
    getCourse: vi.fn(async () => COURSE),
    listLecturesByCourse: vi.fn(async () => []),
    listProfessors: vi.fn(async () => []),
    enrollStudentInCourse: vi.fn(async () => enrolled),
  };
  const user = await open('/university/courses/CS101', fakeSession({ username: 'S1', role: 'STUDENT' }, api));

  expect(await screen.findByText('Intro to CS')).toBeTruthy();
  expect(screen.queryByRole('button', { name: 'Delete course' })).toBeNull();
  await user.press(screen.getByRole('button', { name: 'Enroll me' }));

  expect(api.enrollStudentInCourse).toHaveBeenCalledWith('CS101', 'S1');
  expect(await screen.findByRole('button', { name: 'Unenroll me' })).toBeTruthy();
});

test('asks for confirmation before an admin deletes a course', async () => {
  const api = {
    getCourse: vi.fn(async () => COURSE),
    listLecturesByCourse: vi.fn(async () => []),
    listProfessors: vi.fn(async () => []),
    listCourses: vi.fn(async () => []),
    deleteCourse: vi.fn(async () => undefined),
  };
  const user = await open('/university/courses/CS101', fakeSession({ username: 'root', role: 'ADMIN' }, api));

  await user.press(await screen.findByRole('button', { name: 'Delete course' }));
  expect(api.deleteCourse).not.toHaveBeenCalled();
  expect(screen.getByText('Delete Intro to CS and all of its lectures?')).toBeTruthy();

  await user.press(screen.getByRole('button', { name: 'Delete' }));
  expect(api.deleteCourse).toHaveBeenCalledWith('CS101');
});

test('shows an explicit error and retry when loading fails', async () => {
  const api = { listProfessors: vi.fn().mockRejectedValueOnce(new Error('boom')).mockResolvedValue([]) };
  const user = await open('/university/professors', fakeSession({ username: 'root', role: 'ADMIN' }, api));

  expect(await screen.findByText('boom')).toBeTruthy();
  await user.press(screen.getByRole('button', { name: 'Retry' }));
  expect(await screen.findByText('No professors found.')).toBeTruthy();
});

test('lets a professor review a note', async () => {
  const note = { id: 5, studentId: 'S1', lectureId: 3, content: 'My notes', privateNote: false };
  const api = {
    getNote: vi.fn(async () => note),
    getLecture: vi.fn(async () => ({ id: 3, courseCode: 'CS101' })),
    getCourse: vi.fn(async () => COURSE),
    reviewNote: vi.fn(async () => ({ ...note, grade: 90, reviewerId: 'P1' })),
  };
  const user = await open('/university/notes/5', fakeSession({ username: 'P1', role: 'PROFESSOR' }, api));

  await user.type(await screen.findByLabelText('Grade (0-100)'), '90');
  await user.press(screen.getByRole('button', { name: 'Submit review' }));

  expect(api.reviewNote).toHaveBeenCalledWith(5, { employeeId: 'P1', grade: 90, comment: null });
  expect(await screen.findByText('Grade 90/100 by P1')).toBeTruthy();
});
test('the drawer University entry leads anonymous users to sign in, and signed-in users to University', async () => {
  for (const [session, expected] of [
    [fakeSession(null), 'Sign in with your student id or employee id.'],
    [fakeSession({ username: 'root', role: 'ADMIN' }), 'root · ADMIN'],
  ] as const) {
    await render(App, { providers: [provideAppRouter(), { provide: UniversitySession, useValue: session }] });
    const user = userEvent.setup();
    await user.press(screen.getByRole('button', { name: 'Open navigation menu' }));
    await user.press(screen.getByRole('button', { name: 'Open University' }));
    expect(await screen.findByText(expected)).toBeTruthy();
  }
});