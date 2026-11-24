use ios::*;

#[test]
fn test_read_first_line() {
    let line = read_first_line("tests/resources/test_file.txt").unwrap();
    assert_eq!(line, "Hello, world!\n");
}

#[test]
fn test_count_lines() {
    let count = count_lines("tests/resources/multiline_file.txt").unwrap();
    assert_eq!(count, 4);
}

#[test]
fn test_bufreader_capacity() {
    let (capacity, buffer_len) = get_bufreader_capacity("tests/resources/test_file.txt").unwrap();
    assert!(buffer_len <= capacity);
    assert!(capacity > 0);
}

#[test]
fn test_get_underlying_file() {
    let result = get_underlying_file("tests/resources/test_file.txt");
    assert!(result.is_ok());
}

#[test]
fn test_get_immutable_ref() {
    let result = get_immutable_ref("tests/resources/test_file.txt");
    assert!(result.is_ok());
}

#[test]
fn test_into_inner() {
    let result = into_inner_demo("tests/resources/test_file.txt");
    assert!(result.is_ok());
}

#[test]
fn test_peek_demo() {
    let result = peek_demo().unwrap();
    assert_eq!(result, "hello there");
}
