# Pipe Test Cases

## What Are Anonymous Pipes?

Anonymous pipes (`std::io::pipe()`) create a connected reader/writer pair for in-process communication. Data written to the `PipeWriter` can be read from the `PipeReader`. They work on both Unix and Windows.

```rust
let (reader, writer) = std::io::pipe()?;
```

## Test Cases Explained

### `test_create_pipe`
Tests that pipe creation works on the current platform.
- `std::io::pipe()` returns `Result<(PipeReader, PipeWriter)>`
- Returns an error if the OS cannot create the pipe
- Cross-platform: works on Unix and Windows

### `test_send_and_receive_simple`
Tests sending a basic string through the pipe.
- Writer sends `"hello pipe"` in a background thread
- Reader consumes all bytes with `.read_to_end()`
- Verifies the round-trip content matches exactly

### `test_send_and_receive_empty`
Tests sending an empty message.
- Zero bytes written, zero bytes read
- Ensures the pipe handles empty data without hanging

### `test_send_and_receive_multibyte`
Tests UTF-8 content including emoji.
- `"hola mundo 🦀"` contains multi-byte characters
- Pipes transmit raw bytes — encoding is the caller's responsibility
- `String::from_utf8` validates the round-trip

### `test_send_and_receive_large`
Tests sending 10,000 bytes through the pipe.
- **Critical test**: pipe buffers are limited (~4KB on Windows, ~64KB on Linux)
- Without a concurrent reader thread, `write_all()` would **block forever**
- The implementation uses `std::thread::spawn` for the writer so the reader can drain the buffer simultaneously

### `test_send_lines_and_read`
Tests sending multiple lines and reading with `BufReader::lines()`.
- Writer sends `"line one\n"`, `"line two\n"`, `"line three\n"`
- Reader wraps `PipeReader` in `BufReader` and iterates `.lines()`
- Verifies line ordering is preserved

### `test_send_lines_and_read_empty`
Tests reading from a pipe with no lines written.
- Writer thread closes immediately without writing
- Reader gets an empty iterator
- Result: empty `Vec<String>`

### `test_send_lines_and_read_single`
Tests reading a single line.
- Verifies the line iterator works for the minimal non-empty case

### `test_send_lines_preserves_order`
Tests that line ordering is guaranteed.
- Pipes are FIFO: first byte written = first byte read
- Verifies `["alpha", "beta", "gamma", "delta"]` arrives in order

### `test_pipe_reader_to_owned_handle` (Windows only)
Tests converting `PipeReader` into `OwnedHandle`.
- Demonstrates `impl From<PipeReader> for OwnedHandle`
- `OwnedHandle` gives raw Windows handle access
- Handle is non-null and valid after conversion

### `test_read_via_owned_handle` (Windows only)
Tests reading through a `File` created from `OwnedHandle`.
- Pipeline: write to pipe → convert reader to `OwnedHandle` → `File::from(handle)` → read
- Proves the handle is fully usable after conversion
- Content round-trips correctly

## Why Threads Are Required

Pipes have a **fixed OS buffer**. When the buffer fills, `write_all()` blocks until a reader drains it.

```
Writer thread ──► [pipe buffer ~4KB] ──► Reader thread
     │                                        │
     │   blocks here if buffer full           │
     └────────────────────────────────────────┘
```

**Without threads:**
```rust
// ❌ DEADLOCK for large messages
writer.write_all(&large_data)?;  // blocks — buffer full, no one reading
reader.read_to_end(&mut buf)?;   // never reached
```

**With threads:**
```rust
// ✅ Works — writer and reader run concurrently
let handle = std::thread::spawn(move || writer.write_all(&data));
reader.read_to_end(&mut buf)?;   // drains buffer as writer fills it
handle.join().unwrap()?;
```

## Platform-Specific Tests

| Test | Unix | Windows |
|---|---|---|
| `test_pipe_reader_to_owned_handle` | Skipped | Runs |
| `test_read_via_owned_handle` | Skipped | Runs |
| `test_pipe_reader_to_owned_fd` | Runs | Skipped |
| `test_read_via_owned_fd` | Runs | Skipped |

Tests use `#[cfg(unix)]` and `#[cfg(windows)]` attributes to compile only on the matching platform.

## Key Takeaways

- **Pipes are byte streams** — no message boundaries. What you write is what you read.
- **Buffer size matters** — always use concurrent read/write for data larger than the pipe buffer.
- **Cross-platform API** — `std::io::pipe()` works everywhere, but ownership types differ (`OwnedFd` vs `OwnedHandle`).
- **FIFO guarantee** — bytes arrive in the exact order they were written.
