# Quick introduction to Gentle Shell

Gentle Shell is a terminal coding-agent workspace built around Pi. It provides its own agent session, isolated settings, task and agent views, and Git diff browsing. It is separate from GitHub Copilot and does not share conversations or Copilot sign-in.

## Start in this repository

After restarting VS Code so it loads the updated Windows PATH, open this repository in PowerShell and run:

```powershell
gentle-shell
```

The installed launcher starts Pi with Gentle Shell's isolated agent home. Sign in to a supported AI provider from the session when prompted (usually `/login`), then describe the task you want to work on. Provider access is configured separately from GitHub Copilot.

Useful shortcuts:

| Shortcut | Opens |
|---|---|
| `Alt+G` | Git changes and diffs |
| `Alt+A` | Agent activity |
| `Alt+K` | Command palette |

Review permission prompts and inspect the diff before accepting or merging changes.

## Use alongside GitHub Copilot

Do not have both agents edit the same working tree at once: uncommitted changes are visible to both. For independent parallel work, put one agent in a separate Git worktree:

```powershell
# Run from this repository's root; create a sibling checkout from main.
git worktree add ..\polyglot-gentle-shell -b work/gentle-shell main
Set-Location ..\polyglot-gentle-shell
gentle-shell
```

Review and integrate the worktree branch when the task is complete. Remove the worktree only after keeping or discarding its changes deliberately:

```powershell
Set-Location ..\polyglot-workspace\frontend\angular\fundamentals\project-00
git worktree remove ..\polyglot-gentle-shell
```

Replace the example paths if your checkout is elsewhere. For Gentle AI's separate Copilot configuration workflow, see [the Gentle AI quick introduction](quick-gentle-ai.md).

## Maintenance

The bundled Windows installation can update itself with:

```powershell
gentle-shell upgrade
```

See the [Gentle Shell README](https://github.com/Gentleman-Programming/gentle-shell) for current installation details, platform support, and advanced commands.
