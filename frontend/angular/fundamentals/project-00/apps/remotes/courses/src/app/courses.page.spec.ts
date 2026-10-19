import { renderPage, settle } from '../testing/page-harness';
import { CoursesPage } from './courses.page';

const course = { courseCode: 'CS101', title: 'Course', teacherIds: ['P1'], enrolledStudentIds: ['S-1'] };

describe('CoursesPage (courses remote)', () => {
  it('does not offer admin enrollment', async () => {
    const fixture = await renderPage(CoursesPage, { username: 'admin', role: 'ADMIN' }, {
      listCourses: vi.fn().mockResolvedValue([course]),
      listProfessors: vi.fn().mockResolvedValue([]),
      listStudents: vi.fn().mockResolvedValue([]),
      getCourse: vi.fn().mockResolvedValue(course),
    });
    const root = fixture.nativeElement as HTMLElement;
    root.querySelector<HTMLButtonElement>('.record')?.click();
    await settle(fixture);
    expect(root.textContent).toContain('Assign professor');
    const buttons = Array.from(root.querySelectorAll('button')).map((b) => b.textContent?.trim());
    expect(buttons).not.toContain('Enroll me');
    expect(buttons).not.toContain('Unenroll me');
  });

  it('lets a student enroll and reloads the course list', async () => {
    const listCourses = vi.fn().mockResolvedValue([{ ...course, enrolledStudentIds: [] }]);
    const enrollStudentInCourse = vi.fn().mockResolvedValue({});
    const fixture = await renderPage(CoursesPage, { username: 'S-2', role: 'STUDENT' }, {
      listCourses, enrollStudentInCourse, getCourse: vi.fn().mockResolvedValue({ ...course, enrolledStudentIds: [] }),
    });
    const root = fixture.nativeElement as HTMLElement;
    root.querySelector<HTMLButtonElement>('.record')?.click();
    await settle(fixture);
    Array.from(root.querySelectorAll<HTMLButtonElement>('button')).find((b) => b.textContent?.trim() === 'Enroll me')?.click();
    await settle(fixture);
    expect(enrollStudentInCourse).toHaveBeenCalledWith('CS101', 'S-2');
    expect(root.textContent).toContain('Student S-2 enrolled in CS101.');
    expect(listCourses).toHaveBeenCalledTimes(2);
  });
});
