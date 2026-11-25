use std::io::{self, BufRead, BufReader, Read, Write};

/// Create an anonymous pipe and return (reader, writer).
/// Cross-platform: works on Unix and Windows.
pub fn create_pipe() -> io::Result<(std::io::PipeReader, std::io::PipeWriter)> {
    std::io::pipe()
}

/// Write a message through the pipe and read it back.
/// Demonstrates basic pipe communication.
/// Uses a writer thread so large messages don't block on pipe buffer limits.
pub fn send_and_receive(message: &str) -> io::Result<String> {
    let (mut reader, mut writer) = std::io::pipe()?;

    // Writer thread: send bytes concurrently so pipe buffer doesn't fill up
    let data = message.as_bytes().to_vec();
    let write_handle = std::thread::spawn(move || writer.write_all(&data));

    // Reader side: consume all bytes
    let mut buf = Vec::new();
    reader.read_to_end(&mut buf)?;

    // Ensure writer finished without error
    write_handle.join().unwrap()?;

    Ok(String::from_utf8(buf).map_err(|e| io::Error::new(io::ErrorKind::InvalidData, e))?)
}

/// Write multiple chunks through the pipe, read line by line.
/// Demonstrates BufReader on top of PipeReader.
/// Uses a writer thread so the pipe buffer doesn't fill up.
pub fn send_lines_and_read(lines: &[&str]) -> io::Result<Vec<String>> {
    let (reader, mut writer) = std::io::pipe()?;

    // Writer thread
    let line_data: Vec<String> = lines.iter().map(|l| format!("{}\n", l)).collect();
    let write_handle = std::thread::spawn(move || {
        for line in line_data {
            writer.write_all(line.as_bytes())?;
        }
        io::Result::Ok(())
    });

    // Reader side
    let buf_reader = BufReader::new(reader);
    let mut result = Vec::new();
    for line in buf_reader.lines() {
        result.push(line?);
    }

    write_handle.join().unwrap()?;

    Ok(result)
}

/// Unix-only: convert PipeReader into an OwnedFd.
/// This demonstrates `impl From<PipeReader> for OwnedFd`.
#[cfg(unix)]
pub fn pipe_reader_to_owned_fd() -> io::Result<std::os::unix::io::OwnedFd> {
    let (reader, _writer) = std::io::pipe()?;
    // PipeReader implements Into<OwnedFd> / From<PipeReader> for OwnedFd
    let owned_fd: std::os::unix::io::OwnedFd = reader.into();
    Ok(owned_fd)
}

/// Windows-only: convert PipeReader into an OwnedHandle.
/// This demonstrates `impl From<PipeReader> for OwnedHandle`.
#[cfg(windows)]
pub fn pipe_reader_to_owned_handle() -> io::Result<std::os::windows::io::OwnedHandle> {
    use std::os::windows::io::OwnedHandle;
    let (reader, _writer) = std::io::pipe()?;
    let owned_handle: OwnedHandle = reader.into();
    Ok(owned_handle)
}

/// Unix-only: read from an OwnedFd obtained from a PipeReader.
/// Shows that the file descriptor is usable after conversion.
#[cfg(unix)]
pub fn read_via_owned_fd(message: &str) -> io::Result<String> {
    use std::os::unix::io::OwnedFd;

    let (reader, mut writer) = std::io::pipe()?;
    let data = message.as_bytes().to_vec();
    let write_handle = std::thread::spawn(move || writer.write_all(&data));

    let owned_fd: OwnedFd = reader.into();
    let mut file = std::fs::File::from(owned_fd);
    let mut buf = String::new();
    file.read_to_string(&mut buf)?;

    write_handle.join().unwrap()?;

    Ok(buf)
}

/// Windows-only: read from an OwnedHandle obtained from a PipeReader.
#[cfg(windows)]
pub fn read_via_owned_handle(message: &str) -> io::Result<String> {
    use std::os::windows::io::OwnedHandle;

    let (reader, mut writer) = std::io::pipe()?;
    let data = message.as_bytes().to_vec();
    let write_handle = std::thread::spawn(move || writer.write_all(&data));

    let owned_handle: OwnedHandle = reader.into();
    let mut file = std::fs::File::from(owned_handle);
    let mut buf = String::new();
    file.read_to_string(&mut buf)?;

    write_handle.join().unwrap()?;

    Ok(buf)
}
