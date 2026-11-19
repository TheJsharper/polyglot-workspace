#[path = "../../src/loops/loops.rs"]
mod loops;
use loops::{print_to_end, print_to_start, print_range, print_sum_to_end, print_sum_to_start, print_sum_range};    

#[test]
fn test_print_to_end() {
    let result = print_to_end(3);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), "1\n2\n3\n");
}

#[test]
fn test_print_to_end_invalid() {
    let result = print_to_end(0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "End must be greater than or equal to 1");
}

#[test]
fn test_print_to_start() {
    let result = print_to_start(3);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), "3\n2\n1\n");
}

#[test]
fn test_print_to_start_invalid() {
    let result = print_to_start(0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Start must be greater than or equal to 1");
}

#[test]
fn test_print_range() {
    let result = print_range(1, 3);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 6);
}

#[test]
fn test_print_range_invalid() {
    let result = print_range(3, 1);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Start must be less than or equal to end");
}
#[test]
fn test_print_sum_to_end() {
    let result = print_sum_to_end(3);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 6);
}

#[test]
fn test_print_sum_to_start() {
    let result = print_sum_to_start(3);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 6);
}

#[test]
fn test_print_sum_range() {
    let result = print_sum_range(1, 3);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 6);
}

#[test]
fn test_print_sum_range_invalid() {
    let result = print_sum_range(3, 1);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Start must be less than or equal to end");
}
#[test]
fn test_print_sum_to_end_invalid() {
    let result = print_sum_to_end(0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "End must be greater than or equal to 1");
}
#[test]
fn test_print_sum_to_start_invalid() {
    let result = print_sum_to_start(0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Start must be greater than or equal to 1");
}

#[test]
fn test_print_range_invalid_start() {
    let result = print_range(0, 3);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Start and end must be greater than or equal to 1");
}

#[test]
fn test_print_sum_range_invalid_start() {
    let result = print_sum_range(0, 3);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Start and end must be greater than or equal to 1");
}

#[test]
fn test_print_range_invalid_end() {
    let result = print_range(1, 0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Start and end must be greater than or equal to 1");
}

#[test]
fn test_print_sum_range_invalid_end() {
    let result = print_sum_range(1, 0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Start and end must be greater than or equal to 1");
}