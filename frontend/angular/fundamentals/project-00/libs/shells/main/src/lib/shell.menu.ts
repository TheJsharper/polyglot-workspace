import type { MenuItem } from '@project-00/services-core';

/** One sidebar entry per University resource; each route is served by its own Native Federation remote. */
export const SHELL_MENU: MenuItem[] = [
  { id: 'students', label: 'Students', icon: 'school', route: '/university/students' },
  { id: 'professors', label: 'Professors', icon: 'person', route: '/university/professors' },
  { id: 'courses', label: 'Courses', icon: 'menu_book', route: '/university/courses' },
  { id: 'lectures', label: 'Lectures', icon: 'event', route: '/university/lectures' },
  { id: 'notes', label: 'Notes & reviews', icon: 'rate_review', route: '/university/notes' },
];

const ALLOWED: Record<string, readonly string[]> = {
  STUDENT: ['courses', 'lectures', 'notes'],
  PROFESSOR: ['students', 'courses', 'lectures', 'notes'],
  ADMIN: ['students', 'professors', 'courses', 'lectures', 'notes'],
};

export function allowedResources(role: string | null | undefined): readonly string[] {
  return (role && ALLOWED[role]) || [];
}

/** Menu for a role; professors see "My Students" (only students of the courses they teach). */
export function menuForRole(items: readonly MenuItem[], role: string | null | undefined): MenuItem[] {
  const allowed = allowedResources(role);
  return items
    .filter((item) => allowed.includes(item.id))
    .map((item) => (role === 'PROFESSOR' && item.id === 'students' ? { ...item, label: 'My Students' } : item));
}
