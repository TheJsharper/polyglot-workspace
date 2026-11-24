# Read File Test Cases

## What is File Reading?

File reading loads content from disk into your program. Rust provides safe, error-handled I/O through `std::fs` and `std::io`.

```rust
use std::fs;

let content = fs::read_to_string("file.txt")?;
```

## Test Cases Explained

### `test_read_file`
Tests reading an entire file as a string.
- Returns `Result<String>` — `Ok(content)` or `Err(error)`
- Handles newlines automatically
- Fails if file doesn't exist

### `test_read_file_lines`
Tests reading a file as a vector of lines.
- Splits on `\n` (newline)
- Each line becomes a `String` in a `Vec`
- Useful for processing line-by-line

### `test_count_words_in_file`
Tests counting words in a file.
- Uses `.split_whitespace()` to split on spaces/tabs/newlines
- Returns total word count
- Demonstrates combining read + process

### `test_read_multiline_file`
Tests reading a file with multiple lines.
- Verifies line count matches
- Checks each line's content
- Shows that trailing newlines are preserved

### `test_count_words_multiline`
Tests word count across multiple lines.
- Counts words across all lines
- 14 words in the multiline test file

### `test_read_nonexistent_file`
Tests error handling for missing files.
- Returns `Err` instead of panicking
- `Result` type forces error handling
- Safe I/O — no unexpected crashes

## Key Operations

| Operation | Function | Returns | Notes |
|-----------|----------|---------|-------|
| Read entire file | `read_file(path)` | `Result<String>` | Simple, loads all at once |
| Read as lines | `read_file_lines(path)` | `Result<Vec<String>>` | Split by newline |
| Count words | `count_words_in_file(path)` | `Result<usize>` | Splits on whitespace |
| Check exists | `file_exists(path)` | `bool` | No error, just true/false |

## Error Handling

Rust I/O uses `Result<T, std::io::Error>`:

```rust
match read_file("file.txt") {
    Ok(content) => println!("Got: {}", content),
    Err(e) => eprintln!("Failed: {}", e),
}

// Or with unwrap (panics on error)
let content = read_file("file.txt").unwrap();

// Or with ? operator (in functions returning Result)
let content = read_file("file.txt")?;
```

## When to Use Which

- **`read_file`** — small files, need full content
- **`read_file_lines`** — processing line by line (logs, CSVs)
- **`count_words_in_file`** — text analysis, statistics
- **Manual iteration** — huge files (use `BufReader` for memory efficiency)
