import { createActionGroup, emptyProps, props } from '@ngrx/store';
import type { NotesResponse } from '@project-00/services-api';
import type { NoteLookupMode, Resource, SelectionPayload, UniversityCollections, UniversityOperation } from './university.models';

export const UniversityActions = createActionGroup({
  source: 'University',
  events: {
    /** A resource screen was opened: reset its transient view state and load it. */
    Open: props<{ resource: Resource }>(),
    Load: props<{ resource: Resource }>(),
    'Load Success': props<{ resource: Resource; data: Partial<UniversityCollections>; choiceError: string }>(),
    'Load Failure': props<{ resource: Resource; error: string }>(),
    'Set Filter': props<{ resource: Resource; query: string }>(),
    'Begin Create': props<{ resource: Resource }>(),
    'Cancel Create': props<{ resource: Resource }>(),
    Select: props<SelectionPayload>(),
    'Detail Loaded': props<SelectionPayload>(),
    'Detail Failure': props<{ resource: Resource; key: string; error: string }>(),
    Execute: props<{ resource: Resource; operation: UniversityOperation; success: string; closeCreate?: boolean }>(),
    'Execute Success': props<{ resource: Resource; success: string; closeCreate: boolean }>(),
    'Execute Failure': props<{ resource: Resource; error: string }>(),
    'Set Note Lookup Mode': props<{ mode: NoteLookupMode }>(),
    'Find Notes': props<{ mode: NoteLookupMode; studentId: string; lectureId: number | null }>(),
    'Find Notes Success': props<{ notes: NotesResponse[] }>(),
    'Find Notes Failure': props<{ error: string }>(),
    Reset: emptyProps(),
  },
});
