import { renderPage, settle, type } from '../testing/page-harness';
import { NotesPage } from './notes.page';

describe('NotesPage (notes remote)', () => {
  it('looks up notes by lecture and retrieves the selected note detail', async () => {
    const note = { id: 12, studentId: 'S-1', lectureId: 42, content: 'Detailed notes', privateNote: false };
    const listNotesByLecture = vi.fn().mockResolvedValue([note]);
    const getNote = vi.fn().mockResolvedValue(note);
    const fixture = await renderPage(NotesPage, { username: 'admin', role: 'ADMIN' }, { listNotesByLecture, getNote });
    const root = fixture.nativeElement as HTMLElement;

    type(root, 'select[name=notesLookupMode]', 'lecture');
    await settle(fixture);
    type(root, 'input[name=lookupLectureId]', '42');
    root.querySelector<HTMLFormElement>('.note-lookup')?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await settle(fixture);
    expect(root.textContent).toContain('1 note found.');
    root.querySelector<HTMLButtonElement>('.record')?.click();
    await settle(fixture);

    expect(listNotesByLecture).toHaveBeenCalledWith(42);
    expect(getNote).toHaveBeenCalledWith(12);
    expect(root.textContent).toContain('Detailed notes');
  });

  it('offers only attended lectures when a student writes notes', async () => {
    const listLecturesByCourse = vi.fn().mockResolvedValue([
      { id: 1, courseCode: 'CS101', topic: 'Attended topic', date: '2026-01-01', attendeeIds: ['S-1'] },
      { id: 2, courseCode: 'CS101', topic: 'Missed topic', date: '2026-01-02', attendeeIds: [] },
    ]);
    const fixture = await renderPage(NotesPage, { username: 'S-1', role: 'STUDENT' }, {
      listNotesByStudent: vi.fn().mockResolvedValue([]),
      listCourses: vi.fn().mockResolvedValue([{ courseCode: 'CS101', title: 'Course', enrolledStudentIds: ['S-1'] }]),
      listLecturesByCourse,
    });
    const root = fixture.nativeElement as HTMLElement;
    expect(root.textContent).toContain('My notes');
    root.querySelector<HTMLButtonElement>('.panel-heading button')?.click();
    await settle(fixture);

    expect(listLecturesByCourse).toHaveBeenCalledWith('CS101');
    expect(root.textContent).toContain('Attended topic');
    expect(root.textContent).not.toContain('Missed topic');
  });
});
