# BufReaders Module

## What Is `BufReader`?

`BufReader<R>` adds a buffer on top of any `Read` type. Without buffering, each `.read()` call hits the underlying source directly — a syscall for files, a network round-trip for sockets. `BufReader` reads a chunk (default 8KB) into memory, so subsequent reads hit RAM instead of the source.

```
┌──────────────────────────────────────────┐
│            BufReader<File>               │
│  ┌────────────────────────────────────┐  │
│  │      Internal Buffer (8KB)         │  │
│  │  [byte][byte][byte]...[byte]       │  │
│  └────────────────────────────────────┘  │
│                  ▲                       │
│          fills from File                 │
└──────────────────┼───────────────────────┘
                   │
            ┌──────▼──────┐
            │   File (OS)  │
            │   syscall    │
            └─────────────┘
```

**When to use it:** reading many small pieces (line-by-line, byte-by-byte). For bulk reads (`.read_to_end` on a whole file), a bare `File` is fine — `BufReader` just adds a copy.

---

## Functions and Examples

### 1. `read_first_line(file_path)` — Read only the first line

```rust
// File: greeting.txt
// Hello, Rust!
// Second line
// Third line

let line = read_first_line("greeting.txt")?;
assert_eq!(line, "Hello, Rust!\n");
```

**How it works:** `BufReader` + `.read_line()` stops at the first `\n`. Only the first chunk of the file is buffered; the rest is never read.

---

### 2. `count_lines(file_path)` — Count total lines

```rust
let n = count_lines("greeting.txt")?;
assert_eq!(n, 3);
```

**How it works:** `.lines()` returns an iterator over `Result<String>`. `.count()` consumes all lines. Each line is read from the buffer, not directly from disk.

---

### 3. `get_bufreader_capacity(file_path)` — Inspect buffer state

```rust
let file = File::open("greeting.txt")?;
let reader = BufReader::new(file);

let capacity = reader.capacity();       // 8192 (default 8KB)
let buffer = reader.fill_buf()?;        // peek at buffered bytes
let buffered_len = buffer.len();        // how many bytes are currently in the buffer
```

**`fill_buf()`** is the key method: it returns a `&[u8]` slice of whatever is currently buffered, **without consuming it**. After you process the bytes, call `.consume(n)` to tell the reader how many bytes you used.

```rust
let mut reader = BufReader::new(file);
let data = reader.fill_buf()?;   // e.g. b"Hello, world!\nSecond..."
println!("{:?}", &data[..5]);    // b"Hello"
reader.consume(5);               // now those 5 bytes are "consumed"
```

---

### 4. `get_underlying_file(file_path)` — `.get_mut()` for mutable access

```rust
let file = File::open("data.txt")?;
let mut reader = BufReader::new(file);

let inner_file: &mut File = reader.get_mut();
// You can mutate the underlying File (e.g., seek it)
// But the BufReader's buffer is NOT flushed
```

**Use case:** you need to do something on the underlying `Read` type that `BufReader` doesn't expose. But be careful — the buffer may contain stale data after you mutate the underlying source.

---

### 5. `get_immutable_ref(file_path)` — `.get_ref()` for read-only access

```rust
let file = File::open("data.txt")?;
let reader = BufReader::new(file);

let inner_file: &File = reader.get_ref();
// Read-only access to the underlying File
```

**Use case:** inspect the underlying type without consuming or mutating it.

---

### 6. `into_inner_demo(file_path)` — `.into_inner()` to unwrap

```rust
let file = File::open("data.txt")?;
let reader = BufReader::new(file);

let unwrapped_file: File = reader.into_inner();
// BufReader is dropped, File is returned
// Unread buffered bytes are LOST
```

**Warning:** any bytes sitting in the buffer that haven't been consumed are discarded. If you need them, call `.fill_buf()` first.

---

### 7. `peek_demo()` — Buffer peeking on a byte slice

This demo uses a `&[u8]` slice instead of a file, showing that `BufReader` works with **any** `Read` type:

```rust
let mut data = &b"oh, hello there"[..];
let mut reader = BufReader::with_capacity(6, &mut data);

// Peek: see first bytes WITHOUT consuming
let peeked = reader.fill_buf()?;
assert_eq!(&peeked[..2], b"oh");

// Now actually read 4 bytes
let mut buf = [0; 4];
reader.read(&mut buf)?;
assert_eq!(&buf, b"oh, ");

// The rest
let mut rest = String::new();
reader.read_to_string(&mut rest)?;
assert_eq!(rest, "hello there");
```

**Key insight:** `fill_buf()` is **peek** — it shows buffered bytes without advancing. `read()` and `consume()` advance the position.

---

### 8. `read_lines_from_bufreader(file_path)` — Collect all lines into `Vec<String>`

```rust
let lines = read_lines_from_bufreader("poem.txt")?;
// lines: Vec<String> = ["Line 1", "Line 2", "Line 3"]
```

**How it works:** iterates `.lines()` and collects. Each `Result<String>` is unwrapped with `?`.

---

### 9. `read_lines_with_custom_capacity(file_path, capacity)` — Custom buffer size

```rust
// Small buffer: more syscalls, less memory
let lines = read_lines_with_custom_capacity("small.txt", 64)?;

// Large buffer: fewer syscalls, more memory
let lines = read_lines_with_custom_capacity("huge.txt", 65536)?;
```

**Tradeoff:** smaller buffer = more frequent OS reads. Larger buffer = one big read upfront.

---

### 10. `seek_in_file(file_path, position)` — Seek within a file

```rust
seek_in_file("data.bin", 100)?;  // jump to byte 100
```

**Note:** `File` implements `Seek`. After seeking, the next read starts from that position.

---

### 11. `read_lines_from_bufreader_to_string_buf(file_path)` — Read entire file into a `String`

```rust
let content = read_lines_from_bufreader_to_string_buf("story.txt")?;
// content: "Once upon a time...\nThe end.\n"
```

**How it works:** `BufReader` + `.read_to_string()`. For whole-file reads, a bare `std::fs::read_to_string()` is simpler and does the same thing internally.

---

### 12. `read_lines_from_std_in_to_string_buf()` — Read from stdin ⚠️

```rust
// Usage: echo "hello" | cargo run
let input = read_lines_from_std_in_to_string_buf()?;
```

**Testing limitation:** stdin is **read-only**. `StdinLock` does NOT implement `Write`, so you cannot simulate stdin input from within a test. The tests for this function are disabled with `#[cfg(any())]`.

**Why the tests fail:**
```rust
// ❌ This does NOT compile — StdinLock has no write_all()
let mut stdin = std::io::stdin();
let mut handle = stdin.lock();
handle.write_all(b"hello\n").unwrap();  // error[E0599]: no method named `write_all`
```

**How to test stdin-reading code properly:**
1. **Refactor to accept `impl Read`** — pass a `&[u8]` slice in tests, `stdin` in production.
2. **Integration test** — pipe data through the shell: `echo "hello" | cargo test`.
3. **Temporary file** — write to a file, redirect stdin in the test process.

**Recommended refactor:**
```rust
// Production code:
pub fn read_all<R: Read>(mut reader: R) -> io::Result<String> {
    let mut content = String::new();
    reader.read_to_string(&mut content)?;
    Ok(content)
}

// In main:
let input = read_all(std::io::stdin())?;

// In test:
let result = read_all(&b"hello\n"[..])?;
assert_eq!(result, "hello\n");
```

---

### 13. `read_lines_from_std_in_to_string_buf_with_iterator()` — Read stdin line by line ⚠️

Same stdin limitation as above. Uses `.lines()` iterator instead of `.read_to_string()`.

---

### 14. `read_write_with_pipe(input_file, output_file)` — Copy file through BufReader + BufWriter

```rust
read_write_with_pipe("source.txt", "dest.txt")?;
```

**How it works:** reads from input with `BufReader`, writes to output with `BufWriter`. This is the classic buffered copy pattern — read chunks into memory, write chunks from memory.

---

## Summary: BufReader Method Cheat Sheet

| Method | What it does | Consumes bytes? |
|---|---|---|
| `.read_line(&mut s)` | Read until `\n` | Yes |
| `.lines()` | Iterator over lines | Yes |
| `.fill_buf()` | Peek at buffered bytes | **No** |
| `.consume(n)` | Mark n bytes as consumed | Yes |
| `.read(&mut buf)` | Fill a buffer | Yes |
| `.read_to_string(&mut s)` | Read all into String | Yes |
| `.read_to_end(&mut v)` | Read all into Vec<u8> | Yes |
| `.capacity()` | Buffer size in bytes | No |
| `.get_ref()` | Immutable ref to inner `R` | No |
| `.get_mut()` | Mutable ref to inner `R` | No |
| `.into_inner()` | Unwrap, return inner `R` | BufReader dropped |
