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

  it('lets a student read notes but not write them', async () => {
    const note = { id: 3, studentId: 'S-1', lectureId: 1, content: 'Read me', privateNote: false, grade: 90 };
    const fixture = await renderPage(NotesPage, { username: 'S-1', role: 'STUDENT' }, {
      listNotesByStudent: vi.fn().mockResolvedValue([note]),
      getNote: vi.fn().mockResolvedValue(note),
      listCourses: vi.fn().mockResolvedValue([]),
    });
    const root = fixture.nativeElement as HTMLElement;
    expect(root.textContent).toContain('My notes');
    expect(root.textContent).not.toContain('Write notes');
    root.querySelector<HTMLButtonElement>('.record')?.click();
    await settle(fixture);

    expect(root.textContent).toContain('Read me');
    expect(root.querySelector('textarea')).toBeNull();
  });
});
