import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import { provideEffects } from '@ngrx/effects';
import { provideState } from '@ngrx/store';
import * as universityEffects from './university.effects';
import { universityFeature } from './university.feature';

/**
 * Registers the `university` feature and its effects. Safe to call from every remote's route
 * providers: the feature reducer is keyed by name and NgRx de-duplicates the same effects source.
 */
export function provideUniversityState(): EnvironmentProviders {
  return makeEnvironmentProviders([provideState(universityFeature), provideEffects(universityEffects)]);
}
