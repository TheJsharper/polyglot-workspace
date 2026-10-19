import type { MenuItem } from '@project-00/services-core';

/** One sidebar entry per University resource; each route is served by its own Native Federation remote. */
export const SHELL_MENU: MenuItem[] = [
  { id: 'students', label: 'Students', icon: 'school', route: '/university/students' },
  { id: 'professors', label: 'Professors', icon: 'person', route: '/university/professors' },
  { id: 'courses', label: 'Courses', icon: 'menu_book', route: '/university/courses' },
  { id: 'lectures', label: 'Lectures', icon: 'event', route: '/university/lectures' },
  { id: 'notes', label: 'Notes & reviews', icon: 'rate_review', route: '/university/notes' },
];
