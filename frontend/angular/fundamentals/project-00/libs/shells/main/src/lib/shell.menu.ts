import type { MenuItem } from '@project-00/services-core';

/** One sidebar entry per University resource; each route is served by its own Native Federation remote. */
export const SHELL_MENU: MenuItem[] = [
  { id: 'students', label: 'Students', icon: 'school', route: '/university/students' },
  { id: 'professors', label: 'Professors', icon: 'person', route: '/university/professors' },
  { id: 'courses', label: 'Courses', icon: 'menu_book', route: '/university/courses' },
  { id: 'lectures', label: 'Lectures', icon: 'event', route: '/university/lectures' },
  { id: 'notes', label: 'Notes & reviews', icon: 'rate_review', route: '/university/notes' },
  { id: 'demo-keys', label: 'Demo keys', icon: 'vpn_key', route: '/university/demo-keys' },
  { id: 'demo-logins', label: 'Demo logins', icon: 'badge', route: '/university/demo-logins' },
];

const ALLOWED: Record<string, readonly string[]> = {
  STUDENT: ['courses', 'lectures', 'notes'],
  PROFESSOR: ['students', 'courses', 'lectures', 'notes'],
  ADMIN: ['students', 'professors', 'courses', 'lectures', 'notes', 'demo-logins'],
  MANAGER: ['demo-keys', 'demo-logins'],
};

/** Pages that generated recruiter demo logins never see. */
const HIDDEN_FOR_DEMO: readonly string[] = ['demo-keys', 'demo-logins'];

/**
 * @param role the caller's role
 * @param demo whether the caller is a generated, read-only recruiter demo login
 */
export function allowedResources(role: string | null | undefined, demo = false): readonly string[] {
  const allowed = (role && ALLOWED[role]) || [];
  return demo ? allowed.filter((id) => !HIDDEN_FOR_DEMO.includes(id)) : allowed;
}

/** Menu for a role; professors see "My Students" (only students of the courses they teach). */
export function menuForRole(items: readonly MenuItem[], role: string | null | undefined, demo = false): MenuItem[] {
  const allowed = allowedResources(role, demo);
  return items
    .filter((item) => allowed.includes(item.id))
    .map((item) => (role === 'PROFESSOR' && item.id === 'students' ? { ...item, label: 'My Students' } : item));
}
