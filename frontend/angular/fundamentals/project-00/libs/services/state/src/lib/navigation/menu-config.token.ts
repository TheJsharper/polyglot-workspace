import { InjectionToken } from '@angular/core';
import { MenuItem } from '@project-00/services-core';

export const MENU_CONFIG = new InjectionToken<MenuItem[]>('MENU_CONFIG', {
  factory: () => [],
});
