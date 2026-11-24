# Write File Test Cases

## What is File Writing?

File writing saves data from your program to disk. Rust provides safe, atomic writes with automatic cleanup on failure.

```rust
use std::fs;

fs::write("output.txt", "Hello, world!")?;
```

## Test Cases Explained

### `test_write_file`
Tests writing content to a new file.
- Overwrites if file exists
- Creates file if it doesn't exist
- Returns `Result<()>` — `Ok(())` or `Err(error)`

### `test_write_file_lines`
Tests writing a vector of strings as lines.
- Joins lines with `\n`
- Adds trailing newline
- Useful for generating reports, logs

### `test_append_to_file`
Tests adding content to an existing file.
- Uses `OpenOptions` with `.append(true)`
- Doesn't overwrite existing content
- Creates file if it doesn't exist
- Each append adds a newline

### `test_copy_file`
Tests copying a file to a new location.
- Uses `fs::copy` (fast, OS-level copy)
- Preserves content exactly
- Returns bytes copied on success

### `test_delete_file`
Tests removing a file from disk.
- Returns `Err` if file doesn't exist
- Permanent — no recycle bin!
- Always check `file_exists()` first if unsure

### `test_file_exists`
Tests checking if a file exists.
- Returns `bool` — no errors possible
- Safe to call on any path
- Useful before read/write/delete operations

## Key Operations

| Operation | Function | Returns | Notes |
|-----------|----------|---------|-------|
| Write file | `write_file(path, content)` | `Result<()>` | Overwrites existing |
| Write lines | `write_file_lines(path, lines)` | `Result<()>` | Joins with newlines |
| Append | `append_to_file(path, content)` | `Result<()>` | Adds to end |
| Copy | `copy_file(src, dest)` | `Result<()>` | Fast OS-level copy |
| Delete | `delete_file(path)` | `Result<()>` | Permanent removal |
| Exists | `file_exists(path)` | `bool` | Safe check |

## Write vs Append

```rust
// Write — replaces entire file
write_file("log.txt", "New content")?;

// Append — adds to end
append_to_file("log.txt", "More content")?;
```

## Error Handling

All write operations return `Result<()>`:

```rust
match write_file("output.txt", "data") {
    Ok(()) => println!("Saved!"),
    Err(e) => eprintln!("Failed: {}", e),
}

// Or with ? in functions returning Result
write_file("output.txt", "data")?;
```

## When to Use Which

- **`write_file`** — saving config, exporting data, overwriting
- **`write_file_lines`** — generating reports, CSV output
- **`append_to_file`** — logging, accumulating results
- **`copy_file`** — backups, temp processing copies
- **`delete_file`** — cleanup, temp file removal

## Safety Tips

1. **Always handle errors** — disk full, permissions, invalid paths
2. **Check before delete** — `file_exists()` prevents errors
3. **Use temp files** — write to temp, then rename for atomicity
4. **Clean up after tests** — delete temp files in test cleanup
