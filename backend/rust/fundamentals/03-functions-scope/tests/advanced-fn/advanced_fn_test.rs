#[path = "../../src/advanced-fn/advanced_fn.rs"]
mod advanced_fn;
use advanced_fn::*;

#[test]
fn test_apply_closure() {
    assert_eq!(apply_closure(5, |x| x * 2), 10);
}

#[test]
fn test_map_slice() {
    let nums = [1, 2, 3, 4];
    let doubled: Vec<i32> = map_slice(&nums, |x| x * 2);
    assert_eq!(doubled, vec![2, 4, 6, 8]);
}

#[test]
fn test_filter_slice() {
    let nums = [1, 2, 3, 4, 5, 6];
    let evens = filter_slice(&nums, |x| *x % 2 == 0);
    assert_eq!(evens, vec![&2, &4, &6]);
}

#[test]
fn test_fold_slice() {
    let nums = [1, 2, 3, 4];
    assert_eq!(fold_slice(&nums, 0, |acc, x| acc + x), 10);
    assert_eq!(fold_slice(&nums, 1, |acc, x| acc * x), 24);
}

#[test]
fn test_make_offset_adder() {
    let add_ten = make_offset_adder(10);
    assert_eq!(add_ten(5), 15);
    assert_eq!(add_ten(0), 10);
}

#[test]
fn test_make_counter() {
    let mut counter = make_counter();
    assert_eq!(counter(), 1);
    assert_eq!(counter(), 2);
    assert_eq!(counter(), 3);
}

#[test]
fn test_compose() {
    let double_then_add_one = compose(|x| x + 1, |x| x * 2);
    assert_eq!(double_then_add_one(3), 7); // (3 * 2) + 1 = 7
}

#[test]
fn test_chain_apply() {
    let double = |x: i32| x * 2;
    let add_one = |x: i32| x + 1;
    let triple = |x: i32| x * 3;
    assert_eq!(chain_apply(1, &[double, add_one, triple]), 9); // ((1 * 2) + 1) * 3 = 9
}

#[test]
fn test_curried_add() {
    let add_five = curried_add(5);
    assert_eq!(add_five(3), 8);
    assert_eq!(curried_add(10)(20), 30);
}

#[test]
fn test_partial_apply() {
    let multiply = |a: i32, b: i32| a * b;
    let double = partial_apply(multiply, 2);
    assert_eq!(double(5), 10);
    assert_eq!(double(7), 14);
}

#[test]
fn test_all_match() {
    let nums = [2, 4, 6, 8];
    assert!(all_match(&nums, |x| *x % 2 == 0));
    assert!(!all_match(&nums, |x| *x > 3));
}

#[test]
fn test_any_match() {
    let nums = [1, 3, 5, 7];
    assert!(any_match(&nums, |x| *x % 2 != 0));
    assert!(!any_match(&nums, |x| *x % 2 == 0));
}

#[test]
fn test_find_first() {
    let nums = [1, 3, 5, 6, 7, 8];
    assert_eq!(find_first(&nums, |x| *x % 2 == 0), Some(&6));
    assert_eq!(find_first(&nums, |x| *x > 10), None);
}
