#[path = "../../src/basics/basics_str.rs"]
mod basics_str;

#[test]
fn test_demonstrate_basic_string() {
    assert_eq!(basics_str::demonstrate_basic_string(), "Hello, world!");
}

#[test]
fn test_demonstrate_basic_from_str() {
    assert_eq!(basics_str::demonstrate_basic_from_str(), "Hello, world!");
}

#[test]
fn test_demostrate_basic_from_method() {
    assert_eq!(basics_str::demostrate_basic_from_method(), "Hello, world!");
}

#[test]
fn test_demonstrate_basic_string_with_format() {
    assert_eq!(basics_str::demonstrate_basic_string_with_format(), "Hello, world!");
}

#[test]
fn test_demonstrate_basic_string_with_argument_and_format() {
    assert_eq!(basics_str::demonstrate_basic_string_with_argument_and_format("Alice"), "Hello, Alice!");
}

#[test]
fn test_demonstrate_basic_string_with_multiple_arguments_and_format() {
    assert_eq!(basics_str::demonstrate_basic_string_with_multiple_arguments_and_format("Alice", 30), "Hello, Alice! You are 30 years old.");
}

#[test]
fn test_demonstrate_basic_string_with_named_arguments_and_format() {
    assert_eq!(basics_str::demonstrate_basic_string_with_named_arguments_and_format("Alice", 30), "Hello, Alice! You are 30 years old.");
}

#[test]
fn test_demostrate_basic_mutate_string() {
    assert_eq!(basics_str::demostrate_basic_mutate_string(), "Hello, world!");
}

#[test]
fn test_demonstrate_basic_concatenate_strings() {
    assert_eq!(basics_str::demonstrate_basic_concatenate_strings(), "Hello, world!");
}

#[test]
fn test_demonstrate_basic_concatenate_strings_with_format() {
    assert_eq!(basics_str::demonstrate_basic_concatenate_strings_with_format(), "Hello, world!");
}