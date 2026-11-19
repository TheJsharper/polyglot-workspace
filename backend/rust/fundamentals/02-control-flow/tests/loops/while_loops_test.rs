#[path = "../../src/loops/while_loops.rs"]
mod loops;
use loops::*;

#[test]
fn test_while_loops() {
    // Example test for while loops
    let result = print_sum_to_end(3);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 6);
}

#[test]
fn test_while_loops_invalid_input() {
    let result = print_sum_to_end(0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Input must be greater than or equal to 1");
}

#[test]
fn test_count_to_n() {
    let result = count_to_n(3);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), vec![1, 2, 3]);
}

#[test]
fn test_count_to_n_invalid_input() {
    let result = count_to_n(0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Input must be greater than or equal to 1");
}

#[test]
fn test_is_prime() {
    let result = is_prime(5);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), true);
}

#[test]
fn test_is_prime_invalid_input() {
    let result = is_prime(1);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Input must be greater than or equal to 2");
}

#[test]
fn test_gcd() {
    let result = gcd(12, 18);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 6);
}

#[test]
fn test_gcd_invalid_input() {
    let result = gcd(0, 18);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Input must be greater than or equal to 1");
}

#[test]
fn test_lcm() {
    let result = lcm(12, 18);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 36);
}

#[test]
fn test_lcm_invalid_input() {
    let result = lcm(0, 18);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Input must be greater than or equal to 1");
}
#[test]
fn test_factorial() {
    let result = factorial(5);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 120);
}

#[test]
fn test_factorial_invalid_input() {
    let result = factorial(0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Input must be greater than or equal to 1");
}

#[test]
fn test_fibonacci() {
    let result = fibonacci(6);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 8);
}

#[test]
fn test_fibonacci_invalid_input() {
    let result = fibonacci(0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Input must be greater than or equal to 1");
}

#[test]
fn test_sum_to_n() {
    let result = sum_to_n(5);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 15);
}

#[test]
fn test_sum_to_n_invalid_input() {
    let result = sum_to_n(0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Input must be greater than or equal to 1");
}

#[test]
fn test_print_sum_to_end() {
    let result = print_sum_to_end(5);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 15);
}

#[test]
fn test_print_sum_to_end_invalid_input() {
    let result = print_sum_to_end(0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "Input must be greater than or equal to 1");
}