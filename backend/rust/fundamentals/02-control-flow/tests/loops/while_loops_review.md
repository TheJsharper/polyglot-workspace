# While Loops — Test Review

## Current State

| Metric          | Value         |
| --------------- | ------------- |
| Total tests     | 18            |
| All passing     | yes           |
| Functions tested| 9             |
| Error path tests| 9             |
| Happy path tests| 9             |

---

## Test Coverage Map

| Function          | Happy | Invalid Input | Boundary | Missing                    |
| ----------------- | :---: | :-----------: | :------: | -------------------------- |
| `print_sum_to_end`| yes   | yes (0)       | no       | input 1, negative, large   |
| `count_to_n`      | yes   | yes (0)       | no       | input 1, negative          |
| `sum_to_n`        | yes   | yes (0)       | no       | input 1, negative          |
| `factorial`       | yes   | yes (0)       | no       | input 1, negative, overflow|
| `fibonacci`       | yes   | yes (0)       | no       | input 1, input 2           |
| `is_prime`        | yes   | yes (1)       | no       | composite, small primes    |
| `gcd`             | yes   | yes (0,18)    | no       | both zero, equal inputs    |
| `lcm`             | yes   | yes (0,18)    | no       | both zero, equal inputs    |
| `print_sum_to_end`| yes(5)| yes (0)       | no       | duplicate of test_while_loops |

---

## What works well

### Consistent Result pattern across all functions

Every function returns `Result<T, String>`. Every test checks `is_ok()` then unwraps. No panics, no backtrace noise. This is the correct pattern for expected errors.

### Each function has both happy and invalid test

No function is left untested. The paired structure (happy + invalid) makes it easy to see what is covered at a glance.

### `is_prime` uses correct validation threshold

`is_prime` rejects inputs below 2, not 1. The test correctly tests `1` as invalid. This shows understanding that 1 is not prime.

### `gcd` and `lcm` test both parameters

`gcd(0, 18)` and `lcm(0, 18)` test the first parameter. The source validates both `a` and `b`.

---

## Issues found

### 1. Duplicate: `test_while_loops` and `test_print_sum_to_end`

Both test `print_sum_to_end`. They use different inputs (3 vs 5) but are testing the same function. This is not wrong, but the test names are inconsistent — `test_while_loops` does not tell you what it tests. Rename it to something like `test_print_sum_to_end_small_input`.

### 2. No boundary test for input 1

Every function that accepts `n >= 1` is only tested with `n = 0` (invalid) and some middle value (3, 5, 6). The boundary at `n = 1` is never tested. This is the smallest valid input and the most likely place for off-by-one errors.

### 3. `is_prime` only tests one prime (5)

Testing only `5` does not prove the algorithm works. You should also test:
- A composite number like `4` or `6` (should return `false`)
- A small prime like `2` (edge case, smallest prime)
- A larger prime like `17`

### 4. `gcd` and `lcm` only test one parameter as zero

`gcd(0, 18)` and `lcm(0, 18)` test the first parameter. The second parameter being zero is also invalid but not tested. Also, `gcd(0, 0)` is a meaningful edge case.

### 5. No negative input tests

Every invalid test uses `0`. The source checks `< 1`, so negatives should also fail. Not testing them means the error path works by accident, not by design.

### 6. `factorial` has no overflow awareness

`factorial(13)` overflows `i32`. The function returns `i32`. If a caller passes `20`, the result will be wrong, not an error. This is not a test bug, but it is a design gap worth knowing about.

### 7. `fibonacci(6) = 8` needs verification

The test asserts `fibonacci(6) == 8`. The sequence is 0, 1, 1, 2, 3, 5, 8... so `fibonacci(6)` is the 6th iteration returning `b = 8`. This is correct for this implementation, but the function name suggests it returns the nth Fibonacci number. Make sure you understand which indexing convention this uses (0-based or 1-based).

---

## Pattern to fix

The repeated three-line pattern in every test:

```rust
let result = print_sum_to_end(3);
assert!(result.is_ok());
assert_eq!(result.unwrap(), 6);
```

Can be compressed:

```rust
assert_eq!(print_sum_to_end(3), Ok(6));
```

This is shorter, does the same two checks, and the failure message tells you exactly what went wrong.

For error cases:

```rust
assert_eq!(print_sum_to_end(0), Err("Input must be greater than or equal to 1".to_string()));
```

---

## Summary of recommendations

| Priority | Action                                        | Effort   |
| -------- | --------------------------------------------- | -------- |
| High     | Add `n = 1` boundary tests for every function | 5 minutes|
| High     | Add `is_prime` tests for composite and 2      | 2 minutes|
| Medium   | Add negative input tests                      | 2 minutes|
| Medium   | Test `gcd` and `lcm` with second param zero   | 1 minute |
| Medium   | Rename `test_while_loops` to descriptive name | 30 sec   |
| Low      | Compress `is_ok() + unwrap()` to `Ok(x)`      | 5 minutes|
| Low      | Consider `factorial` overflow behavior        | decision |
