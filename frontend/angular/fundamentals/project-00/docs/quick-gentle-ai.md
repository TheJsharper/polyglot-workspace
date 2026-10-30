# Quick introduction to Gentle AI

Gentle AI adds a managed engineering workflow, instructions, skills, and optional integrations to AI coding agents you already have. It can configure VS Code GitHub Copilot; it does **not** install Copilot, provide a model subscription, or replace its sign-in.

## Configure GitHub Copilot

On Windows, the standalone Gentle AI install currently requires Go 1.25.10 or later. Open PowerShell and install the stable release using the [official instructions](https://github.com/Gentleman-Programming/gentle-ai#-get-started), then verify the command:

```powershell
go install github.com/gentleman-programming/gentle-ai/v4/cmd/gentle-ai@latest
gentle-ai version
```

If PowerShell cannot find `gentle-ai`, add Go's install directory (`go env GOBIN`, or `go env GOPATH` plus `\bin`) to your user PATH and restart the terminal.

Preview the configuration before writing anything:

```powershell
gentle-ai install --dry-run --agent vscode-copilot
```

If the plan looks right, run the install and select the components you want:

```powershell
gentle-ai install --agent vscode-copilot
gentle-ai doctor
```

Gentle AI backs up managed configuration before writes. Read the plan and completion summary; select only the agents and optional components you want Gentle AI to manage.

## Use it

After setup, keep using GitHub Copilot normally in VS Code. Gentle AI adds its selected managed instructions, skills, and supported MCP configuration to Copilot; it does not share the conversation with Gentle Shell or change your Copilot account.

After upgrading Gentle AI, preview and refresh the managed files for the agents you selected:

```powershell
gentle-ai sync --dry-run
gentle-ai sync
```

`gentle-ai doctor` is a read-only health check. `gentle-ai sync` refreshes managed configuration; it does not install or update the agent application itself.

## Gentle AI and Gentle Shell

| Tool | Role |
|---|---|
| Gentle AI | Configures supported agents, including VS Code Copilot. |
| Gentle Shell | A terminal-based coding-agent experience built around Pi; it includes its own pinned Gentle AI runtime. |

Use [the Gentle Shell quick introduction](quick-gentle-shell.md) to start and work with Gentle Shell. See the [Gentle AI README](https://github.com/Gentleman-Programming/gentle-ai) and [supported agents](https://github.com/Gentleman-Programming/gentle-ai/blob/main/docs/agents.md) for current platform details.
