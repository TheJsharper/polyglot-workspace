# BufWriter Test Cases

## What is BufWriter?

`BufWriter` wraps any writer (like `File`) with an **internal buffer** to reduce system calls. Instead of writing byte-by-byte to disk (each write = one syscall), it collects small writes in memory and flushes them in large batches.

```rust
use std::fs::File;
use std::io::BufWriter;

let file = File::create("output.txt")?;
let mut writer = BufWriter::new(file);
writer.write_all(b"hello")?;
writer.flush()?; // data actually hits disk here
```

**When to use:** many small, repeated writes to the same destination.
**When NOT to use:** one big write, or writing to memory (`Vec<u8>`) — buffering adds overhead with no benefit.

## Buffer Behavior

Data written to `BufWriter` **stays in memory** until:
1. You call `.flush()`
2. The buffer fills up (auto-flush)
3. You call `.into_inner()` (auto-flush)
4. The `BufWriter` is dropped (attempts flush, but **silently ignores errors**)

```
┌──────────────────────────────────────────┐
│            BufWriter<File>               │
│  ┌────────────────────────────────────┐  │
│  │      Internal Buffer (8KB)         │  │
│  │  [byte][byte]...  ← data sits here │  │
│  └────────────┬───────────────────────┘  │
│               │                          │
│         .flush() sends to disk           │
└───────┬───────┼──────────────────────────┘
        │     ▼
        │  File (OS syscall)
        │
   write! / write_all() → goes to buffer, NOT disk
```

## Test Cases Explained

### `test_write_with_bufwriter`
Tests basic write + flush round-trip.
- `write!` macro formats into the buffer
- `.flush()` pushes buffered data to disk
- Read back to verify content matches

### `test_write_lines_with_bufwriter`
Tests writing multiple lines with `writeln!`.
- Each `writeln!` adds content + `\n` to the buffer
- Single `.flush()` at the end writes everything at once

### `test_write_with_bufwriter_overwrites`
Tests that `File::create` truncates existing files.
- First write creates file with content
- Second write truncates and replaces — no append

### `test_file_exists`
Tests that writing creates the file on disk.
- File does not exist before write
- File exists after write + flush

### `test_write_with_custom_capacity_small`
Tests a 16-byte buffer.
- Small buffer = data fits, flushes normally
- Returns byte count from `.write()`

### `test_write_with_custom_capacity_large`
Tests an 8KB buffer with 5000 bytes of data.
- Data fits in buffer entirely
- Flush writes all 5000 bytes in one syscall

### `test_capacity_returns_buffer_size`
Tests `.capacity()` returns the correct buffer size.
- `with_capacity(256)` → capacity = 256
- `new(file)` → capacity = 8192 (default)

### `test_get_underlying_writer`
Tests `.get_ref()` for immutable access to the inner `File`.
- Returns `&File` without consuming `BufWriter`
- You can call `File` methods like `.metadata()` on the reference

### `test_get_mutable_underlying_writer`
Tests `.get_mut()` for mutable access to the inner `File`.
- Returns `&mut File` — you can seek, truncate, etc.
- **Warning:** mutating the inner file does NOT update the buffer

### `test_into_inner_demo`
Tests `.into_inner()` to unwrap the `BufWriter`.
- Returns `Result<File, (BufWriter, Error)>`
- Consumes the `BufWriter` — cannot use it after
- Auto-flushes buffer before returning the `File`

### `test_into_inner_flushes_buffer`
Tests that `into_inner()` automatically flushes.
- Write data (stays in buffer)
- Call `into_inner()` → data hits disk automatically
- Read back to verify all data made it

### `test_write_all_bufwriter`
Tests `.write_all()` which guarantees all bytes are written.
- Unlike `.write()` (may write fewer bytes than requested), `.write_all()` loops until done or errors
- The buffer still holds data until `.flush()` is called

### `test_write_all_guarantees_full_write`
Tests `.write_all()` with 10KB of data.
- Proves the full content round-trips correctly
- No partial writes, no truncation

### `test_demonstrate_buffer_behavior`
Tests the key insight: data is NOT on disk until flush.
- Writes data → buffer
- Reads file on disk → **empty** (nothing flushed yet)
- Flushes → reads file again → **data present**

### `test_data_not_on_disk_until_flush`
Direct test of buffer isolation.
- Creates `BufWriter` with 1KB buffer
- Writes "not flushed yet" → sits in buffer
- Reads file on disk → **still empty**
- Flushes → reads file → **content appears**

### `test_demonstrate_drop_without_flush`
Tests what happens when `BufWriter` is dropped without flush.
- Writes data, drops without flush
- **Drop attempts flush but silently ignores errors**
- On many platforms data still makes it, but you can't rely on it
- Key lesson: **always call `.flush()` explicitly**

### `test_write_many_small_writes`
Tests BufWriter's sweet spot: many small writes batched together.
- Fragments: `"hello "`, `"world "`, `"from "`, `"bufwriter"`
- Each `.write_all()` adds to buffer
- Single `.flush()` writes everything in one syscall

### `test_write_many_small_writes_empty`
Tests empty fragment list — produces empty file.

### `test_write_many_small_writes_single_fragment`
Tests single fragment — simplest case of many-small-writes.

## BufWriter Methods Summary

| Method | Returns | Flushes? | Use when |
|---|---|---|---|
| `new(writer)` | `BufWriter` | No | Default 8KB buffer |
| `with_capacity(n, writer)` | `BufWriter` | No | Custom buffer size |
| `write(&buf)` | `Result<usize>` | No | May write fewer bytes than requested |
| `write_all(&buf)` | `Result<()>` | No | Guarantees all bytes written |
| `flush()` | `Result<()>` | **Yes** | Push buffer to underlying writer |
| `get_ref()` | `&W` | No | Read-only access to inner writer |
| `get_mut()` | `&mut W` | No | Mutable access to inner writer |
| `into_inner()` | `Result<W>` | **Yes** (auto) | Consume BufWriter, get inner writer back |
| `capacity()` | `usize` | No | Check buffer size |

## Critical Rule: Always Flush

**Dropping a BufWriter flushes, but silently discards errors.** If the disk is full or the file is locked, your data is lost with no error reported.

```rust
// ❌ DANGEROUS — errors silently ignored
{
    let mut w = BufWriter::new(file);
    w.write_all(data)?;
    // dropped here — if flush fails, data is lost
}

// ✅ SAFE — explicit flush catches errors
let mut w = BufWriter::new(file);
w.write_all(data)?;
w.flush()?;  // Error returned here if write fails
```

## Default Buffer Size

**8,192 bytes** (8KB). This is the default for `BufWriter::new()`. Customize with `with_capacity()`:

```rust
let writer = BufWriter::with_capacity(32_768, file);  // 32KB buffer
```

Larger buffers = fewer syscalls for many small writes, but more memory usage.
