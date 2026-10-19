import { renderPage, settle, type } from '../testing/page-harness';
import { ProfessorsPage } from './professors.page';

describe('ProfessorsPage (professors remote)', () => {
  it('creates a professor and closes the create form on success', async () => {
    const createProfessor = vi.fn().mockResolvedValue({});
    const listProfessors = vi.fn().mockResolvedValue([]);
    const fixture = await renderPage(ProfessorsPage, { username: 'admin', role: 'ADMIN' }, { listProfessors, createProfessor });
    const root = fixture.nativeElement as HTMLElement;

    root.querySelector<HTMLButtonElement>('.panel-heading button')?.click();
    await settle(fixture);
    type(root, 'input[name=newProfessorId]', 'P-1');
    type(root, 'input[name=newProfessorName]', 'Grace');
    type(root, 'input[name=newProfessorDepartment]', 'CS');
    type(root, 'input[name=newProfessorPassword]', 'temporary-pass');
    root.querySelector<HTMLFormElement>('.form')?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await settle(fixture);

    expect(createProfessor).toHaveBeenCalledWith({ employeeId: 'P-1', name: 'Grace', department: 'CS', password: 'temporary-pass' });
    expect(root.querySelector('[role=status]')?.textContent).toContain('Professor record created.');
    expect(root.querySelector('input[name=newProfessorPassword]')).toBeNull();
    expect(listProfessors).toHaveBeenCalledTimes(2);
  });

  it('hides admin actions from other roles', async () => {
    const fixture = await renderPage(ProfessorsPage, { username: 'S-1', role: 'STUDENT' }, {
      listProfessors: vi.fn().mockResolvedValue([{ employeeId: 'P-1', name: 'Grace' }]),
      getProfessor: vi.fn().mockResolvedValue({ employeeId: 'P-1', name: 'Grace' }),
    });
    const root = fixture.nativeElement as HTMLElement;
    root.querySelector<HTMLButtonElement>('.record')?.click();
    await settle(fixture);
    expect(root.textContent).toContain('Grace');
    expect(root.textContent).not.toContain('Add professor');
    expect(root.textContent).not.toContain('Edit professor');
  });
});
