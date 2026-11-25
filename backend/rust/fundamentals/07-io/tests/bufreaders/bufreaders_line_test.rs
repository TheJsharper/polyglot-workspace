use ios::*;

#[test]
fn test_read_lines_from_bufreader() {

    let file_path = "tests/resources/test_file.txt";

    let lines = read_lines_from_bufreader(file_path).unwrap();

    assert_eq!(lines, vec!["Hello, world!"]);
}

#[test]
fn test_read_lines_from_bufreader_multiple_lines() {

    let file_path = "tests/resources/multiline_file.txt";

    let lines = read_lines_from_bufreader(file_path).unwrap();

    assert_eq!(lines.len(), 4);

    assert_eq!(lines[0], "Hello, world!");
}

#[test]
fn test_read_lines_from_bufreader_empty_file() {

    let file_path = "tests/resources/empty_file.txt";

    std::fs::write(&file_path, "").unwrap();

    let lines = read_lines_from_bufreader(file_path).unwrap();

    assert!(lines.is_empty());

    let _ = std::fs::remove_file(file_path);
}

#[test]
fn test_read_lines_from_bufreader_file_not_found() {

    let file_path = "tests/resources/does_not_exist.txt";

    let result = read_lines_from_bufreader(file_path);

    assert!(result.is_err());

    let error = result.unwrap_err();

    assert_eq!(error.kind(), std::io::ErrorKind::NotFound);

    assert!(error.to_string().contains("File not found"));
}

#[test]
fn test_read_lines_with_custom_capacity() {

    let file_path = "tests/resources/test_file.txt";

    let lines = read_lines_with_custom_capacity(file_path, 16).unwrap();

    assert_eq!(lines, vec!["Hello, world!"]);
}

#[test]
fn test_seek_in_file() {

    let file_path = "tests/resources/test_file.txt";

    let _ = seek_in_file(file_path, 0).unwrap();
}

#[test]
fn test_read_lines_from_bufreader_to_string_buf() {

    let file_path = "tests/resources/test_file.txt";

    let content = read_lines_from_bufreader_to_string_buf(file_path).unwrap();

    assert_eq!(content, "Hello, world!\n");
}

#[cfg(any())] // Disabled: stdin is read-only; cannot write to it. Requires refactoring functions to accept impl Read.
#[test]
fn test_read_lines_from_std_in_to_string_buf() {

    use std::io::Write;
    let input = b"Hello, world!\n";
    let mut stdin = std::io::stdin();
    let mut handle = stdin.lock();
    handle.write_all(input).unwrap();

    let content = read_lines_from_std_in_to_string_buf().unwrap();

    assert_eq!(content, "Hello, world!\n");
}

#[cfg(any())] // Disabled: stdin is read-only; cannot write to it. Requires refactoring functions to accept impl Read.
#[test]
fn test_read_lines_from_std_in_to_string_buf_with_iterator() {

    use std::io::Write;
    let input = b"Hello, world!\n";
    let mut stdin = std::io::stdin();
    let mut handle = stdin.lock();
    handle.write_all(input).unwrap();

    let content = read_lines_from_std_in_to_string_buf_with_iterator().unwrap();

    assert_eq!(content, "Hello, world!\n");
}

#[test]
fn test_read_write_with_pipe() {

    let input_file = "tests/resources/test_file.txt";
    let output_file = "tests/resources/output_file.txt";

    read_write_with_pipe(input_file, output_file).unwrap();

    let content = std::fs::read_to_string(output_file).unwrap();

    assert_eq!(content, "Hello, world!\n");

    let _ = std::fs::remove_file(output_file);
}
