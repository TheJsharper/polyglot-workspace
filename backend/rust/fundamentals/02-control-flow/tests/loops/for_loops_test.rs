#[path = "../../src/loops/for_loops.rs"]
mod for_loops;
use for_loops::*;

#[test]
fn test_demonstrate_for_loops() {
    assert_eq!(demonstrate_for_loops(5), Ok(15));
    assert_eq!(demonstrate_for_loops(-1), Err("Inclusive value must be non-negative".to_string()));
}

#[test]
fn test_demonstrate_for_loops_with_step() {
    assert_eq!(demonstrate_for_loops_with_step(5), Ok(6));
    assert_eq!(demonstrate_for_loops_with_step(-1), Err("Inclusive value must be non-negative".to_string()));
}

#[test]
fn test_demonstrate_for_loops_reverse() {
    assert_eq!(demonstrate_for_loops_reverse(5), Ok(15));
    assert_eq!(demonstrate_for_loops_reverse(-1), Err("Inclusive value must be non-negative".to_string()));
}

#[test]
fn test_demonstrate_for_loops_reverse_with_step() {
    assert_eq!(demonstrate_for_loops_reverse_with_step(5), Ok(9));
    assert_eq!(demonstrate_for_loops_reverse_with_step(-1), Err("Inclusive value must be non-negative".to_string()));
}

#[test]
fn test_demonstrate_for_loops_with_step_reverse() {
    assert_eq!(demonstrate_for_loops_with_step_reverse(5), Ok(9));
    assert_eq!(demonstrate_for_loops_with_step_reverse(-1), Err("Inclusive value must be non-negative".to_string()));
}

#[test]
fn test_demonstrate_for_loops_with_step_reverse_with_from_inclusive() {
    // (1..=5).rev().step_by(2) = [5, 3, 1] = 9
    assert_eq!(demonstrate_for_loops_with_step_reverse_with_from_inclusive(1, 5), Ok(9));
    assert_eq!(demonstrate_for_loops_with_step_reverse_with_from_inclusive(-1, 5), Err("Inclusive and from values must be non-negative and from must be less than or equal to inclusive".to_string()));
}

#[test]
fn test_demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step() {
    // (1..=5).rev().step_by(2) = [5, 3, 1] = 9
    assert_eq!(demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step(1, 5, 2), Ok(9));
    assert_eq!(demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step(-1, 5, 2), Err("Inclusive and from values must be non-negative and from must be less than or equal to inclusive".to_string()));
}

#[test]
fn test_demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse() {
    // (1..=5).rev().step_by(2) = [5, 3, 1] = 9
    assert_eq!(demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse(1, 5, 2), Ok(9));
    assert_eq!(demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse(-1, 5, 2), Err("Inclusive and from values must be non-negative and from must be less than or equal to inclusive".to_string()));
}

#[test]
fn test_demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse_and_step() {
    // (1..=5).rev().step_by(2) = [5, 3, 1] = 9
    assert_eq!(demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse_and_step(1, 5, 2), Ok(9));
    assert_eq!(demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse_and_step(-1, 5, 2), Err("Inclusive and from values must be non-negative and from must be less than or equal to inclusive".to_string()));
}

#[test]
fn test_demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse_and_step_and_reverse() {
    // (1..=5).rev().step_by(2) = [5, 3, 1] = 9
    assert_eq!(demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse_and_step_and_reverse(1, 5, 2), Ok(9));
    assert_eq!(demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse_and_step_and_reverse(-1, 5, 2), Err("Inclusive and from values must be non-negative and from must be less than or equal to inclusive".to_string()));
}
