use ios::pipes::pipe_demo;

#[test]
fn test_create_pipe() {
    let (reader, writer) = pipe_demo::create_pipe().unwrap();
    // Just verify they were created — actual use is in other tests
    drop(reader);
    drop(writer);
}

#[test]
fn test_send_and_receive_simple() {
    let result = pipe_demo::send_and_receive("hello pipe").unwrap();
    assert_eq!(result, "hello pipe");
}

#[test]
fn test_send_and_receive_empty() {
    let result = pipe_demo::send_and_receive("").unwrap();
    assert_eq!(result, "");
}

#[test]
fn test_send_and_receive_multibyte() {
    let result = pipe_demo::send_and_receive("hola mundo 🦀").unwrap();
    assert_eq!(result, "hola mundo 🦀");
}

#[test]
fn test_send_and_receive_large() {
    let message = "x".repeat(10_000);
    let result = pipe_demo::send_and_receive(&message).unwrap();
    assert_eq!(result.len(), 10_000);
    assert_eq!(result, message);
}

#[test]
fn test_send_lines_and_read() {
    let lines = &["line one", "line two", "line three"];
    let result = pipe_demo::send_lines_and_read(lines).unwrap();
    assert_eq!(result, vec!["line one", "line two", "line three"]);
}

#[test]
fn test_send_lines_and_read_empty() {
    let result = pipe_demo::send_lines_and_read(&[]).unwrap();
    assert!(result.is_empty());
}

#[test]
fn test_send_lines_and_read_single() {
    let result = pipe_demo::send_lines_and_read(&["solo"]).unwrap();
    assert_eq!(result, vec!["solo"]);
}

#[test]
fn test_send_lines_preserves_order() {
    let lines = &["alpha", "beta", "gamma", "delta"];
    let result = pipe_demo::send_lines_and_read(lines).unwrap();
    assert_eq!(result, lines.to_vec());
}

// --- Platform-specific OwnedFd / OwnedHandle tests ---

#[cfg(unix)]
#[test]
fn test_pipe_reader_to_owned_fd() {
    let owned_fd = pipe_demo::pipe_reader_to_owned_fd().unwrap();
    // OwnedFd is valid — the raw fd number is > 0
    use std::os::unix::io::AsRawFd;
    assert!(owned_fd.as_raw_fd() > 0);
}

#[cfg(unix)]
#[test]
fn test_read_via_owned_fd() {
    let message = "through owned fd";
    let result = pipe_demo::read_via_owned_fd(message).unwrap();
    assert_eq!(result, message);
}

#[cfg(windows)]
#[test]
fn test_pipe_reader_to_owned_handle() {
    let owned_handle = pipe_demo::pipe_reader_to_owned_handle().unwrap();
    use std::os::windows::io::AsRawHandle;
    assert!(!owned_handle.as_raw_handle().is_null());
}

#[cfg(windows)]
#[test]
fn test_read_via_owned_handle() {
    let message = "through owned handle";
    let result = pipe_demo::read_via_owned_handle(message).unwrap();
    assert_eq!(result, message);
}
