import { computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import type { MeResponse } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';
import { ColorScheme } from '@ng-native/device';
import { Storage } from '@ng-native/expo/async-storage';
import { Store } from '@ng-native/expo/store';
import * as Crypto from 'expo-crypto';
import * as WebBrowser from 'expo-web-browser';
import { render, screen, userEvent } from '@ng-native/testing';
import { expect, test, vi } from 'vitest';
import { resolveApiBaseUrl } from './api-config.ts';
import { App } from './app.ts';
import { provideAppRouter } from './app.routes.ts';

vi.mock('expo-crypto', () => ({
  getRandomBytesAsync: vi.fn(async () => new Uint8Array(32).fill(1)),
  digestStringAsync: vi.fn(async () => `${'a'.repeat(43)}=`),
  CryptoDigestAlgorithm: { SHA256: 'SHA-256' },
  CryptoEncoding: { BASE64: 'base64' },
}));
vi.mock('expo-linking', () => ({
  parse: vi.fn((url: string) => ({
    queryParams: Object.fromEntries(new URL(url).searchParams.entries()),
  })),
}));
vi.mock('expo-web-browser', () => ({
  openAuthSessionAsync: vi.fn(),
}));

/* eslint-disable @typescript-eslint/no-explicit-any */
function fakeSession(initial: MeResponse | null, api: Record<string, any> = {}) {
  const current = signal<MeResponse | null>(initial);
  return {
    user: current.asReadonly(),
    authenticated: computed(() => !!current()),
    api: {
      googleStatus: vi.fn(async () => ({ enabled: false })),
      googleMobileStartUrl: vi.fn(() => 'https://university.example/api/auth/google/mobile/start'),
      listCourses: vi.fn(async () => []),
      listStudents: vi.fn(async () => []),
      listProfessors: vi.fn(async () => []),
      ...api,
    },
    loginMobile: vi.fn(async (credentials: { username: string }) => {
      const user: MeResponse = { username: credentials.username, role: 'STUDENT' };
      current.set(user);
      return user;
    }),
    completeGoogleMobile: vi.fn(async () => {
      const user: MeResponse = { username: 'google-student', role: 'STUDENT' };
      current.set(user);
      return { session: { accessToken: 'google-token' }, onboardingRequired: false };
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

test('resolves the API base URL from Expo config or the localhost default', () => {
  expect(resolveApiBaseUrl('android')).toBe('http://localhost:8080');
  expect(resolveApiBaseUrl('ios')).toBe('http://localhost:8080');
  expect(resolveApiBaseUrl('android', ' http://192.168.1.20:8080/ ')).toBe('http://192.168.1.20:8080');
});

test('redirects an anonymous visitor of University to sign in', async () => {
  await open('/university', fakeSession(null));
  expect(await screen.findByText(/Use the username provided by your University administrator/)).toBeTruthy();
});

test('shows Google sign-in only when the API enables it', async () => {
  const session = fakeSession(null, {
    googleStatus: vi.fn(async () => ({ enabled: true })),
  });
  await open('/login', session);
  expect(await screen.findByRole('button', { name: 'Continue with Google' })).toBeTruthy();
});

test('exchanges a state-verified Google callback for a mobile session', async () => {
  const session = fakeSession(null, {
    googleStatus: vi.fn(async () => ({ enabled: true })),
  });
  vi.mocked(WebBrowser.openAuthSessionAsync).mockResolvedValueOnce({
    type: 'success',
    url: 'app://sso/callback?ticket=one-time-ticket&state=' + '01'.repeat(32),
  });
  const user = await open('/login', session);

  await user.press(await screen.findByRole('button', { name: 'Continue with Google' }));

  expect(session.api.googleMobileStartUrl).toHaveBeenCalledWith('01'.repeat(32), 'a'.repeat(43));
  expect(session.completeGoogleMobile).toHaveBeenCalledWith('one-time-ticket', '01'.repeat(32));
  expect(await screen.findByText('google-student')).toBeTruthy();
  expect(Crypto.getRandomBytesAsync).toHaveBeenCalledTimes(2);
});

test('lets the user show and hide the sign-in password with an eye icon', async () => {
  const user = await open('/login', fakeSession(null));
  const password = screen.getByLabelText('Password');

  expect(password.props.secureTextEntry).toBe(true);
  expect(screen.queryByText('Show')).toBeNull();
  await user.press(screen.getByRole('button', { name: 'Show Password' }));
  expect(screen.getByLabelText('Password').props.secureTextEntry).toBe(false);
  expect(screen.queryByText('Hide')).toBeNull();
  await user.press(screen.getByRole('button', { name: 'Hide Password' }));
  expect(screen.getByLabelText('Password').props.secureTextEntry).toBe(true);
});

test('signs in and lands on University', async () => {
  const session = fakeSession(null);
  const user = await open('/login', session);

  await user.type(screen.getByLabelText('Username'), 'S1');
  await user.type(screen.getByLabelText('Password'), 'secret');
  await user.press(screen.getByRole('button', { name: 'Sign in' }));

  expect(session.loginMobile).toHaveBeenCalledWith({ username: 'S1', password: 'secret' });
  expect(await screen.findByText('S1')).toBeTruthy();
  expect(screen.getByText('STUDENT')).toBeTruthy();
  expect(screen.getByText('No courses found.')).toBeTruthy();
}, 15000);

test('shows the server problem when sign in fails', async () => {
  const session = fakeSession(null);
  session.loginMobile.mockRejectedValueOnce(new Error('Bad credentials'));
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
  const passwordFields = screen.getAllByLabelText('Password');
  const registerPassword = passwordFields[passwordFields.length - 1];
  if (!registerPassword) {
    throw new Error('Register password input not found.');
  }
  await user.type(registerPassword, 'pw');
  await user.press(screen.getByRole('button', { name: 'Register' }));

  expect(session.register).toHaveBeenCalledWith(expect.objectContaining({ role: 'STUDENT', id: 'S9', name: 'Sam' }));
  expect(session.loginMobile).toHaveBeenCalledWith({ username: 'S9', password: 'pw' });
  expect(await screen.findByText('S9')).toBeTruthy();
  expect(screen.getByText('STUDENT')).toBeTruthy();
  expect(screen.getByText('No courses found.')).toBeTruthy();
}, 15000);

test('lists students for an admin with a create action', async () => {
  const listStudents = vi.fn(async () => [{ id: 1, studentId: 'S1', name: 'Ada', enrollmentYear: 2024 }]);
  const session = fakeSession({ username: 'root', role: 'ADMIN' }, {
    listStudents,
  });
  const user = await open('/university/students', session);

  expect(await screen.findByText('Ada')).toBeTruthy();
  expect(screen.getByRole('button', { name: 'New student' })).toBeTruthy();
  const callsBeforeRefresh = listStudents.mock.calls.length;
  await user.press(screen.getByRole('button', { name: 'Refresh students' }));
  expect(listStudents).toHaveBeenCalledTimes(callsBeforeRefresh + 1);
});

test('loads only the signed-in student rather than the staff-only list', async () => {
  const api = { getStudent: vi.fn(async () => ({ id: 1, studentId: 'S1', name: 'Ada' })) };
  await open('/university/students', fakeSession({ username: 'S1', role: 'STUDENT' }, api));
  expect(await screen.findByText('Ada')).toBeTruthy();
  expect(api.getStudent).toHaveBeenCalledWith('S1');
}, 15000);

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
test('redirects anonymous users from University resources to sign in', async () => {
  await open('/university/students', fakeSession(null));
  expect(await screen.findByText(/Use the username provided by your University administrator/)).toBeTruthy();
});

test('restores the saved theme and cycles System, Light and Dark from the header', async () => {
  const disk = new Map([['theme-choice', JSON.stringify('dark')]]);
  const store = new Store({
    get: async (key) => disk.get(key) ?? null,
    set: async (key, value) => void disk.set(key, value),
    remove: async (key) => void disk.delete(key),
  });
  const { componentRef } = await render(App, {
    providers: [provideAppRouter(), { provide: UniversitySession, useValue: fakeSession(null) }, { provide: Storage, useValue: store }],
  });
  await componentRef.injector.get(Router).navigateByUrl('/login');
  const user = userEvent.setup();
  const scheme = componentRef.injector.get(ColorScheme);
  const setScheme = vi.spyOn(scheme, 'set');

  const toggle = await screen.findByRole('button', { name: 'Theme: Dark. Change theme' });
  await user.press(toggle);
  expect(screen.getByRole('button', { name: 'Theme: System. Change theme' })).toBeTruthy();
  expect(setScheme).toHaveBeenLastCalledWith(null);
  await user.press(screen.getByRole('button', { name: 'Theme: System. Change theme' }));
  expect(setScheme).toHaveBeenLastCalledWith('light');
  await store.flush();
  expect(disk.get('theme-choice')).toBe(JSON.stringify('light'));
});


test('admins land on Students from the app root and see the web-aligned University menu', async () => {
  const api = { listStudents: vi.fn(async () => []) };
  await render(App, {
    providers: [provideAppRouter(), { provide: UniversitySession, useValue: fakeSession({ username: 'root', role: 'ADMIN' }, api) }],
  });
  const user = userEvent.setup();
  expect(await screen.findByRole('button', { name: 'New student' })).toBeTruthy();
  await user.press(screen.getByRole('button', { name: 'Open University navigation' }));
  expect(screen.getByRole('button', { name: 'Open Students' }).props.accessibilityState).toMatchObject({ selected: true });
  expect(screen.getByRole('button', { name: 'Open Professors' }).props.accessibilityState).toMatchObject({ selected: false });
  expect(screen.getByRole('button', { name: 'Open Courses' })).toBeTruthy();
  expect(screen.getByRole('button', { name: 'Open Lectures' })).toBeTruthy();
  expect(screen.getByRole('button', { name: 'Open Notes & reviews' })).toBeTruthy();
  expect(screen.getByRole('button', { name: 'Open Demo logins' })).toBeTruthy();
});

test('admins can manage demo logins from the University resource drawer', async () => {
  const activeLogin = {
    username: 'demo-student-1',
    persona: 'STUDENT' as const,
    label: 'Recruiter',
    createdBy: 'university_admin',
    createdAt: '2026-10-31T10:00:00Z',
    expiresAt: '2026-11-07T10:00:00Z',
  };
  const createdLogin = { ...activeLogin, username: 'demo-student-2', password: 'one-time-password' };
  const api = {
    listDemoLogins: vi.fn(async () => [activeLogin]),
    createDemoLogin: vi.fn(async () => createdLogin),
  };
  const user = await open('/university/students', fakeSession({ username: 'university_admin', role: 'ADMIN' }, api));

  await user.press(screen.getByRole('button', { name: 'Open University navigation' }));
  await user.press(screen.getByRole('button', { name: 'Open Demo logins' }));

  expect(await screen.findByText('demo-student-1')).toBeTruthy();
  await user.press(screen.getByRole('button', { name: 'Generate login' }));
  expect(api.createDemoLogin).toHaveBeenCalledWith({ persona: 'STUDENT', days: 7, label: undefined });
  expect(await screen.findByText('one-time-password')).toBeTruthy();
});