#[path = "../../src/basics/basics_str.rs"]
mod basics_str;
use basics_str::*;

#[test]
fn test_demonstrate_basic_string() {
    assert_eq!(demonstrate_basic_string(), "Hello, world!");
}

#[test]
fn test_demonstrate_basic_from_str() {
    assert_eq!(demonstrate_basic_from_str(), "Hello, world!");
}

#[test]
fn test_demostrate_basic_from_method() {
    assert_eq!(demostrate_basic_from_method(), "Hello, world!");
}

#[test]
fn test_demonstrate_basic_string_with_format() {
    assert_eq!(demonstrate_basic_string_with_format(), "Hello, world!");
}

#[test]
fn test_demonstrate_basic_string_with_argument_and_format() {
    assert_eq!(demonstrate_basic_string_with_argument_and_format("Alice"), "Hello, Alice!");
}

#[test]
fn test_demonstrate_basic_string_with_multiple_arguments_and_format() {
    assert_eq!(demonstrate_basic_string_with_multiple_arguments_and_format("Alice", 30), "Hello, Alice! You are 30 years old.");
}

#[test]
fn test_demonstrate_basic_string_with_named_arguments_and_format() {
    assert_eq!(demonstrate_basic_string_with_named_arguments_and_format("Alice", 30), "Hello, Alice! You are 30 years old.");
}

#[test]
fn test_demostrate_basic_mutate_string() {
    assert_eq!(demostrate_basic_mutate_string(), "Hello, world!");
}

#[test]
fn test_demonstrate_basic_concatenate_strings() {
    assert_eq!(demonstrate_basic_concatenate_strings(), "Hello, world!");
}

#[test]
fn test_demonstrate_basic_concatenate_strings_with_format() {
    assert_eq!(demonstrate_basic_concatenate_strings_with_format(), "Hello, world!");
}

// --- Basic String Methods ---

#[test]
fn test_demonstrate_string_length() {
    assert_eq!(demonstrate_string_length("Hello"), 5);
    assert_eq!(demonstrate_string_length(""), 0);
    assert_eq!(demonstrate_string_length("🦀"), 4); // emoji is 4 bytes
}

#[test]
fn test_demonstrate_string_is_empty() {
    assert!(demonstrate_string_is_empty(""));
    assert!(!demonstrate_string_is_empty("Hello"));
}

#[test]
fn test_demonstrate_string_contains() {
    assert!(demonstrate_string_contains("Hello, world!", "world"));
    assert!(!demonstrate_string_contains("Hello, world!", "Rust"));
}

#[test]
fn test_demonstrate_string_starts_with() {
    assert!(demonstrate_string_starts_with("Hello, world!", "Hello"));
    assert!(!demonstrate_string_starts_with("Hello, world!", "world"));
}

#[test]
fn test_demonstrate_string_ends_with() {
    assert!(demonstrate_string_ends_with("Hello, world!", "world!"));
    assert!(!demonstrate_string_ends_with("Hello, world!", "Hello"));
}

#[test]
fn test_demonstrate_string_replace() {
    assert_eq!(demonstrate_string_replace("Hello, world!", "world", "Rust"), "Hello, Rust!");
    assert_eq!(demonstrate_string_replace("aaa", "a", "b"), "bbb");
}

#[test]
fn test_demonstrate_string_to_uppercase() {
    assert_eq!(demonstrate_string_to_uppercase("hello"), "HELLO");
}

#[test]
fn test_demonstrate_string_to_lowercase() {
    assert_eq!(demonstrate_string_to_lowercase("HELLO"), "hello");
}

#[test]
fn test_demonstrate_string_trim() {
    assert_eq!(demonstrate_string_trim("  hello  "), "hello");
    assert_eq!(demonstrate_string_trim("hello"), "hello");
}

#[test]
fn test_demonstrate_string_split() {
    assert_eq!(demonstrate_string_split("a,b,c", ","), vec!["a", "b", "c"]);
    assert_eq!(demonstrate_string_split("hello", " "), vec!["hello"]);
}

#[test]
fn test_demonstrate_string_find() {
    assert_eq!(demonstrate_string_find("Hello, world!", "world"), Some(7));
    assert_eq!(demonstrate_string_find("Hello, world!", "Rust"), None);
}

#[test]
fn test_demonstrate_string_slice() {
    assert_eq!(demonstrate_string_slice("Hello, world!", 0, 5), "Hello");
    assert_eq!(demonstrate_string_slice("Hello, world!", 7, 12), "world");
}

#[test]
fn test_demonstrate_string_push_char() {
    let mut s = String::from("Hello");
    demonstrate_string_push_char(&mut s, '!');
    assert_eq!(s, "Hello!");
}

#[test]
fn test_demonstrate_string_clear() {
    let mut s = String::from("Hello");
    demonstrate_string_clear(&mut s);
    assert!(s.is_empty());
}

#[test]
fn test_demonstrate_string_chars() {
    assert_eq!(demonstrate_string_chars("abc"), vec!['a', 'b', 'c']);
    assert_eq!(demonstrate_string_chars("🦀"), vec!['🦀']);
}

#[test]
fn test_demonstrate_string_repeat() {
    assert_eq!(demonstrate_string_repeat("ab", 3), "ababab");
    assert_eq!(demonstrate_string_repeat("x", 0), "");
}