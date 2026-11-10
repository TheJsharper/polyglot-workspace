#[path = "../../src/operators/operators.rs"]
mod operators;
use operators::{
    MY_OPERATOR_NUMBER, MY_OPERATOR_SUBTRACTION, MY_OPERATOR_MULTIPLICATION, MY_OPERATOR_DIVISION,
    MY_OPERATOR_REMAINDER,
};

#[test]
fn test_operators() {
    assert_eq!(MY_OPERATOR_NUMBER, 8);
    assert_eq!(MY_OPERATOR_SUBTRACTION, 6);
    assert_eq!(MY_OPERATOR_MULTIPLICATION, 42);
    assert_eq!(MY_OPERATOR_DIVISION, 4);
    assert_eq!(MY_OPERATOR_REMAINDER, 2);
}
