use ios::*;

#[test]
fn test_read_first_line() {
    let first_line = read_first_line("tests/resources/test_file.txt").unwrap();

    assert_eq!(first_line, "Hello, world!\n");
}

#[test]
fn test_count_lines() {
    let line_count = count_lines("tests/resources/multiline_file.txt").unwrap();

    assert_eq!(line_count, 4);
}

#[test]
fn test_bufreader_capacity() {
    let (buffer_capacity, filled_length) =
        get_bufreader_capacity("tests/resources/test_file.txt").unwrap();

    assert!(filled_length <= buffer_capacity);

    assert!(buffer_capacity > 0);
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
