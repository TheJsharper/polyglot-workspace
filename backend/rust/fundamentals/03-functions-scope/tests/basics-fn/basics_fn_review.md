# Basics Fn — Test Review

## Current State

| Metric          | Value         |
| --------------- | ------------- |
| Total tests     | 14            |
| All passing     | yes           |
| Functions tested| 12            |
| Happy path tests| 14            |
| Error path tests| 0             |

---

## Test Coverage Map

| Function                                          | Tested | Notes                                  |
| ------------------------------------------------- | :----: | -------------------------------------- |
| `demonstrate_basic_function`                      | yes    | constant return 42                     |
| `demonstrate_basic_function_with_argument`        | yes    | identity, single input                 |
| `demonstrate_basic_function_with_multiple_arguments` | yes | addition, two inputs                |
| `demonstrate_basic_function_with_return_type_annotation` | yes | explicit return, single input     |
| `demonstrate_basic_function_with_expression`      | yes    | tail expression return                 |
| `demonstrate_basic_function_with_default_argument`| yes    | no defaults in Rust, tested as add     |
| `demonstrate_basic_function_with_optional_argument`| yes   | both Some and None cases               |
| `demonstrate_basic_function_with_variadic_arguments` | yes  | slice-based variadic pattern           |
| `demonstrate_basic_function_with_closure_argument`| yes    | Fn trait bound                         |
| `demonstrate_basic_function_with_generic_argument`| yes    | generic tuple return                   |
| `demonstrate_basic_function_with_generic_return_type` | yes | identity generic                    |
| `demonstrate_basic_function_with_generic_closure_argument` | yes | generic closure                  |
| `demonstrate_basic_function_with_generic_variadic_arguments` | yes | generic vec builder             |

---

## What works well

### Every function is tested

All 12 public functions in the source have at least one test. Nothing is left uncovered.

### Optional argument tests both branches

`test_demonstrate_basic_function_with_optional_argument_some` and `_none` cover both `Some(4)` and `None`. This is correct — `Option<T>` always needs both paths tested.

### Closure tests use inline lambdas

The closure tests pass `|x| x * 2` and `|x| x + 10` directly. This is the idiomatic way to test `Fn`-bound parameters — no need for named closure structs.

### Generic variadic test destructures the tuple

The test correctly unpacks `(first, rest)` and asserts both parts separately. This catches bugs where the first element or the vector could be wrong independently.

---

## Issues found

### 1. No error path tests

Every test is a happy path. The source functions don't currently return `Result` or handle invalid input, so there are no error paths to test. But this means the tests don't verify behavior under bad input either. If these functions later gain validation, there will be no tests for it.

### 2. `default_argument` test is misleading

The function `demonstrate_basic_function_with_default_argument(x, y)` does not actually have default values — Rust does not support default parameters. It just adds two numbers. The test name implies defaults exist, but the function signature is `(x: i32, y: i32)` with no defaults. Either rename the function or add actual defaults via the builder pattern or `Option`.

### 3. Single input per function

Most tests use only one input combination. For example:
- `with_argument` only tests `5`
- `with_multiple_arguments` only tests `(3, 4)`
- `with_variadic_arguments` only tests `(1, &[2, 3, 4])`

You should also test:
- Zero values (`0`, `&[]`)
- Negative values (`-1`)
- Empty slices for variadic functions

### 4. No test for `generic_argument` with different types

The test only uses `i32`. The function is generic over `T`, so it should also be tested with at least one non-numeric type like `&str` or `bool` to prove the generic actually works.

### 5. `generic_variadic_arguments` asserts `rest` contains `first`

The function pushes `x` into the vector then extends with `ys`, so the returned vector is `[x, ...ys]`. The test correctly checks `rest == vec![1, 2, 3]`. But note: the function returns `(x, vec![x, ...ys])`, meaning `x` appears twice — once as the first tuple element and once inside the vector. This may be intentional or a bug in the source.

---

## Pattern observations

### Consistent assert_eq! style

All tests use the compact `assert_eq!(actual, expected)` form. No verbose `is_ok() + unwrap()` chains. This is the preferred pattern — failure messages are clean and direct.

### Good use of `use basics_fn::*`

The glob import at the top avoids repeating the module prefix in every test function. This is fine for test modules where namespace pollution is not a concern.

---

## Summary of recommendations

| Priority | Action                                          | Effort   |
| -------- | ----------------------------------------------- | -------- |
| High     | Test zero, negative, and empty inputs           | 10 min   |
| High     | Add `&str` test for generic functions           | 2 min    |
| Medium   | Clarify `default_argument` naming or impl       | decision |
| Medium   | Verify `generic_variadic` double-inclusion intent| 1 min   |
| Low      | Add edge case tests (empty slice, single item)  | 5 min    |
