use borrow_demo_crate::*;
use std::borrow::Cow;

// ─────────────────────────────────────────────
// ToOwned tests
// ─────────────────────────────────────────────

#[test]
fn test_to_owned_demo() {
    let owned = to_owned_demo();
    assert_eq!(owned, "hello from borrowed");
}

#[test]
fn test_to_owned_slice_demo() {
    let owned = to_owned_slice_demo();
    assert_eq!(owned, vec![1, 2, 3, 4, 5]);
}

// ─────────────────────────────────────────────
// Borrow / BorrowMut tests
// ─────────────────────────────────────────────

#[test]
fn test_borrow_trait_demo() {
    // String implements Borrow<str> directly (pass by value)
    let key_string = "apple".to_string();
    assert_eq!(borrow_trait_demo(key_string), 5);

    // &str also implements Borrow<str>
    assert_eq!(borrow_trait_demo("banana"), 6);
}

#[test]
fn test_borrow_mut_trait_demo() {
    let mut value = String::from("hello");
    let len = borrow_mut_trait_demo(&mut value);
    assert_eq!(len, 5);
    assert_eq!(value, "HELLO");
}

#[test]
fn test_borrow_hashmap_style_demo() {
    let found = borrow_hashmap_style_demo();
    assert_eq!(found, 3);
}

// ─────────────────────────────────────────────
// Cow tests
// ─────────────────────────────────────────────

#[test]
fn test_cow_borrowed_demo() {
    let (is_borrowed, len) = cow_borrowed_demo();
    assert!(is_borrowed);
    assert_eq!(len, 14);
}

#[test]
fn test_cow_owned_demo() {
    let (is_owned, content) = cow_owned_demo();
    assert!(is_owned);
    assert_eq!(content, "owned data");
}

#[test]
fn test_cow_clone_on_write_demo() {
    let (was_borrowed, is_owned_after, content) = cow_clone_on_write_demo();
    assert!(was_borrowed);
    assert!(is_owned_after);
    assert_eq!(content, "HELLO WORLD");
}

#[test]
fn test_cow_owned_no_clone_demo() {
    let (was_owned, is_owned_after, content) = cow_owned_no_clone_demo();
    assert!(was_owned);
    assert!(is_owned_after);
    assert_eq!(content, "ALREADY OWNED");
}

#[test]
fn test_cow_function_return_demo_lowercase() {
    let (is_borrowed, content) = cow_function_return_demo("hello world");
    assert!(is_borrowed);
    assert_eq!(content, "hello world");
}

#[test]
fn test_cow_function_return_demo_mixed() {
    let (is_borrowed, content) = cow_function_return_demo("Hello World");
    assert!(!is_borrowed);
    assert_eq!(content, "hello world");
}

#[test]
fn test_cow_slice_demo() {
    let (was_borrowed, is_owned_after, content) = cow_slice_demo();
    assert!(was_borrowed);
    assert!(is_owned_after);
    assert_eq!(content, vec![100, 2, 3]);
}

#[test]
fn test_cow_deref_demo() {
    let (len, first) = cow_deref_demo();
    assert_eq!(len, 12);
    assert_eq!(first, 'd');
}

#[test]
fn test_cow_lifetime_demo() {
    let data = "lifetime test";
    let result = cow_lifetime_demo(data);
    assert_eq!(result, "lifetime test");
}

#[test]
fn test_cow_comparison_demo() {
    let (efficient, when_needed) = cow_comparison_demo();
    assert!(matches!(efficient, Cow::Borrowed(_)));
    assert!(matches!(when_needed, Cow::Owned(_)));
}

#[test]
fn test_borrow_module_description() {
    let description = borrow_module_description();
    assert!(description.contains("Cow"));
    assert!(description.contains("Borrow"));
    assert!(description.contains("ToOwned"));
}
