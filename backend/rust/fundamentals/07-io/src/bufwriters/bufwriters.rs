use std::fs::File;
use std::io::{BufWriter, Write};

/// Basic write with BufWriter — buffers data, then flushes explicitly.
pub fn write_with_bufwriter(file_path: &str, content: &str) -> std::io::Result<()> {
    let file = File::create(file_path)?;
    let mut buf_writer = BufWriter::new(file);
    write!(buf_writer, "{}", content)?;
    buf_writer.flush()?;
    Ok(())
}

/// Write multiple lines, each followed by a newline.
pub fn write_lines_with_bufwriter(file_path: &str, lines: &[&str]) -> std::io::Result<()> {
    let file = File::create(file_path)?;
    let mut buf_writer = BufWriter::new(file);
    for current_line in lines {
        writeln!(buf_writer, "{}", current_line)?;
    }
    buf_writer.flush()?;
    Ok(())
}

/// Write with a custom buffer capacity.
/// Smaller capacity = more frequent flushes to the underlying writer.
/// Larger capacity = fewer flushes, more memory.
pub fn write_with_custom_capacity(
    file_path: &str,
    content: &str,
    capacity: usize,
) -> std::io::Result<usize> {
    let file = File::create(file_path)?;
    let mut buf_writer = BufWriter::with_capacity(capacity, file);
    let bytes_written = buf_writer.write(content.as_bytes())?;
    buf_writer.flush()?;
    Ok(bytes_written)
}

/// Get an immutable reference to the underlying writer.
/// Returns a reference to the inner `File` without consuming the `BufWriter`.
pub fn get_underlying_writer(file_path: &str) -> std::io::Result<()> {
    let file = File::create(file_path)?;
    let buf_writer = BufWriter::new(file);
    let _inner: &File = buf_writer.get_ref();
    // You can call File methods like .metadata() on the inner reference
    Ok(())
}

/// Get a mutable reference to the underlying writer.
/// Allows mutating the inner `File` (e.g., seeking) while keeping the buffer.
pub fn get_mutable_underlying_writer(file_path: &str) -> std::io::Result<()> {
    let file = File::create(file_path)?;
    let mut buf_writer = BufWriter::new(file);
    let _inner: &mut File = buf_writer.get_mut();
    // You can mutate the File — but the BufWriter buffer is NOT flushed
    Ok(())
}

/// Unwrap the BufWriter and return ownership of the inner writer.
/// Returns `Ok(File)` on success, or `Err((BufWriter, Error))` if a flush error occurred.
/// After calling this, the BufWriter is consumed and cannot be used.
pub fn into_inner_bufwriter(file_path: &str) -> std::io::Result<File> {
    let file = File::create(file_path)?;
    let buf_writer = BufWriter::new(file);
    let inner_file: File = buf_writer.into_inner()?;
    Ok(inner_file)
}

/// Write_all writes the entire buffer in one call.
/// Unlike `write()`, `write_all()` guarantees all bytes are written or returns an error.
pub fn write_all_bufwriter(file_path: &str, content: &str) -> std::io::Result<()> {
    let file = File::create(file_path)?;
    let mut buf_writer = BufWriter::new(file);
    buf_writer.write_all(content.as_bytes())?;
    buf_writer.flush()?;
    Ok(())
}

/// Demonstrates buffer behavior: data stays in the buffer until flush or buffer fills.
/// Writes content smaller than the buffer — nothing hits disk until flush.
pub fn demonstrate_buffer_behavior(file_path: &str, content: &str) -> std::io::Result<String> {
    let file = File::create(file_path)?;
    let mut buf_writer = BufWriter::with_capacity(1024, file);

    // Write data — it sits in the buffer, NOT on disk yet
    buf_writer.write_all(content.as_bytes())?;

    // File on disk is still empty at this point
    let disk_content = std::fs::read_to_string(file_path).unwrap_or_default();

    // Now flush — data goes to disk
    buf_writer.flush()?;

    let after_flush = std::fs::read_to_string(file_path)?;

    Ok(format!("before_flush: \"{}\"\nafter_flush: \"{}\"", disk_content, after_flush))
}

/// Demonstrates what happens when into_inner() is called with unflushed data.
/// The buffer is flushed automatically during into_inner().
pub fn into_inner_flushes_buffer(file_path: &str, content: &str) -> std::io::Result<String> {
    let file = File::create(file_path)?;
    let mut buf_writer = BufWriter::with_capacity(1024, file);

    // Write data — stays in buffer
    buf_writer.write_all(content.as_bytes())?;

    // into_inner() flushes automatically before returning the File
    let inner_file = buf_writer.into_inner()?;

    let disk_content = std::fs::read_to_string(file_path)?;
    drop(inner_file);

    Ok(disk_content)
}

/// Demonstrates that dropping BufWriter without flush may lose data.
/// Drop silently discards flush errors.
/// Returns what was actually written to disk.
pub fn demonstrate_drop_without_flush(file_path: &str, content: &str) -> std::io::Result<String> {
    let file = File::create(file_path)?;
    {
        let mut buf_writer = BufWriter::with_capacity(1024, file);
        buf_writer.write_all(content.as_bytes())?;
        // buf_writer is dropped here WITHOUT flush — data may be lost
    }

    // Check what actually made it to disk
    let disk_content = std::fs::read_to_string(file_path).unwrap_or_default();
    Ok(disk_content)
}

/// Write multiple small writes — this is where BufWriter shines.
/// Many small writes get batched into one system call on flush.
pub fn write_many_small_writes(file_path: &str, fragments: &[&str]) -> std::io::Result<()> {
    let file = File::create(file_path)?;
    let mut buf_writer = BufWriter::new(file);

    for fragment in fragments {
        buf_writer.write_all(fragment.as_bytes())?;
    }

    buf_writer.flush()?;
    Ok(())
}
