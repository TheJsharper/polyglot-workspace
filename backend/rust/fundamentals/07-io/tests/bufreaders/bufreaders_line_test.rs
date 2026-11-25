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
