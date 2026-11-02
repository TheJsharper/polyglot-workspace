import { inject } from '@angular/core';
import { Router } from '@angular/router';
import type { CanActivateFn } from '@angular/router';
import { UniversitySession } from '@project-00/services-session';
import { Access } from './access.ts';

export const authGuard: CanActivateFn = (_route, state) => {
  if (inject(UniversitySession).authenticated()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const universityDefaultGuard: CanActivateFn = () => {
  const access = inject(Access);
  const destination = access.isAdmin()
    ? '/university/students'
    : access.isProfessor()
      ? '/university/students'
      : access.isStudent()
        ? '/university/courses'
        : access.isManager()
          ? '/university/demo-logins'
          : '/login';
  return inject(Router).parseUrl(destination);
};

export const adminRouteGuard: CanActivateFn = () => {
  const access = inject(Access);
  return access.isAdmin() || access.isManager()
    ? true
    : inject(Router).createUrlTree(['/university']);
};