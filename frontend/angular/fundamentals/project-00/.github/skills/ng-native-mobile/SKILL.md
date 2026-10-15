---
name: ng-native-mobile
description: "Trigger: ng-native, Angular Native, Expo mobile app, native UI, mobile documentation. Guide Nx mobile setup and feature work with upstream checks."
license: Apache-2.0
metadata:
  author: "TheJsharper"
  version: "1.0"
---

# ng-native mobile

## Activation Contract
Use for mobile setup, native UI/features, routing, testing, or docs in this Angular workspace.

## Hard Rules
- Read [local mobile guidance](../../../apps/mobiles/README.md) first. Check current upstream ng-native and Nx docs before installing or generating; do not assume version compatibility.
- Check `apps/mobiles/` for existing Nx applications before generating or describing project status; `apps/mobiles/app` is the current native app.
- Do not reuse DOM APIs, Angular Material, web-only NgRx effects, or browser Module Federation in native rendering. Reuse only platform-neutral code after inspecting imports.

## Decision Gates
| Request | Action |
| --- | --- |
| Explain or update mobile guidance | Update local docs with cited official sources; no dependencies or app scaffold. |
| Create a mobile app | Confirm name, platforms and device/build needs; check Nx/Angular/Expo/React compatibility before running the official Nx generator. |
| Implement a native feature | Check current component/native API and platform support; use generated mobile project conventions and native tests. |

## Execution Steps
1. Inspect existing mobile projects and workspace versions; use the official getting-started, Nx and limitations guides linked from the local guide.
2. Plan platform-specific dependencies and boundaries; seek clarification for unspecified app scope or unsupported features.
3. Make only requested changes; test/typecheck the generated app when one exists and verify device behavior when required.

## Output Contract
Report files changed, version/source checks, validation results and any device verification not performed.

## References
- [Mobile area and upstream documentation links](../../../apps/mobiles/README.md)
- [Workspace architecture](../../../docs/architecture.md)
