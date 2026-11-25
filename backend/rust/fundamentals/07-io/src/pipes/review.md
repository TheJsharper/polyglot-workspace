# Pipes Module — `std::io::pipe()`

## What It Demonstrates

Anonymous pipes in Rust (`std::io::pipe()`), introduced in Rust 1.89.0. This module shows:

1. **Cross-platform pipe creation** — `std::io::pipe()` returns `(PipeReader, PipeWriter)` on both Unix and Windows.
2. **Concurrent read/write pattern** — Pipes have a limited OS buffer (~4KB on Windows, ~64KB on Linux). Writing more than the buffer without a concurrent reader **blocks**. The demo uses `std::thread::spawn` for the writer so large messages work.
3. **`From<PipeReader> for OwnedFd` (Unix)** — Converts a `PipeReader` into an `OwnedFd`, giving direct access to the raw file descriptor. The `OwnedFd` can then be converted back into a `File` via `From<OwnedFd> for File`.
4. **`From<PipeReader> for OwnedHandle` (Windows)** — The Windows equivalent: `PipeReader` converts to `OwnedHandle`, then to `File`.

## Key Functions

| Function | Platform | Purpose |
|---|---|---|
| `create_pipe()` | Both | Returns `(PipeReader, PipeWriter)` wrapped in `io::Result` |
| `send_and_receive(msg)` | Both | Write through pipe, read back all bytes |
| `send_lines_and_read(lines)` | Both | Write multiple lines, read with `BufReader::lines()` |
| `pipe_reader_to_owned_fd()` | Unix only | Demonstrates `PipeReader -> OwnedFd` |
| `read_via_owned_fd(msg)` | Unix only | Write -> convert to `OwnedFd` -> read via `File::from(OwnedFd)` |
| `pipe_reader_to_owned_handle()` | Windows only | Demonstrates `PipeReader -> OwnedHandle` |
| `read_via_owned_handle(msg)` | Windows only | Write -> convert to `OwnedHandle` -> read via `File::from(OwnedHandle)` |

## Architecture Decision: Threads for Writing

**Problem:** `write_all()` blocks when the pipe buffer is full and no one is reading.

**Solution:** Writer runs in a separate thread. Reader consumes bytes on the main thread. This is the standard pattern for pipe communication in Rust — you cannot write arbitrarily large data synchronously without a concurrent reader.

```
┌─────────────┐         pipe buffer          ┌─────────────┐
│   Writer    │  ──────────────────────►      │   Reader    │
│  (thread)   │  (~4KB Windows / ~64KB Linux)  │  (main)     │
└─────────────┘                                └─────────────┘
```

## Platform Differences

| Feature | Unix | Windows |
|---|---|---|
| Pipe type | Anonymous pipe (fd pair) | Anonymous pipe (handle pair) |
| Ownership type | `OwnedFd` | `OwnedHandle` |
| Trait impl | `From<PipeReader> for OwnedFd` | `From<PipeReader> for OwnedHandle` |
| Back to File | `File::from(OwnedFd)` | `File::from(OwnedHandle)` |

## Tests (11 total)

- `test_create_pipe` — pipe creation works
- `test_send_and_receive_simple` — basic string round-trip
- `test_send_and_receive_empty` — empty message
- `test_send_and_receive_multibyte` — UTF-8 with emoji
- `test_send_and_receive_large` — 10KB message (proves thread pattern works)
- `test_send_lines_and_read` — multi-line with `BufReader`
- `test_send_lines_and_read_empty` — no lines
- `test_send_lines_and_read_single` — one line
- `test_send_lines_preserves_order` — ordering guarantee
- `test_pipe_reader_to_owned_handle` — Windows `OwnedHandle` conversion
- `test_read_via_owned_handle` — read through converted `File::from(OwnedHandle)`

Unix equivalents run only on Unix via `#[cfg(unix)]`.
