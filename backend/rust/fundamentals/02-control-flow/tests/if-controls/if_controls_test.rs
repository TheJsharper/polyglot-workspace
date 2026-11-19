#[path = "../../src/if-controls/if-controls.rs"]
mod if_controls;

use if_controls::{test, is_zero, greater_than, less_than, greater_than_or_equal, less_than_or_equal, equal, is_even, is_odd, is_positive, is_negative, greater};

#[test]
fn test_smoke() {
    assert_eq!(test(), "Hello from test!!");
}

#[test]
fn test_is_zero() {
    assert_eq!(is_zero(0), true);
    assert_eq!(is_zero(1), false);
}

#[test]
fn test_greater_than() {
    assert_eq!(greater_than(2, 1), true);
    assert_eq!(greater_than(1, 2), false);
}

#[test]
fn test_less_than() {
    assert_eq!(less_than(1, 2), true);
    assert_eq!(less_than(2, 1), false);
}

#[test]
fn test_greater_than_or_equal() {
    assert_eq!(greater_than_or_equal(2, 2), true);
    assert_eq!(greater_than_or_equal(1, 2), false);
}

#[test]
fn test_less_than_or_equal() {
    assert_eq!(less_than_or_equal(2, 2), true);
    assert_eq!(less_than_or_equal(2, 1), false);
}

#[test]
fn test_equal() {
    assert_eq!(equal(2, 2), true);
    assert_eq!(equal(2, 1), false);
}

#[test]
fn test_is_even() {
    assert_eq!(is_even(2), true);
    assert_eq!(is_even(3), false);
}

#[test]
fn test_is_odd() {
    assert_eq!(is_odd(3), true);
    assert_eq!(is_odd(2), false);
}

#[test]
fn test_is_positive() {
    assert_eq!(is_positive(3), true);
    assert_eq!(is_positive(-2), false);
}

#[test]
fn test_is_negative() {
    assert_eq!(is_negative(-3), true);
    assert_eq!(is_negative(2), false);
}

#[test]
fn test_greater() {
    assert_eq!(greater(3, 2), 3);
    assert_eq!(greater(2, 3), 3);
}