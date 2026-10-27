/** Contracts mirroring http://localhost:8080/v3/api-docs. Dates are ISO strings. */

export type Role = 'STUDENT' | 'PROFESSOR' | 'ADMIN' | 'MANAGER';

/** RFC 9457 problem detail returned by every error response. */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  properties?: Record<string, unknown>;
  [extension: string]: unknown;
}

// Auth
export interface RegisterRequest {
  role?: Role;
  id: string;
  name?: string;
  enrollmentYear?: number;
  department?: string;
  email?: string;
  password: string;
}
export interface RegisterResponse {
  username?: string;
  role?: Role;
  name?: string;
}
export interface LoginRequest {
  username: string;
  password: string;
}
export interface TokenResponse {
  accessToken?: string;
  tokenType?: string;
  expiresIn?: number;
}
export interface GoogleOnboardingRequest {
  role: 'STUDENT' | 'PROFESSOR';
  enrollmentYear?: number;
  department?: string;
}
export interface GoogleCompleteResponse {
  session?: TokenResponse;
  onboardingRequired: boolean;
}
export interface DemoRedeemRequest {
  token: string;
  persona: 'STUDENT' | 'PROFESSOR';
}
export interface MeResponse {
  username?: string;
  role?: Role;
  /** Google name, student/professor name, or the username. */
  displayName?: string;
  /** HTTPS Google avatar (googleusercontent.com) or null. */
  pictureUrl?: string | null;
  /** Roles the caller can switch to (dev manager only); empty otherwise. */
  availableRoles?: Role[];
  /** True for a generated, read-only recruiter demo login. */
  demo?: boolean;
  /** When the demo login expires (ISO-8601), or null. */
  demoExpiresAt?: string | null;
}

// Demo logins (ADMIN or MANAGER). The password is only returned once, on creation.
export type DemoLoginPersona = 'STUDENT' | 'PROFESSOR' | 'ADMIN';
export interface CreateDemoLoginRequest {
  persona: DemoLoginPersona;
  /** 1 to 30, default 7. */
  days?: number;
  label?: string;
}
export interface DemoLoginResponse {
  username: string;
  persona: DemoLoginPersona;
  label?: string | null;
  createdBy: string;
  createdAt: string;
  expiresAt: string;
}
export interface CreatedDemoLoginResponse extends DemoLoginResponse {
  password: string;
}

// Demo keys (MANAGER, dev profile only). The private key is never returned.
export interface CreateDemoKeysRequest {
  /** 1 to 10. */
  count: number;
  /** 1 to 30, default 10. */
  days?: number;
  label?: string;
}
export interface DemoKeyResponse {
  id: number;
  label: string;
  publicKey: string;
  token: string;
  createdAt: string;
  notBefore: string;
  expiresAt: string;
  redeemed: boolean;
  redeemedAt?: string | null;
  redeemCount: number;
  createdBy: string;
}
/** Key creation is asynchronous: POST answers 202 with a job to poll until it is DONE or FAILED. */
export interface DemoKeyJobResponse {
  id: string;
  status: 'PENDING' | 'RUNNING' | 'DONE' | 'FAILED';
  keys: DemoKeyResponse[];
  error?: string | null;
  createdAt: string;
}

// Students
export interface CreateStudentRequest {
  studentId: string;
  name: string;
  enrollmentYear?: number;
}
export interface UpdateStudentRequest {
  name: string;
  enrollmentYear?: number;
}
export interface StudentResponse {
  id?: number;
  studentId?: string;
  name?: string;
  enrollmentYear?: number;
}

// Professors
export interface CreateProfessorRequest {
  employeeId: string;
  name: string;
  department: string;
  password: string;
}
export interface UpdateProfessorRequest {
  name: string;
  department: string;
}
export interface ProfessorResponse {
  id?: number;
  employeeId?: string;
  name?: string;
  department?: string;
}

// Courses
export interface CreateCourseRequest {
  courseCode: string;
  title: string;
  credits?: number;
}
export interface UpdateCourseRequest {
  title: string;
  credits?: number;
}
export interface CourseResponse {
  id?: number;
  courseCode?: string;
  title?: string;
  credits?: number;
  teacherIds?: string[];
  enrolledStudentIds?: string[];
}

// Lectures
export interface ScheduleLectureRequest {
  courseCode: string;
  employeeId: string;
  /** yyyy-MM-dd */
  date: string;
  /** HH:mm[:ss] */
  time: string;
  topic: string;
  room: string;
}
export interface RescheduleLectureRequest {
  date: string;
  time: string;
  room: string;
}
export interface AssignProfessorRequest {
  employeeId: string;
}
export interface LectureResponse {
  id?: number;
  courseCode?: string;
  employeeId?: string;
  date?: string;
  time?: string;
  topic?: string;
  room?: string;
  attendeeIds?: string[];
}

// Notes
export interface WriteNotesRequest {
  studentId: string;
  lectureId: number;
  content: string;
  privateNote?: boolean;
}
export interface EditNotesRequest {
  content: string;
  privateNote?: boolean;
}
export interface ReviewNotesRequest {
  employeeId: string;
  grade: number;
  comment?: string | null;
}
export interface NotesResponse {
  id?: number;
  studentId?: string;
  lectureId?: number;
  content?: string;
  privateNote?: boolean;
  /** ISO date-time */
  timestamp?: string;
  reviewerId?: string | null;
  grade?: number | null;
  reviewComment?: string | null;
}

/** Typed result of 204 No Content operations. */
export type NoContent = void;