import { createActionGroup, createFeature, createReducer, createSelector, on, props } from '@ngrx/store';
import type { MeResponse, Role } from '@project-00/services-api';

/** Token-free view of the signed-in user. The bearer token stays inside `UniversitySession`. */
export interface AuthUser {
  username: string;
  role: Role;
  displayName: string;
  pictureUrl: string | null;
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
  if (!user?.username || !user.role) {
    return null;
  }
  return {
    username: user.username,
    role: user.role,
    displayName: user.displayName?.trim() || user.username,
    pictureUrl: user.pictureUrl || null,
  };
}

function sameUser(a: AuthUser | null, b: AuthUser | null): boolean {
  return a?.username === b?.username && a?.role === b?.role
    && a?.displayName === b?.displayName && a?.pictureUrl === b?.pictureUrl;
}

const initialState: AuthState = { user: null };

export const authFeature = createFeature({
  name: 'auth',
  reducer: createReducer(
    initialState,
    on(AuthActions.userChanged, (state, { user }): AuthState => sameUser(state.user, user) ? state : { user }),
  ),
  extraSelectors: ({ selectUser }) => {
    const selectRole = createSelector(selectUser, (user) => user?.role ?? null);
    return {
      selectUsername: createSelector(selectUser, (user) => user?.username ?? ''),
      selectDisplayName: createSelector(selectUser, (user) => user?.displayName || user?.username || ''),
      selectPictureUrl: createSelector(selectUser, (user) => user?.pictureUrl ?? null),
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
  selectDisplayName,
  selectPictureUrl,
  selectRole,
  selectAuthenticated,
  selectIsAdmin,
  selectIsProfessor,
  selectIsStudent,
} = authFeature;
