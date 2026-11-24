use ios::*;

#[test]
fn test_read_file() {
    let content = read_file("tests/resources/test_file.txt").unwrap();
    assert_eq!(content, "Hello, world!\n");
}

#[test]
fn test_read_file_lines() {
    let lines = read_file_lines("tests/resources/test_file.txt").unwrap();
    assert_eq!(lines.len(), 1);
    assert_eq!(lines[0], "Hello, world!");
}

#[test]
fn test_count_words_in_file() {
    let count = count_words_in_file("tests/resources/test_file.txt").unwrap();
    assert_eq!(count, 2); // "Hello," and "world!"
}

#[test]
fn test_read_multiline_file() {
    let lines = read_file_lines("tests/resources/multiline_file.txt").unwrap();
    assert_eq!(lines.len(), 4);
    assert_eq!(lines[0], "Hello, world!");
    assert_eq!(lines[1], "This is a test file.");
    assert_eq!(lines[2], "It has multiple lines.");
    assert_eq!(lines[3], "For testing purposes.");
}

#[test]
fn test_count_words_multiline() {
    let count = count_words_in_file("tests/resources/multiline_file.txt").unwrap();
    assert_eq!(count, 14);
}

#[test]
fn test_read_nonexistent_file() {
    let result = read_file("tests/resources/does_not_exist.txt");
    assert!(result.is_err());
}