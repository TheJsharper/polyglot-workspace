import { renderPage, settle } from '../testing/page-harness';
import { LecturesPage } from './lectures.page';

describe('LecturesPage (lectures remote)', () => {
  it('offers lecture scheduling to admins', async () => {
    const fixture = await renderPage(LecturesPage, { username: 'admin', role: 'ADMIN' }, {
      listCourses: vi.fn().mockResolvedValue([{ courseCode: 'CS101', teacherIds: ['P1'] }]),
      listProfessors: vi.fn().mockResolvedValue([]),
      listStudents: vi.fn().mockResolvedValue([]),
      listLecturesByCourse: vi.fn().mockResolvedValue([]),
    });
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Schedule lecture');
  });

  it('lets a teaching professor manage attendance for a lecture', async () => {
    const lecture = { id: 5, courseCode: 'CS101', employeeId: 'P1', topic: 'Intro', date: '2026-01-01', time: '09:00:00', room: 'A', attendeeIds: ['S-1'] };
    const fixture = await renderPage(LecturesPage, { username: 'P1', role: 'PROFESSOR' }, {
      listCourses: vi.fn().mockResolvedValue([{ courseCode: 'CS101', title: 'Course', teacherIds: ['P1'] }]),
      listProfessors: vi.fn().mockResolvedValue([]),
      listStudents: vi.fn().mockResolvedValue([{ studentId: 'S-1', name: 'Ada' }]),
      listLecturesByProfessor: vi.fn().mockResolvedValue([lecture]),
      listLecturesByCourse: vi.fn().mockResolvedValue([lecture]),
      getLecture: vi.fn().mockResolvedValue(lecture),
    });
    const root = fixture.nativeElement as HTMLElement;
    expect(root.textContent).toContain('Schedule lecture');
    expect(root.querySelectorAll('.record')).toHaveLength(1);
    root.querySelector<HTMLButtonElement>('.record')?.click();
    await settle(fixture);
    expect(root.textContent).toContain('Record attendance');
    expect(root.querySelector<HTMLInputElement>('input[name=editLectureTime]')?.value).toBe('09:00');
  });
});
