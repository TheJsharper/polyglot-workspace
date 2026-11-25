# BufWriter Test Cases

## What is BufWriter?

`BufWriter` wraps any writer (like `File`) with an **internal buffer** to reduce system calls. Instead of writing every byte directly to disk, it collects data in memory and flushes it in larger chunks.

```rust
use std::fs::File;
use std::io::BufWriter;

let file = File::create("output.txt")?;
let mut writer = BufWriter::new(file);
```

## Test Cases Explained

### `test_write_with_bufwriter`
Tests writing content to a file using `BufWriter`.
- Content is buffered in memory before flushing to disk
- `flush()` forces all buffered data to the underlying file
- More efficient than writing byte-by-byte for many small writes

### `test_write_lines_with_bufwriter`
Tests writing multiple lines with a single `BufWriter`.
- Each line is written with `writeln!` (adds a newline)
- All lines share the same buffer
- Single flush at the end writes everything at once

### `test_write_with_bufwriter_overwrites`
Tests that `BufWriter` with `File::create` replaces existing content.
- `File::create` truncates the file to zero length
- Second write completely replaces the first write's content
- Useful when updating files with new data

### `test_file_exists`
Tests checking if a file exists before or after writing.
- Uses `std::path::Path::exists()` for safe boolean check
- Returns `false` for non-existent paths, `true` for existing files
- No error handling needed — just a true/false result

## BufWriter Methods Summary

| Method | Returns | Use when |
|--------|---------|----------|
| `new(writer)` | `BufWriter` | Wrap any type implementing `Write` |
| `with_capacity(size, writer)` | `BufWriter` | Customize buffer size (default 8KB) |
| `flush()` | `Result<()>` | Force buffered data to disk |
| `get_ref()` | `&Writer` | Read-only access to underlying writer |
| `get_mut()` | `&mut Writer` | Mutable access to underlying writer |
| `into_inner()` | `Result<Writer>` | Extract underlying writer, auto-flushing |

## When to Use BufWriter

- Writing many small pieces of data to the same file
- Building a file incrementally (logs, reports, CSV rows)
- Network writing (`TcpStream`) — reduces packet count
- Any `Write` type that benefits from batching

## When NOT to Use BufWriter

- Writing one large chunk at once — `fs::write` is simpler
- Already buffered output — double buffering wastes memory
- Immediate flush required — buffering delays writes

## Buffer Size

Default buffer size is **8,192 bytes** (8KB). Customize with `with_capacity()`:

```rust
let writer = BufWriter::with_capacity(32_768, file);  // 32KB buffer
```

Larger buffers = fewer system calls, but more memory usage. Always call `flush()` when done, or use `into_inner()` which flushes automatically.

## BufWriter vs Direct Write

```rust
// Direct write — one system call per write
let mut file = File::create("log.txt")?;
for entry in log_entries {
    writeln!(file, "{}", entry)?;  // Each call hits disk
}

// BufWriter — batches into fewer system calls
let file = File::create("log.txt")?;
let mut writer = BufWriter::new(file);
for entry in log_entries {
    writeln!(writer, "{}", entry)?;  // Buffered in memory
}
writer.flush()?;  // One disk write for all entries
```
