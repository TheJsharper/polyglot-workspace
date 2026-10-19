import type {
  AssignProfessorRequest,
  CourseResponse,
  CreateCourseRequest,
  CreateProfessorRequest,
  CreateStudentRequest,
  EditNotesRequest,
  LectureResponse,
  NotesResponse,
  ProfessorResponse,
  RescheduleLectureRequest,
  ReviewNotesRequest,
  ScheduleLectureRequest,
  StudentResponse,
  UpdateCourseRequest,
  UpdateProfessorRequest,
  UpdateStudentRequest,
  WriteNotesRequest,
} from '@project-00/services-api';

export type Resource = 'students' | 'professors' | 'courses' | 'lectures' | 'notes';
export type NoteLookupMode = 'pending' | 'student' | 'lecture';

export const RESOURCES: readonly Resource[] = ['students', 'professors', 'courses', 'lectures', 'notes'];

export interface ResourceView {
  filter: string;
  createMode: boolean;
  loading: boolean;
  error: string;
  message: string;
  choiceError: string;
}

export interface UniversityCollections {
  students: StudentResponse[];
  professors: ProfessorResponse[];
  courses: CourseResponse[];
  lectures: LectureResponse[];
  notes: NotesResponse[];
}

/** Selection payload; `key` is the resource's natural key (see the `*Key` helpers). */
export type SelectionPayload =
  | { resource: 'students'; key: string; item: StudentResponse }
  | { resource: 'professors'; key: string; item: ProfessorResponse }
  | { resource: 'courses'; key: string; item: CourseResponse }
  | { resource: 'lectures'; key: string; item: LectureResponse }
  | { resource: 'notes'; key: string; item: NotesResponse; courseCode?: string };

/** Serializable description of a mutating API call; executed by the effects. */
export type UniversityOperation =
  | { type: 'createStudent'; request: CreateStudentRequest }
  | { type: 'updateStudent'; studentId: string; request: UpdateStudentRequest }
  | { type: 'deleteStudent'; studentId: string }
  | { type: 'createProfessor'; request: CreateProfessorRequest }
  | { type: 'updateProfessor'; employeeId: string; request: UpdateProfessorRequest }
  | { type: 'deleteProfessor'; employeeId: string }
  | { type: 'createCourse'; request: CreateCourseRequest }
  | { type: 'updateCourse'; courseCode: string; request: UpdateCourseRequest }
  | { type: 'deleteCourse'; courseCode: string }
  | { type: 'assignCourseTeacher'; courseCode: string; employeeId: string }
  | { type: 'unassignCourseTeacher'; courseCode: string; employeeId: string }
  | { type: 'enrollStudentInCourse'; courseCode: string; studentId: string }
  | { type: 'unenrollStudentFromCourse'; courseCode: string; studentId: string }
  | { type: 'scheduleLecture'; request: ScheduleLectureRequest }
  | { type: 'rescheduleLecture'; lectureId: number; request: RescheduleLectureRequest }
  | { type: 'assignLectureProfessor'; lectureId: number; request: AssignProfessorRequest }
  | { type: 'cancelLecture'; lectureId: number }
  | { type: 'recordLectureAttendance'; lectureId: number; studentId: string }
  | { type: 'removeLectureAttendance'; lectureId: number; studentId: string }
  | { type: 'writeNote'; request: WriteNotesRequest }
  | { type: 'editNote'; notesId: number; request: EditNotesRequest }
  | { type: 'deleteNote'; notesId: number }
  | { type: 'reviewNote'; notesId: number; request: ReviewNotesRequest };

export function studentKey(item: StudentResponse): string {
  return item.studentId || (item.id == null ? '' : String(item.id));
}
export function professorKey(item: ProfessorResponse): string {
  return item.employeeId || (item.id == null ? '' : String(item.id));
}
export function courseKey(item: CourseResponse): string {
  return item.courseCode || (item.id == null ? '' : String(item.id));
}
export function lectureKey(item: LectureResponse): string {
  return item.id == null ? '' : String(item.id);
}
export function noteKey(item: NotesResponse): string {
  return item.id == null ? '' : String(item.id);
}

export function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}
