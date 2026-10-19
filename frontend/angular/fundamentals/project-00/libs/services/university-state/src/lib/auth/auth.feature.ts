import { createActionGroup, createFeature, createReducer, createSelector, on, props } from '@ngrx/store';
import type { MeResponse, Role } from '@project-00/services-api';

/** Token-free view of the signed-in user. The bearer token stays inside `UniversitySession`. */
export interface AuthUser {
  username: string;
  role: Role;
}

export interface AuthState {
  user: AuthUser | null;
}

export const AuthActions = createActionGroup({
  source: 'Auth',
  events: {
    'User Changed': props<{ user: AuthUser | null }>(),
  },
});

export function toAuthUser(user: MeResponse | null | undefined): AuthUser | null {
  return user?.username && user.role ? { username: user.username, role: user.role } : null;
}

const initialState: AuthState = { user: null };

export const authFeature = createFeature({
  name: 'auth',
  reducer: createReducer(
    initialState,
    on(AuthActions.userChanged, (state, { user }): AuthState =>
      state.user?.username === user?.username && state.user?.role === user?.role ? state : { user }),
  ),
  extraSelectors: ({ selectUser }) => {
    const selectRole = createSelector(selectUser, (user) => user?.role ?? null);
    return {
      selectUsername: createSelector(selectUser, (user) => user?.username ?? ''),
      selectRole,
      selectAuthenticated: createSelector(selectUser, (user) => !!user),
      selectIsAdmin: createSelector(selectRole, (role) => role === 'ADMIN'),
      selectIsProfessor: createSelector(selectRole, (role) => role === 'PROFESSOR'),
      selectIsStudent: createSelector(selectRole, (role) => role === 'STUDENT'),
    };
  },
});

export const {
  selectUser: selectAuthUser,
  selectUsername,
  selectRole,
  selectAuthenticated,
  selectIsAdmin,
  selectIsProfessor,
  selectIsStudent,
} = authFeature;
