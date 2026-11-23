#[path = "../../src/advanced/advanced_ownership.rs"]
mod advanced_ownership;
use advanced_ownership::*;

// --- Clone vs Copy Deep Dive ---

#[test]
fn test_demonstrate_clone() {
    // Both strings are valid and independent after clone
    let (a, b) = demonstrate_clone();
    assert_eq!(a, "Hello");
    assert_eq!(b, "Hello");
    // They are independent — modifying one wouldn't affect the other
}

#[test]
fn test_demonstrate_clone_vec() {
    // Vec clones recursively — the entire contents are copied
    let (a, b) = demonstrate_clone_vec();
    assert_eq!(a, vec![1, 2, 3]);
    assert_eq!(b, vec![1, 2, 3]);
}

// --- Reference Types ---

#[test]
fn test_reference_string() {
    // &String borrows — caller retains ownership
    let s = String::from("Hello");
    assert_eq!(reference_string(&s), 5);
    assert_eq!(s, "Hello"); // s is still valid
}

#[test]
fn test_reference_str() {
    // &str is more flexible — accepts String, &str, and literals
    let s = String::from("Hello");
    assert_eq!(reference_str(&s), 5);
    assert_eq!(reference_str("world"), 5);
    assert_eq!(reference_str("Hello, world!"), 13);
}

#[test]
fn test_mutate_via_reference() {
    // &mut gives exclusive write access
    let mut s = String::from("Hello");
    mutate_via_reference(&mut s);
    assert_eq!(s, "Hello — mutated!");
}

#[test]
fn test_mixed_borrowing_pattern() {
    // Mutable borrow ends before immutable read — correct pattern
    let (s, length) = mixed_borrowing_pattern();
    assert_eq!(s, "Hello, world!");
    assert_eq!(length, 13);
}

// --- Box<T> ---

#[test]
fn test_demonstrate_box() {
    let b = demonstrate_box();
    assert_eq!(*b, 42);
}

#[test]
fn test_demonstrate_box_string() {
    let b = demonstrate_box_string();
    assert_eq!(*b, "Hello from the heap");
}

#[test]
fn test_unbox_value() {
    let b = Box::new(42);
    let value = unbox_value(b);
    assert_eq!(value, 42);
    // b is consumed — can't use it anymore
}

// --- Rc<T> ---

#[test]
fn test_demonstrate_rc() {
    let (a, b) = demonstrate_rc();
    assert_eq!(*a, "shared");
    assert_eq!(*b, "shared");
    // Both point to the same heap data
}

#[test]
fn test_rc_strong_count() {
    let rc = std::rc::Rc::new(String::from("shared"));
    assert_eq!(rc_strong_count(&rc), 1);

    let rc2 = rc.clone();
    assert_eq!(rc_strong_count(&rc), 2);

    drop(rc2);
    assert_eq!(rc_strong_count(&rc), 1);
}

// --- Ownership in Structs ---

#[test]
fn test_demonstrate_owned_struct() {
    let data = demonstrate_owned_struct();
    assert_eq!(data.name, "example");
    assert_eq!(data.values, vec![1, 2, 3]);
}

#[test]
fn test_demonstrate_borrowed_struct() {
    let data = demonstrate_borrowed_struct();
    assert_eq!(data.name, "static");
    assert_eq!(data.values, &[1, 2, 3]);
}

#[test]
fn test_owned_struct_move_semantics() {
    // Structs with owned fields follow move semantics
    let a = OwnedData::new("test", vec![1]);
    let b = a; // moves — a is invalid
    assert_eq!(b.name, "test");
    assert_eq!(b.values, vec![1]);
}

// --- Ownership Transfer Patterns ---

#[test]
fn test_transform_consume() {
    let s = String::from("hello");
    let result = transform_consume(s);
    assert_eq!(result, "HELLO");
    // s is consumed — can't use it anymore
}

#[test]
fn test_transform_mutate_in_place() {
    let s = String::from("hello");
    let result = transform_mutate_in_place(s);
    assert_eq!(result, "hello — appended");
}

#[test]
fn test_transform_borrow() {
    let s = String::from("hello");
    let result = transform_borrow(&s);
    assert_eq!(result, "HELLO");
    assert_eq!(s, "hello"); // s is still valid — it was borrowed
}

// --- Deref Coercion ---

#[test]
fn test_demonstrate_deref_coercion() {
    let (from_string, from_str, from_box) = demonstrate_deref_coercion();
    assert_eq!(from_string, 5);
    assert_eq!(from_str, 5);
    assert_eq!(from_box, 5);
    // All three produce the same result — coercion works
}

// --- Move vs Copy Type Registry ---

#[test]
fn test_copy_vs_move_summary() {
    let (a, b, c, d, e, f, g, h, i) = copy_vs_move_summary();
    assert_eq!(a, 42);
    assert_eq!(b, 42);
    assert!((c - 3.14).abs() < f64::EPSILON);
    assert_eq!(d, 3.14);
    assert!(e);
    assert!(f);
    assert_eq!(g, 'Z');
    assert_eq!(h, 'Z');
    assert_eq!(i, (1, true));
    // All variables are still valid — all are Copy types
}
