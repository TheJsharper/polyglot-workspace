# Loops Module — Test Review

## Current State

| Metric          | Value         |
| --------------- | ------------- |
| Total tests     | 16            |
| All passing     | yes           |
| Functions tested| 6             |
| Error path tests| 9             |
| Happy path tests| 7             |

---

## Test Coverage Map

| Function                | Happy | Invalid Input | Boundary | Missing           |
| ----------------------- | :---: | :-----------: | :------: | ----------------- |
| `print_to_end`          | yes   | yes (0)       | no       | negative end      |
| `print_to_start`        | yes   | yes (0)       | no       | negative start    |
| `print_range`           | yes   | yes (3,1)     | yes      | equal start==end  |
| `print_sum_to_end`      | yes   | yes (0)       | no       | negative end      |
| `print_sum_to_start`    | yes   | yes (0)       | no       | negative start    |
| `print_sum_range`       | yes   | yes (3,1)     | yes      | equal start==end  |

---

## What works well

### Consistent Result pattern

Every function returns `Result<T, String>`. Every test checks `is_ok()`/`is_err()` before unwrapping. No panics, no noise.

```rust
let result = print_to_end(3);
assert!(result.is_ok());
assert_eq!(result.unwrap(), "1\n2\n3\n");
```

### Error tests check both the error exists AND the message

```rust
let result = print_to_end(0);
assert!(result.is_err());
assert_eq!(result.unwrap_err(), "End must be greater than or equal to 1");
```

This is better than just `assert!(result.is_err())` — it proves the error message is correct, not just that an error occurred.

---

## What is missing

### 1. No boundary tests for single-value loops

`print_to_end(1)` should produce `"1\n"`. `print_to_start(1)` should produce `"1\n"`. These are the smallest valid inputs.

```rust
#[test]
fn test_print_to_end_boundary_one() {
    let result = print_to_end(1);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), "1\n");
}
```

### 2. No equal-range tests

`print_range(3, 3)` and `print_sum_range(3, 3)` are valid inputs (start equals end). They are not tested.

```rust
#[test]
fn test_print_range_equal() {
    let result = print_range(3, 3);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), 3);
}
```

### 3. No negative input tests

All invalid tests use `0`. What about `-1`, `-100`? The source checks `end < 1`, so negatives should also fail.

```rust
#[test]
fn test_print_to_end_negative() {
    let result = print_to_end(-5);
    assert!(result.is_err());
}
```

### 4. `print_range` invalid test is incomplete

`test_print_range_invalid` only tests `start > end` (3, 1). But `print_range` also rejects `start < 1` or `end < 1`. Those are covered by `test_print_range_invalid_start` and `test_print_range_invalid_end`, but they test a different error message. The three rejection paths are covered, just split across different test names — that is fine.

---

## Duplication concern

`print_range` and `print_sum_range` have nearly identical validation logic and nearly identical tests. The only difference is return type: `print_range` returns the sum as `Result<i32, String>`, and so does `print_sum_range`. They are the same function with different names.

If this is intentional for learning, it is fine. If not, consider removing one.

---

## Improvement: data-driven tests

The matches module uses arrays of expected values. The loops tests repeat the same three-line pattern 16 times. You could compress:

```rust
#[test]
fn test_print_to_end_all() {
    let cases = [
        (3, Ok("1\n2\n3\n".to_string())),
        (1, Ok("1\n".to_string())),
        (0, Err("End must be greater than or equal to 1".to_string())),
        (-5, Err("End must be greater than or equal to 1".to_string())),
    ];

    for (input, expected) in cases {
        assert_eq!(print_to_end(input), expected, "input: {}", input);
    }
}
```

This cuts 4 tests into 1, and adding a new case is just one line.

---

## Summary of recommendations

| Priority | Action                                    | Effort   |
| -------- | ----------------------------------------- | -------- |
| High     | Add `print_to_end(1)` boundary test       | 1 minute |
| High     | Add `print_to_start(1)` boundary test     | 1 minute |
| Medium   | Add `print_range(3, 3)` equal-range test  | 1 minute |
| Medium   | Add negative input tests                  | 2 minutes|
| Low      | Compress repeated assertions into arrays  | 10 mins  |
| Low      | Decide if `print_range` and `print_sum_range` are both needed | decision |
