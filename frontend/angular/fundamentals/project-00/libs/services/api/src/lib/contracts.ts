/** Contracts mirroring http://localhost:8080/v3/api-docs. Dates are ISO strings. */

export type Role = 'STUDENT' | 'PROFESSOR' | 'ADMIN';

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
export interface MeResponse {
  username?: string;
  role?: Role;
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