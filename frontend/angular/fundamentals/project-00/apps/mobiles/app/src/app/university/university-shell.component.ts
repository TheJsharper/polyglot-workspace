import { Component, effect, inject } from '@angular/core';
import { Router } from '@angular/router';
import { NativeStackOutlet } from '@ng-native/router';
import { UniversitySession } from '@project-00/services-session';

/** Hosts the University screens as their own native stack and leaves them when the session ends. */
@Component({
  selector: 'app-university-shell',
  imports: [NativeStackOutlet],
  template: '<native-stack-outlet />',
  host: { style: 'flex: 1' },
})
export class UniversityShellComponent {
  constructor() {
    const session = inject(UniversitySession);
    const router = inject(Router);
    effect(() => {
      if (!session.authenticated()) {
        void router.navigate(['/login'], { replaceUrl: true });
      }
    });
  }
}