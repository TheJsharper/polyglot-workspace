import { renderPage, settle, type } from '../testing/page-harness';
import { routes } from './remote.routes';
import { StudentsPage } from './students.page';

describe('StudentsPage (students remote)', () => {
  it('exposes a single lazily provided route', () => {
    expect(routes).toHaveLength(1);
    expect(routes[0]).toMatchObject({ path: '', component: StudentsPage });
    expect(routes[0].providers?.length).toBeGreaterThan(0);
  });

  it('shows only the signed-in student record and omits destructive actions', async () => {
    const getStudent = vi.fn().mockResolvedValue({ studentId: 'S-1', name: 'A Student', enrollmentYear: 2025 });
    const fixture = await renderPage(StudentsPage, { username: 'S-1', role: 'STUDENT' }, { getStudent });
    const root = fixture.nativeElement as HTMLElement;

    expect(getStudent).toHaveBeenCalledWith('S-1');
    expect(root.textContent).toContain('University workspace');
    expect(root.textContent).toContain('A Student');
    root.querySelector<HTMLButtonElement>('.record')?.click();
    await settle(fixture);
    expect(root.textContent).toContain('Edit student');
    expect(root.textContent).not.toContain('Delete student');
  });

  it('reports API failures and does not announce a successful create', async () => {
    const createStudent = vi.fn().mockRejectedValue(new Error('Student ID already exists'));
    const fixture = await renderPage(StudentsPage, { username: 'admin', role: 'ADMIN' }, {
      listStudents: vi.fn().mockResolvedValue([]), createStudent,
    });
    const root = fixture.nativeElement as HTMLElement;

    root.querySelector<HTMLButtonElement>('.panel-heading button')?.click();
    await settle(fixture);
    type(root, 'input[name=newStudentId]', 'S-2');
    type(root, 'input[name=newStudentName]', 'Another Student');
    root.querySelector<HTMLFormElement>('.form')?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await settle(fixture);

    expect(createStudent).toHaveBeenCalledWith(expect.objectContaining({ studentId: 'S-2', name: 'Another Student' }));
    expect(root.querySelector('[role=alert]')?.textContent).toContain('Student ID already exists');
    expect(root.querySelector('[role=status]')?.textContent || '').not.toContain('Student record created');
    expect(root.querySelector('input[name=newStudentId]')).not.toBeNull();
  });

  it('filters the list through the store', async () => {
    const fixture = await renderPage(StudentsPage, { username: 'admin', role: 'ADMIN' }, {
      listStudents: vi.fn().mockResolvedValue([{ studentId: 'S-1', name: 'Ada' }, { studentId: 'S-2', name: 'Bob' }]),
    });
    const root = fixture.nativeElement as HTMLElement;
    type(root, 'input[type=search]', 'bob');
    await settle(fixture);
    expect(Array.from(root.querySelectorAll('.record strong')).map((n) => n.textContent)).toEqual(['Bob']);
  });
});
