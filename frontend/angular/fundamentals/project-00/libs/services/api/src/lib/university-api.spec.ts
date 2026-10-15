import { ApiError, UnauthorizedError, UniversityApi } from '../index';

function mockFetch(status: number, body?: unknown, contentType = 'application/json') {
  return vi.fn(async (_url: string, _init?: RequestInit) =>
    new Response(body === undefined ? null : typeof body === 'string' ? body : JSON.stringify(body), {
      status,
      headers: { 'Content-Type': contentType },
    }),
  );
}
const make = (f: ReturnType<typeof mockFetch>, token: string | null = 'tok') =>
  new UniversityApi('http://localhost:8080/', () => token, f as unknown as typeof fetch);
const call = (f: ReturnType<typeof mockFetch>) => ({
  url: f.mock.calls[0][0],
  init: f.mock.calls[0][1] as RequestInit,
});
const auth = (init: RequestInit) => (init.headers as Record<string, string>)['Authorization'];

describe('UniversityApi', () => {
  it('sends bearer auth, JSON body and parses the response', async () => {
    const f = mockFetch(201, { studentId: 's1', name: 'Ann' });
    const res = await make(f).createStudent({ studentId: 's1', name: 'Ann' });
    const { url, init } = call(f);
    expect(url).toBe('http://localhost:8080/api/students');
    expect(init.method).toBe('POST');
    expect(auth(init)).toBe('Bearer tok');
    expect(init.body).toBe(JSON.stringify({ studentId: 's1', name: 'Ann' }));
    expect(res.name).toBe('Ann');
  });

  it('omits Authorization for login and when there is no token', async () => {
    const f = mockFetch(200, { accessToken: 'a' });
    await make(f, 'tok').login({ username: 'u', password: 'p' });
    expect(auth(call(f).init)).toBeUndefined();
    const g = mockFetch(200, { username: 'u' });
    await make(g, null).me();
    expect(auth(call(g).init)).toBeUndefined();
  });

  it('URL-encodes path and query params', async () => {
    const f = mockFetch(200, {});
    await make(f).getStudent('a/b c?#');
    expect(call(f).url).toBe('http://localhost:8080/api/students/a%2Fb%20c%3F%23');
    const g = mockFetch(200, []);
    await make(g).listNotesByStudent('x&y');
    expect(call(g).url).toBe('http://localhost:8080/api/notes?studentId=x%26y');
    const h = mockFetch(200, {});
    await make(h).recordLectureAttendance(3, 'S 1');
    expect(call(h).url).toBe('http://localhost:8080/api/lectures/3/attendees/S%201');
  });

  it('returns undefined for 204', async () => {
    const f = mockFetch(204);
    await expect(make(f).deleteCourse('C1')).resolves.toBeUndefined();
    expect(call(f).init.method).toBe('DELETE');
  });

  it('rejects an empty successful response when JSON is required', async () => {
    const f = mockFetch(200);
    await expect(make(f).getCourse('C1')).rejects.toThrow('Expected a JSON response');
  });

  it('throws ApiError with numeric status and ProblemDetail', async () => {
    const f = mockFetch(409, { title: 'Conflict', status: 409, detail: 'in use' }, 'application/problem+json');
    const err = await make(f).deleteProfessor('e1').catch((e) => e);
    expect(err).toBeInstanceOf(ApiError);
    expect(err).not.toBeInstanceOf(UnauthorizedError);
    expect(err.status).toBe(409);
    expect(err.problem.detail).toBe('in use');
    expect(err.message).toBe('in use');
  });

  it('throws UnauthorizedError (an ApiError) on 401 and synthesizes problem for non-JSON', async () => {
    const f = mockFetch(401, 'nope', 'text/plain');
    const err = await make(f).me().catch((e) => e);
    expect(err).toBeInstanceOf(UnauthorizedError);
    expect(err).toBeInstanceOf(ApiError);
    expect(err.status).toBe(401);
    expect(err.problem.detail).toBe('nope');
  });

  it('maps every operation to the right method and path', async () => {
    const cases: [(a: UniversityApi) => Promise<unknown>, string, string][] = [
      [(a) => a.register({ id: 'i', password: 'p' }), 'POST', '/api/auth/register'],
      [(a) => a.login({ username: 'u', password: 'p' }), 'POST', '/api/auth/login'],
      [(a) => a.me(), 'GET', '/api/auth/me'],
      [(a) => a.listStudents(), 'GET', '/api/students'],
      [(a) => a.createStudent({ studentId: 's', name: 'n' }), 'POST', '/api/students'],
      [(a) => a.getStudent('s'), 'GET', '/api/students/s'],
      [(a) => a.updateStudent('s', { name: 'n' }), 'PUT', '/api/students/s'],
      [(a) => a.deleteStudent('s'), 'DELETE', '/api/students/s'],
      [(a) => a.listProfessors(), 'GET', '/api/professors'],
      [(a) => a.createProfessor({ employeeId: 'e', name: 'n', department: 'd', password: 'p' }), 'POST', '/api/professors'],
      [(a) => a.getProfessor('e'), 'GET', '/api/professors/e'],
      [(a) => a.updateProfessor('e', { name: 'n', department: 'd' }), 'PUT', '/api/professors/e'],
      [(a) => a.deleteProfessor('e'), 'DELETE', '/api/professors/e'],
      [(a) => a.listCourses(), 'GET', '/api/courses'],
      [(a) => a.createCourse({ courseCode: 'c', title: 't' }), 'POST', '/api/courses'],
      [(a) => a.getCourse('c'), 'GET', '/api/courses/c'],
      [(a) => a.updateCourse('c', { title: 't' }), 'PUT', '/api/courses/c'],
      [(a) => a.deleteCourse('c'), 'DELETE', '/api/courses/c'],
      [(a) => a.enrollStudentInCourse('c', 's'), 'PUT', '/api/courses/c/students/s'],
      [(a) => a.unenrollStudentFromCourse('c', 's'), 'DELETE', '/api/courses/c/students/s'],
      [(a) => a.assignCourseTeacher('c', 'e'), 'PUT', '/api/courses/c/professors/e'],
      [(a) => a.unassignCourseTeacher('c', 'e'), 'DELETE', '/api/courses/c/professors/e'],
      [(a) => a.listLecturesByCourse('c'), 'GET', '/api/lectures?courseCode=c'],
      [(a) => a.listLecturesByProfessor('e'), 'GET', '/api/lectures?employeeId=e'],
      [(a) => a.scheduleLecture({ courseCode: 'c', employeeId: 'e', date: '2026-01-01', time: '10:00', topic: 't', room: 'r' }), 'POST', '/api/lectures'],
      [(a) => a.getLecture(1), 'GET', '/api/lectures/1'],
      [(a) => a.cancelLecture(1), 'DELETE', '/api/lectures/1'],
      [(a) => a.rescheduleLecture(1, { date: 'd', time: 't', room: 'r' }), 'PUT', '/api/lectures/1/schedule'],
      [(a) => a.assignLectureProfessor(1, { employeeId: 'e' }), 'PUT', '/api/lectures/1/professor'],
      [(a) => a.recordLectureAttendance(1, 's'), 'PUT', '/api/lectures/1/attendees/s'],
      [(a) => a.removeLectureAttendance(1, 's'), 'DELETE', '/api/lectures/1/attendees/s'],
      [(a) => a.listNotesByStudent('s'), 'GET', '/api/notes?studentId=s'],
      [(a) => a.listNotesByLecture(1), 'GET', '/api/notes?lectureId=1'],
      [(a) => a.writeNote({ studentId: 's', lectureId: 1, content: 'c' }), 'POST', '/api/notes'],
      [(a) => a.listPendingReviewNotes(), 'GET', '/api/notes/pending-review'],
      [(a) => a.getNote(2), 'GET', '/api/notes/2'],
      [(a) => a.editNote(2, { content: 'c' }), 'PUT', '/api/notes/2'],
      [(a) => a.deleteNote(2), 'DELETE', '/api/notes/2'],
      [(a) => a.reviewNote(2, { employeeId: 'e', grade: 5 }), 'PUT', '/api/notes/2/review'],
    ];
    expect(cases).toHaveLength(39);
    for (const [run, method, path] of cases) {
      const f = mockFetch(200, {});
      await run(make(f));
      expect(call(f).init.method).toBe(method);
      expect(call(f).url).toBe(`http://localhost:8080${path}`);
    }
  });

  it('normalizes empty and slash-only base URLs to relative /api paths', async () => {
    for (const base of ['', '/', '//']) {
      const f = mockFetch(200, {});
      await new UniversityApi(base, () => null, f as unknown as typeof fetch).listCourses();
      expect(call(f).url).toBe('/api/courses');
    }
    const g = mockFetch(200, {});
    await new UniversityApi('http://10.0.2.2:8080/', () => null, g as unknown as typeof fetch).listCourses();
    expect(call(g).url).toBe('http://10.0.2.2:8080/api/courses');
  });

  it('register sends bearer only when a session token exists; login never does', async () => {
    const a = mockFetch(201, {});
    await make(a, 'admin').register({ id: 'i', password: 'p' });
    expect(auth(call(a).init)).toBe('Bearer admin');
    const b = mockFetch(201, {});
    await make(b, null).register({ id: 'i', password: 'p' });
    expect(auth(call(b).init)).toBeUndefined();
  });

  it('exposes a UI-friendly message, falling back to title then status line', async () => {
    const e1 = await make(mockFetch(400, { title: 'Bad Request' })).me().catch((e) => e);
    expect(e1.message).toBe('Bad Request');
    const e2 = await make(mockFetch(500, '', 'text/plain')).me().catch((e) => e);
    expect(e2.message).toContain('500');
  });
});