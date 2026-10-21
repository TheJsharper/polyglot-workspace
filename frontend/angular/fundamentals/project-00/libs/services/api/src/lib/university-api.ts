import type {
  Role,
  AssignProfessorRequest,
  CourseResponse,
  CreateCourseRequest,
  CreateDemoKeysRequest,
  CreateProfessorRequest,
  CreateStudentRequest,
  DemoKeyJobResponse,
  DemoKeyResponse,
  DemoRedeemRequest,
  GoogleCompleteResponse,
  GoogleOnboardingRequest,
  EditNotesRequest,
  LectureResponse,
  LoginRequest,
  MeResponse,
  NoContent,
  NotesResponse,
  ProblemDetail,
  ProfessorResponse,
  RegisterRequest,
  RegisterResponse,
  RescheduleLectureRequest,
  ReviewNotesRequest,
  ScheduleLectureRequest,
  StudentResponse,
  TokenResponse,
  UpdateCourseRequest,
  UpdateProfessorRequest,
  UpdateStudentRequest,
  WriteNotesRequest,
} from './contracts';
import { ApiError, UnauthorizedError } from './errors';

type Method = 'GET' | 'POST' | 'PUT' | 'DELETE';
type Param = string | number;

const enc = (value: Param) => encodeURIComponent(String(value));

/**
 * Typed client for the University REST API (OpenAPI 3, bearer JWT).
 * Platform-neutral: only needs a `fetch` implementation.
 * Failures reject with ApiError (ProblemDetail in `.problem`); 401 rejects with UnauthorizedError.
 */
export class UniversityApi {
  private readonly base: string;
  private csrfToken = '';

  constructor(
    baseUrl: string,
    private readonly getToken: () => string | null,
    private readonly fetchImpl?: typeof fetch,
  ) {
    this.base = baseUrl.replace(/\/+$/, '');
  }

  // ---- Auth ----
  /** POST /api/auth/register */
  register(request: RegisterRequest): Promise<RegisterResponse> {
    return this.send('POST', '/api/auth/register', { body: request });
  }
  /** POST /api/auth/login (sent without Authorization header) */
  login(request: LoginRequest): Promise<TokenResponse> {
    return this.send('POST', '/api/auth/login', { body: request, anonymous: true });
  }
  googleComplete(ticket: string, onboarding?: GoogleOnboardingRequest): Promise<GoogleCompleteResponse> {
    return this.send('POST', `/api/auth/google/complete?ticket=${enc(ticket)}`, {
      body: onboarding,
      anonymous: true,
    });
  }
  async refresh(): Promise<TokenResponse> {
    const csrf = await this.send<{ token: string }>('GET', '/api/auth/csrf', { anonymous: true });
    this.csrfToken = csrf.token;
    return this.send('POST', '/api/auth/refresh', { anonymous: true, csrf: true });
  }
  async endSession(): Promise<void> {
    const csrf = await this.send<{ token: string }>('GET', '/api/auth/csrf', { anonymous: true });
    this.csrfToken = csrf.token;
    await this.send('POST', '/api/auth/logout', { anonymous: true, csrf: true });
  }
  /** POST /api/demo/redeem (dev profile only) */
  redeemDemo(request: DemoRedeemRequest): Promise<TokenResponse> {
    return this.send('POST', '/api/demo/redeem', { body: request, anonymous: true });
  }
  /** GET /api/auth/me */
  me(): Promise<MeResponse> {
    return this.send('GET', '/api/auth/me');
  }

  /** POST /api/auth/switch-role (dev manager only; rotates the refresh cookie) */
  switchRole(role: Role): Promise<TokenResponse> {
    return this.send('POST', '/api/auth/switch-role', { body: { role } });
  }

  // ---- Demo keys (MANAGER, dev profile only) ----
  /** GET /api/manager/demo-keys */
  listDemoKeys(): Promise<DemoKeyResponse[]> {
    return this.send('GET', '/api/manager/demo-keys');
  }
  /** POST /api/manager/demo-keys (202 Accepted with a job to poll) */
  createDemoKeys(request: CreateDemoKeysRequest): Promise<DemoKeyJobResponse> {
    return this.send('POST', '/api/manager/demo-keys', { body: request });
  }
  /** GET /api/manager/demo-keys/jobs/{id} */
  getDemoKeyJob(id: string): Promise<DemoKeyJobResponse> {
    return this.send('GET', `/api/manager/demo-keys/jobs/${enc(id)}`);
  }
  /** DELETE /api/manager/demo-keys/{id} (204; 409 once redeemed) */
  deleteDemoKey(id: number): Promise<NoContent> {
    return this.send('DELETE', `/api/manager/demo-keys/${enc(id)}`);
  }

  // ---- Students ----
  /** GET /api/students */
  listStudents(): Promise<StudentResponse[]> {
    return this.send('GET', '/api/students');
  }
  /** POST /api/students (201) */
  createStudent(request: CreateStudentRequest): Promise<StudentResponse> {
    return this.send('POST', '/api/students', { body: request });
  }
  /** GET /api/students/{studentId} */
  getStudent(studentId: string): Promise<StudentResponse> {
    return this.send('GET', `/api/students/${enc(studentId)}`);
  }
  /** PUT /api/students/{studentId} */
  updateStudent(studentId: string, request: UpdateStudentRequest): Promise<StudentResponse> {
    return this.send('PUT', `/api/students/${enc(studentId)}`, { body: request });
  }
  /** DELETE /api/students/{studentId} (204) */
  deleteStudent(studentId: string): Promise<NoContent> {
    return this.send('DELETE', `/api/students/${enc(studentId)}`);
  }

  // ---- Professors ----
  /** GET /api/professors */
  listProfessors(): Promise<ProfessorResponse[]> {
    return this.send('GET', '/api/professors');
  }
  /** POST /api/professors (201) */
  createProfessor(request: CreateProfessorRequest): Promise<ProfessorResponse> {
    return this.send('POST', '/api/professors', { body: request });
  }
  /** GET /api/professors/{employeeId} */
  getProfessor(employeeId: string): Promise<ProfessorResponse> {
    return this.send('GET', `/api/professors/${enc(employeeId)}`);
  }
  /** PUT /api/professors/{employeeId} */
  updateProfessor(employeeId: string, request: UpdateProfessorRequest): Promise<ProfessorResponse> {
    return this.send('PUT', `/api/professors/${enc(employeeId)}`, { body: request });
  }
  /** DELETE /api/professors/{employeeId} (204) */
  deleteProfessor(employeeId: string): Promise<NoContent> {
    return this.send('DELETE', `/api/professors/${enc(employeeId)}`);
  }

  // ---- Courses ----
  /** GET /api/courses */
  listCourses(): Promise<CourseResponse[]> {
    return this.send('GET', '/api/courses');
  }
  /** POST /api/courses (201) */
  createCourse(request: CreateCourseRequest): Promise<CourseResponse> {
    return this.send('POST', '/api/courses', { body: request });
  }
  /** GET /api/courses/{courseCode} */
  getCourse(courseCode: string): Promise<CourseResponse> {
    return this.send('GET', `/api/courses/${enc(courseCode)}`);
  }
  /** PUT /api/courses/{courseCode} */
  updateCourse(courseCode: string, request: UpdateCourseRequest): Promise<CourseResponse> {
    return this.send('PUT', `/api/courses/${enc(courseCode)}`, { body: request });
  }
  /** DELETE /api/courses/{courseCode} (204) */
  deleteCourse(courseCode: string): Promise<NoContent> {
    return this.send('DELETE', `/api/courses/${enc(courseCode)}`);
  }
  /** PUT /api/courses/{courseCode}/students/{studentId} */
  enrollStudentInCourse(courseCode: string, studentId: string): Promise<CourseResponse> {
    return this.send('PUT', `/api/courses/${enc(courseCode)}/students/${enc(studentId)}`);
  }
  /** DELETE /api/courses/{courseCode}/students/{studentId} */
  unenrollStudentFromCourse(courseCode: string, studentId: string): Promise<CourseResponse> {
    return this.send('DELETE', `/api/courses/${enc(courseCode)}/students/${enc(studentId)}`);
  }
  /** PUT /api/courses/{courseCode}/professors/{employeeId} */
  assignCourseTeacher(courseCode: string, employeeId: string): Promise<CourseResponse> {
    return this.send('PUT', `/api/courses/${enc(courseCode)}/professors/${enc(employeeId)}`);
  }
  /** DELETE /api/courses/{courseCode}/professors/{employeeId} */
  unassignCourseTeacher(courseCode: string, employeeId: string): Promise<CourseResponse> {
    return this.send('DELETE', `/api/courses/${enc(courseCode)}/professors/${enc(employeeId)}`);
  }

  // ---- Lectures ----
  /** GET /api/lectures?courseCode= (controller mapping params="courseCode") */
  listLecturesByCourse(courseCode: string): Promise<LectureResponse[]> {
    return this.send('GET', '/api/lectures', { query: { courseCode } });
  }
  /** GET /api/lectures?employeeId= (controller mapping params="employeeId") */
  listLecturesByProfessor(employeeId: string): Promise<LectureResponse[]> {
    return this.send('GET', '/api/lectures', { query: { employeeId } });
  }
  /** POST /api/lectures (201) */
  scheduleLecture(request: ScheduleLectureRequest): Promise<LectureResponse> {
    return this.send('POST', '/api/lectures', { body: request });
  }
  /** GET /api/lectures/{lectureId} */
  getLecture(lectureId: number): Promise<LectureResponse> {
    return this.send('GET', `/api/lectures/${enc(lectureId)}`);
  }
  /** DELETE /api/lectures/{lectureId} (204) */
  cancelLecture(lectureId: number): Promise<NoContent> {
    return this.send('DELETE', `/api/lectures/${enc(lectureId)}`);
  }
  /** PUT /api/lectures/{lectureId}/schedule */
  rescheduleLecture(lectureId: number, request: RescheduleLectureRequest): Promise<LectureResponse> {
    return this.send('PUT', `/api/lectures/${enc(lectureId)}/schedule`, { body: request });
  }
  /** PUT /api/lectures/{lectureId}/professor */
  assignLectureProfessor(lectureId: number, request: AssignProfessorRequest): Promise<LectureResponse> {
    return this.send('PUT', `/api/lectures/${enc(lectureId)}/professor`, { body: request });
  }
  /** PUT /api/lectures/{lectureId}/attendees/{studentId} */
  recordLectureAttendance(lectureId: number, studentId: string): Promise<LectureResponse> {
    return this.send('PUT', `/api/lectures/${enc(lectureId)}/attendees/${enc(studentId)}`);
  }
  /** DELETE /api/lectures/{lectureId}/attendees/{studentId} (returns the lecture) */
  removeLectureAttendance(lectureId: number, studentId: string): Promise<LectureResponse> {
    return this.send('DELETE', `/api/lectures/${enc(lectureId)}/attendees/${enc(studentId)}`);
  }

  // ---- Notes ----
  /** GET /api/notes?studentId= (controller mapping params="studentId") */
  listNotesByStudent(studentId: string): Promise<NotesResponse[]> {
    return this.send('GET', '/api/notes', { query: { studentId } });
  }
  /** GET /api/notes?lectureId= (controller mapping params="lectureId") */
  listNotesByLecture(lectureId: number): Promise<NotesResponse[]> {
    return this.send('GET', '/api/notes', { query: { lectureId } });
  }
  /** POST /api/notes (201) */
  writeNote(request: WriteNotesRequest): Promise<NotesResponse> {
    return this.send('POST', '/api/notes', { body: request });
  }
  /** GET /api/notes/pending-review */
  listPendingReviewNotes(): Promise<NotesResponse[]> {
    return this.send('GET', '/api/notes/pending-review');
  }
  /** GET /api/notes/{notesId} */
  getNote(notesId: number): Promise<NotesResponse> {
    return this.send('GET', `/api/notes/${enc(notesId)}`);
  }
  /** PUT /api/notes/{notesId} */
  editNote(notesId: number, request: EditNotesRequest): Promise<NotesResponse> {
    return this.send('PUT', `/api/notes/${enc(notesId)}`, { body: request });
  }
  /** DELETE /api/notes/{notesId} (204) */
  deleteNote(notesId: number): Promise<NoContent> {
    return this.send('DELETE', `/api/notes/${enc(notesId)}`);
  }
  /** PUT /api/notes/{notesId}/review */
  reviewNote(notesId: number, request: ReviewNotesRequest): Promise<NotesResponse> {
    return this.send('PUT', `/api/notes/${enc(notesId)}/review`, { body: request });
  }

  private async send<T>(
    method: Method,
    path: string,
    options: { body?: unknown; query?: Record<string, Param>; anonymous?: boolean; csrf?: boolean } = {},
  ): Promise<T> {
    let url = this.base + path;
    if (options.query) {
      const qs = Object.entries(options.query)
        .map(([k, v]) => `${enc(k)}=${enc(v)}`)
        .join('&');
      if (qs) url += `?${qs}`;
    }

    const headers: Record<string, string> = { Accept: 'application/json, application/problem+json' };
    if (options.csrf && this.csrfToken) headers['X-XSRF-TOKEN'] = this.csrfToken;
    const token = options.anonymous ? null : this.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const init: RequestInit = { method, headers, credentials: 'same-origin' };
    if (options.body !== undefined) {
      headers['Content-Type'] = 'application/json';
      init.body = JSON.stringify(options.body);
    }

    const doFetch = this.fetchImpl ?? globalThis.fetch.bind(globalThis);
    const response = await doFetch(url, init);

    if (!response.ok) {
      const problem = await readProblem(response);
      if (response.status === 401) throw new UnauthorizedError(problem, method, url);
      throw new ApiError(response.status, problem, method, url);
    }
    if (response.status === 204) return undefined as T;
    const text = await response.text();
    if (!text) {
      throw new Error(`Expected a JSON response from ${method} ${url} (HTTP ${response.status}).`);
    }
    return JSON.parse(text) as T;
  }
}

async function readProblem(response: Response): Promise<ProblemDetail> {
  let text = '';
  try {
    text = await response.text();
  } catch {
    // body unreadable; fall through to synthesized problem
  }
  if (text) {
    try {
      const parsed: unknown = JSON.parse(text);
      if (parsed && typeof parsed === 'object') {
        return { status: response.status, ...(parsed as ProblemDetail) };
      }
    } catch {
      return { status: response.status, title: response.statusText, detail: text };
    }
  }
  return { status: response.status, title: response.statusText };
}