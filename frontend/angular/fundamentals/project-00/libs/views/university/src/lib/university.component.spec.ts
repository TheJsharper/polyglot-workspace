import { TestBed } from '@angular/core/testing';
import { UniversitySession } from '@project-00/services-session';
import { UniversityComponent } from './university.component';

function configureSession(role: 'ADMIN' | 'STUDENT', api: Record<string, ReturnType<typeof vi.fn>>) {
  const session = {
    api,
    user: () => ({ username: role === 'STUDENT' ? 'S-1' : 'admin', role }),
    run: (action: () => Promise<unknown>) => action(),
    logout: vi.fn(),
  };
  TestBed.configureTestingModule({ providers: [{ provide: UniversitySession, useValue: session }] });
  return session;
}

describe('UniversityComponent', () => {
  it('shows only the signed-in student record and omits destructive actions', async () => {
    const getStudent = vi.fn().mockResolvedValue({ studentId: 'S-1', name: 'A Student', enrollmentYear: 2025 });
    const fixture = TestBed.configureTestingModule({
      providers: [{
        provide: UniversitySession,
        useValue: {
          api: { getStudent },
          user: () => ({ username: 'S-1', role: 'STUDENT' }),
          run: (action: () => Promise<unknown>) => action(),
          logout: vi.fn(),
        },
      }],
    }).createComponent(UniversityComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const root = fixture.nativeElement as HTMLElement;
    expect(getStudent).toHaveBeenCalledWith('S-1');
    expect(root.textContent).toContain('A Student');
    root.querySelector<HTMLButtonElement>('.record')?.click();
    fixture.detectChanges();
    expect(root.textContent).toContain('Edit student');
    expect(root.textContent).not.toContain('Delete student');
  });

  it('reports API failures and does not announce a successful create', async () => {
    const listStudents = vi.fn().mockResolvedValue([]);
    const createStudent = vi.fn().mockRejectedValue(new Error('Student ID already exists'));
    const api = { listStudents, createStudent };
    configureSession('ADMIN', api);
    const fixture = TestBed.createComponent(UniversityComponent);
    await fixture.whenStable();
    fixture.detectChanges();
    const root = fixture.nativeElement as HTMLElement;

    root.querySelector<HTMLButtonElement>('.panel-heading button')?.click();
    fixture.detectChanges();
    const component = fixture.componentInstance as unknown as {
      studentId: string;
      studentName: string;
    };
    component.studentId = 'S-2';
    component.studentName = 'Another Student';
    fixture.detectChanges();
    root.querySelector<HTMLFormElement>('.form')?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();
    fixture.detectChanges();

    expect(createStudent).toHaveBeenCalledWith(expect.objectContaining({ studentId: 'S-2', name: 'Another Student' }));
    expect(root.querySelector('[role=alert]')?.textContent).toContain('Student ID already exists');
    expect(root.querySelector('[role=status]')?.textContent || '').not.toContain('Student record created');
  });

  it('looks up notes by lecture and retrieves the selected note detail', async () => {
    const note = { id: 12, studentId: 'S-1', lectureId: 42, content: 'Detailed notes', privateNote: false };
    const listNotesByLecture = vi.fn().mockResolvedValue([note]);
    const getNote = vi.fn().mockResolvedValue(note);
    configureSession('ADMIN', { listStudents: vi.fn().mockResolvedValue([]), listNotesByLecture, getNote });
    const fixture = TestBed.createComponent(UniversityComponent);
    await fixture.whenStable();
    fixture.detectChanges();
    const root = fixture.nativeElement as HTMLElement;
    const notesTab = Array.from(root.querySelectorAll<HTMLButtonElement>('.resource-tabs button'))
      .find((button) => button.textContent?.includes('Notes'));
    notesTab?.click();
    await fixture.whenStable();
    fixture.detectChanges();

    const component = fixture.componentInstance as unknown as {
      noteLookupMode: { set(value: 'lecture'): void };
      lookupLectureId: number;
      findNotes: () => Promise<void>;
    };
    component.noteLookupMode.set('lecture');
    component.lookupLectureId = 42;
    await component.findNotes();
    fixture.detectChanges();
    root.querySelector<HTMLButtonElement>('.record')?.click();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(listNotesByLecture).toHaveBeenCalledWith(42);
    expect(getNote).toHaveBeenCalledWith(12);
    expect(root.textContent).toContain('Detailed notes');
  });

  it('does not offer admin enrollment, lecture scheduling, or note review', async () => {
    configureSession('ADMIN', {
      listStudents: vi.fn().mockResolvedValue([]),
      listCourses: vi.fn().mockResolvedValue([{
        courseCode: 'CS101', title: 'Course', teacherIds: ['P1'], enrolledStudentIds: ['S-1'],
      }]),
      listProfessors: vi.fn().mockResolvedValue([]),
      listLecturesByCourse: vi.fn().mockResolvedValue([]),
      getCourse: vi.fn().mockResolvedValue({
        courseCode: 'CS101', title: 'Course', teacherIds: ['P1'], enrolledStudentIds: ['S-1'],
      }),
    });
    const fixture = TestBed.createComponent(UniversityComponent);
    await fixture.whenStable();
    fixture.detectChanges();
    const root = fixture.nativeElement as HTMLElement;
    const tabs = root.querySelectorAll<HTMLButtonElement>('.resource-tabs button');
    tabs[2].click();
    await fixture.whenStable();
    fixture.detectChanges();
    root.querySelector<HTMLButtonElement>('.record')?.click();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(root.textContent).not.toContain('Enroll student');
    expect(root.textContent).not.toContain('Unenroll');
    tabs[3].click();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(root.textContent).not.toContain('Schedule lecture');
  });

  it('offers only attended lectures when a student writes notes from a fresh Notes tab', async () => {
    const listCourses = vi.fn().mockResolvedValue([{ courseCode: 'CS101', title: 'Course', enrolledStudentIds: ['S-1'] }]);
    const listLecturesByCourse = vi.fn().mockResolvedValue([
      { id: 1, courseCode: 'CS101', topic: 'Attended topic', date: '2026-01-01', attendeeIds: ['S-1'] },
      { id: 2, courseCode: 'CS101', topic: 'Missed topic', date: '2026-01-02', attendeeIds: [] },
    ]);
    configureSession('STUDENT', {
      getStudent: vi.fn().mockResolvedValue({ studentId: 'S-1', name: 'A Student' }),
      listNotesByStudent: vi.fn().mockResolvedValue([]), listCourses, listLecturesByCourse,
    });
    const fixture = TestBed.createComponent(UniversityComponent);
    await fixture.whenStable();
    const root = fixture.nativeElement as HTMLElement;
    root.querySelectorAll<HTMLButtonElement>('.resource-tabs button')[4].click();
    await fixture.whenStable();
    fixture.detectChanges();
    root.querySelector<HTMLButtonElement>('.panel-heading button')?.click();
    fixture.detectChanges();

    expect(listLecturesByCourse).toHaveBeenCalledWith('CS101');
    expect(root.textContent).toContain('Attended topic');
    expect(root.textContent).not.toContain('Missed topic');
  });
});
