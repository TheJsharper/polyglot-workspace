import type { CourseResponse, LectureResponse, UniversityApi } from '@project-00/services-api';
import type { UniversitySession } from '@project-00/services-session';
import { errorMessage, Resource, UniversityCollections, UniversityOperation } from './university.models';
export interface LoadContext {
  session: UniversitySession;
  api: UniversityApi;
  username: string;
  role: string | undefined;
  cachedCourses: CourseResponse[];
}

export interface LoadResult {
  data: Partial<UniversityCollections>;
  choiceErrors: string[];
}

async function loadChoices(ctx: LoadContext, result: LoadResult): Promise<void> {
  if (ctx.role === 'ADMIN' || ctx.role === 'PROFESSOR') {
    try { result.data.professors = await ctx.api.listProfessors(); }
    catch (reason) { result.choiceErrors.push(`Professors: ${errorMessage(reason)}`); }
  }
  if (ctx.role === 'ADMIN') {
    try { result.data.students = await ctx.api.listStudents(); }
    catch (reason) { result.choiceErrors.push(`Students: ${errorMessage(reason)}`); }
  }
}

async function loadLectures(ctx: LoadContext, result: LoadResult): Promise<void> {
  const allCourses = result.data.courses ?? (ctx.cachedCourses.length ? ctx.cachedCourses : await ctx.api.listCourses());
  result.data.courses = allCourses;
  await loadChoices(ctx, result);
  let list: LectureResponse[];
  if (ctx.role === 'PROFESSOR' && ctx.username) {
    const taught = allCourses.filter((course) => course.teacherIds?.includes(ctx.username) && course.courseCode);
    const results = await Promise.all([
      ctx.api.listLecturesByProfessor(ctx.username),
      ...taught.map((course) => ctx.api.listLecturesByCourse(course.courseCode as string)),
    ]);
    list = results.flat();
  } else {
    const courses = ctx.role === 'STUDENT'
      ? allCourses.filter((course) => (course.enrolledStudentIds || []).includes(ctx.username))
      : allCourses;
    const results = await Promise.all(courses.filter((course) => course.courseCode)
      .map((course) => ctx.api.listLecturesByCourse(course.courseCode as string)));
    list = results.flat();
  }
  const byId = new Map<number, LectureResponse>();
  for (const item of list) if (item.id != null) byId.set(item.id, item);
  result.data.lectures = [...byId.values()].sort((a, b) => `${a.date} ${a.time}`.localeCompare(`${b.date} ${b.time}`));
  if (ctx.role === 'ADMIN' || ctx.role === 'PROFESSOR') {
    try { result.data.students = await ctx.api.listStudents(); }
    catch (reason) { result.choiceErrors.push(`Students: ${errorMessage(reason)}`); }
  }
}

/** Loads everything a resource screen needs, including role-dependent lookup data. */
export async function loadResource(resource: Resource, ctx: LoadContext): Promise<LoadResult> {
  const result: LoadResult = { data: {}, choiceErrors: [] };
  await ctx.session.run(async () => {
    switch (resource) {
      case 'students':
        result.data.students = ctx.role === 'STUDENT' && ctx.username
          ? [await ctx.api.getStudent(ctx.username)]
          : await ctx.api.listStudents();
        break;
      case 'professors':
        result.data.professors = await ctx.api.listProfessors();
        break;
      case 'courses':
        result.data.courses = await ctx.api.listCourses();
        await loadChoices(ctx, result);
        break;
      case 'lectures':
        await loadLectures(ctx, result);
        break;
      case 'notes':
        if (ctx.role === 'STUDENT') {
          result.data.notes = ctx.username ? await ctx.api.listNotesByStudent(ctx.username) : [];
          try { await loadLectures(ctx, result); }
          catch (reason) { result.choiceErrors.push(`Lectures: ${errorMessage(reason)}`); }
        } else if (ctx.role === 'PROFESSOR') {
          result.data.courses = await ctx.api.listCourses();
          result.data.notes = await ctx.api.listPendingReviewNotes();
        } else {
          result.data.notes = [];
        }
        break;
    }
  });
  return result;
}

/** Maps a serializable operation onto the matching University API call. */
export function runOperation(api: UniversityApi, operation: UniversityOperation): Promise<unknown> {
  switch (operation.type) {
    case 'createStudent': return api.createStudent(operation.request);
    case 'updateStudent': return api.updateStudent(operation.studentId, operation.request);
    case 'deleteStudent': return api.deleteStudent(operation.studentId);
    case 'createProfessor': return api.createProfessor(operation.request);
    case 'updateProfessor': return api.updateProfessor(operation.employeeId, operation.request);
    case 'deleteProfessor': return api.deleteProfessor(operation.employeeId);
    case 'createCourse': return api.createCourse(operation.request);
    case 'updateCourse': return api.updateCourse(operation.courseCode, operation.request);
    case 'deleteCourse': return api.deleteCourse(operation.courseCode);
    case 'assignCourseTeacher': return api.assignCourseTeacher(operation.courseCode, operation.employeeId);
    case 'unassignCourseTeacher': return api.unassignCourseTeacher(operation.courseCode, operation.employeeId);
    case 'enrollStudentInCourse': return api.enrollStudentInCourse(operation.courseCode, operation.studentId);
    case 'unenrollStudentFromCourse': return api.unenrollStudentFromCourse(operation.courseCode, operation.studentId);
    case 'scheduleLecture': return api.scheduleLecture(operation.request);
    case 'rescheduleLecture': return api.rescheduleLecture(operation.lectureId, operation.request);
    case 'assignLectureProfessor': return api.assignLectureProfessor(operation.lectureId, operation.request);
    case 'cancelLecture': return api.cancelLecture(operation.lectureId);
    case 'recordLectureAttendance': return api.recordLectureAttendance(operation.lectureId, operation.studentId);
    case 'removeLectureAttendance': return api.removeLectureAttendance(operation.lectureId, operation.studentId);
    case 'writeNote': return api.writeNote(operation.request);
    case 'editNote': return api.editNote(operation.notesId, operation.request);
    case 'deleteNote': return api.deleteNote(operation.notesId);
    case 'reviewNote': return api.reviewNote(operation.notesId, operation.request);
  }
}
