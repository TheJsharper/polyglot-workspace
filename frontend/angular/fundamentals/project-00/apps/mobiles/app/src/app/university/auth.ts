import { inject } from '@angular/core';
import { Router } from '@angular/router';
import type { CanActivateFn } from '@angular/router';
import { UniversitySession } from '@project-00/services-session';

export const authGuard: CanActivateFn = (_route, state) => {
  if (inject(UniversitySession).authenticated()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};