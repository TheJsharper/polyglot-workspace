# BufReader Test Cases

## What is BufReader?

`BufReader` wraps any reader (like `File`) with an **internal buffer** to reduce system calls. Instead of reading byte-by-byte from disk, it reads in chunks and serves from memory.

```rust
use std::fs::File;
use std::io::BufReader;

let file = File::open("log.txt")?;
let reader = BufReader::new(file);
```

## Test Cases Explained

### `test_read_first_line`
Tests reading a single line using `read_line()`.
- `BufReader` provides `.read_line()` via `BufRead` trait
- Returns the line including the newline character
- Much faster than reading byte-by-byte

### `test_count_lines`
Tests counting lines using `.lines()` iterator.
- `.lines()` returns an iterator over `Result<String>`
- Each iteration reads one line lazily
- Efficient for large files — doesn't load everything into memory

### `test_bufreader_capacity`
Tests checking buffer capacity and filled length.
- `.capacity()` returns total buffer size (default 8KB)
- `.fill_buf()` returns the currently buffered data
- Buffer length ≤ capacity always

### `test_get_immutable_ref`
Tests getting a read-only reference to the underlying file.
- `.get_ref()` returns `&File`
- Useful for calling `File` methods like `.metadata()`
- Doesn't consume the `BufReader`

### `test_get_underlying_file`
Tests getting a mutable reference to the underlying file.
- `.get_mut()` returns `&mut File`
- Allows modifying the file while keeping the buffer
- Doesn't consume the `BufReader`

### `test_into_inner`
Tests extracting the original file from `BufReader`.
- `.into_inner()` consumes the `BufReader`
- Returns ownership of the `File`
- Can't use `BufReader` after this call

### `test_peek_demo`
Tests peeking at buffered data without consuming it.
- Uses `fill_buf()` (stable) instead of `peek()` (nightly-only)
- Shows data in buffer without advancing the read position
- `.read()` consumes bytes, `.fill_buf()` doesn't

## BufReader Methods Summary

| Method | Returns | Consumes? | Use when |
|--------|---------|-----------|----------|
| `read_line()` | `Result<usize>` |  No | Read one line |
| `lines()` | `Lines` iterator |  No | Process file line-by-line |
| `fill_buf()` | `Result<&[u8]>` |  No | Peek at buffered data |
| `get_ref()` | `&File` |  No | Read-only file access |
| `get_mut()` | `&mut File` |  No | Mutable file access |
| `into_inner()` | `File` | ✅ Yes | Done buffering |
| `capacity()` | `usize` |  No | Check buffer size |

## When to Use BufReader

- Reading line-by-line (logs, CSVs, configs)
- Many small reads from the same file
- Parsing text files
- Any `Read` type that benefits from buffering (`TcpStream`, etc.)

## When NOT to Use BufReader

- Reading entire file at once — use `fs::read_to_string()`
- Already reading large chunks — buffering adds overhead
- In-memory data (`Vec<u8>`, `&[u8]`) — already fast

## Buffer Size

Default buffer size is **8,192 bytes** (8KB). Customize with `with_capacity()`:

```rust
let reader = BufReader::with_capacity(32_768, file);  // 32KB buffer
```

Larger buffers = fewer system calls, but more memory usage.
