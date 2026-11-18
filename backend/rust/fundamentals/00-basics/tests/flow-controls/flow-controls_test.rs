#[path = "../../src/flow-controls/flow-control.rs"]
mod flow_controls;
use flow_controls::*;
//use std::fmt::format;

#[test]
fn test_less_than() -> () {
    assert!(LESS_THAN);
}
#[test]
fn test_greater_then() -> () {
    assert!(GREATER_THAN)
}

#[test]
fn test_less_than_or_equal() -> () {
    assert!(LESS_THAN_OR_EQUAL)
}

#[test]
fn test_greater_then_or_equal() -> () {
    assert!(GREATER_THAN_OR_EQUAL);
}

#[test]

fn test_equal_to() -> () {
    assert!(EQUAL_TO)
}

#[test]
fn test_not_equal_to() -> () {
    assert!(NOT_EQUAL_TO)
}
#[test]
fn test_is_greater() -> () {
    assert!(is_greater(7, 5));
    assert!(!is_greater(3, 5))
}

#[test]
fn test_comparison_message() -> () {
    let a: i32 = 7;
    let b: i32 = 5;
    assert_eq!(comparison_message(a, b), format!("{a} is greater than {b}"));
    assert_eq!(
        comparison_message(b, a),
        format!("{b} is not greater than {a}")
    );
}
#[test]
fn test_greeting_message()->(){

    assert_eq!(greeting_message(20), "Good evening");
    assert_eq!(greeting_message(17), "Good Day");
}