#[path = "../../src/basics/basics_ownership.rs"]
mod basics_ownership;
use basics_ownership::*;

// --- Move Semantics ---

#[test]
fn test_demonstrate_move() {
    // String moves — a becomes invalid after `let b = a`
    // The function returns b, proving ownership transferred successfully
    assert_eq!(demonstrate_move(), "Hello");
}

// --- Copy Types ---

#[test]
fn test_demonstrate_copy_type() {
    // i32 implements Copy — both a and b remain valid
    let (a, b) = demonstrate_copy_type();
    assert_eq!(a, 5);
    assert_eq!(b, 5);
}

#[test]
fn test_demonstrate_copy_bool() {
    // bool implements Copy — both remain valid
    let (a, b) = demonstrate_copy_bool();
    assert!(a);
    assert!(b);
}

#[test]
fn test_demonstrate_copy_char() {
    // char implements Copy — both remain valid
    let (a, b) = demonstrate_copy_char();
    assert_eq!(a, 'A');
    assert_eq!(b, 'A');
}

// --- Ownership + Functions ---

#[test]
fn test_string_length() {
    // Borrows — doesn't take ownership
    assert_eq!(string_length("Hello"), 5);
    assert_eq!(string_length(""), 0);
    assert_eq!(string_length("🦀"), 4); // byte length
}

#[test]
fn test_give_back() {
    // Takes ownership but returns it — caller keeps access
    let s = String::from("Hello");
    let result = give_back(s);
    assert_eq!(result, "Hello");
    // s was moved into the function, but returned as result
}

#[test]
fn test_consume_and_drop() {
    // Takes ownership and drops it — only returns the length
    let s = String::from("Hello");
    let len = consume_and_drop(s);
    assert_eq!(len, 5);
    // s is gone — we can't use it anymore
}

// --- Ownership Rules ---

#[test]
fn test_demonstrate_scope_drop() {
    // Value is dropped at end of scope, but we kept the computed result
    assert_eq!(demonstrate_scope_drop(), 5);
}

#[test]
fn test_demonstrate_mutable_borrow_exclusive() {
    // Mutable borrow is exclusive — s can't be used while borrowed
    assert_eq!(demonstrate_mutable_borrow_exclusive(), "Hello, world!");
}

#[test]
fn test_demonstrate_multiple_immutable_borrows() {
    // Multiple immutable borrows can coexist
    let (len1, len2) = demonstrate_multiple_immutable_borrows();
    assert_eq!(len1, 13);
    assert_eq!(len2, 26); // 13 + 13
}
