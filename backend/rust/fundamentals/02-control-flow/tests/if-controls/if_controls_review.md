# If-Controls & Matches — Review & Recommendations

## Current State

| Module     | Tests | Coverage                          | Status |
| ---------- | ----- | --------------------------------- | ------ |
| if-controls| 12    | Every function has a smoke test   | PASS   |
| matches    | 14    | Valid + invalid paths, boundaries | PASS   |

---

## Source Code — if-controls.rs

### CRITICAL: `if x { true } else { false }` anti-pattern

Every boolean function repeats the same structure:

```rust
// Current — verbose, unidiomatic
pub fn is_even(n: i32) -> bool {
    if n % 2 == 0 {
        true
    } else {
        false
    }
}
```

**Idiomatic Rust** — just return the expression:

```rust
pub fn is_even(n: i32) -> bool {
    n % 2 == 0
}
```

This applies to ALL boolean functions: `is_odd`, `is_positive`, `is_negative`, `is_zero`, `greater_than`, `less_than`, `greater_than_or_equal`, `less_than_or_equal`, `equal`.

### CRITICAL: Wrapper functions that add nothing

`greater_than(a, b)` is literally `a > b`. `equal(a, b)` is `a == b`. These functions exist in the stdlib already. They're fine for learning, but recognize that in real code you'd just write `a > b`.

### Boundary gaps in tests

| Function        | Missing case          |
| --------------- | --------------------- |
| `is_zero`       | negative zero (irrelevant for i32, but worth noting) |
| `is_positive`   | zero — `is_positive(0)` should be `false` |
| `is_negative`   | zero — `is_negative(0)` should be `false` |
| `greater_than`  | equal inputs `greater_than(5, 5)` |
| `less_than`     | equal inputs `less_than(5, 5)` |
| `is_even`       | zero, negative even `-4` |
| `is_odd`        | zero, negative odd `-3` |

### `greater` edge case

```rust
assert_eq!(greater(2, 3), 3);  // equal returns second argument
```

`greater(3, 3)` returns `3` (the else branch). That's correct but undocumented. Consider testing equal inputs explicitly.

---

## Source Code — matches.rs

### Good: Three variants with different return types

`get_day_static` (`&'static str`), `get_day_dynamic` (`String`), `get_day` (guarded `String`). This is a great learning setup to see the tradeoffs.

### Good: Early validation guard in `get_day`

```rust
if day > 7 || day == 0 {
    return "Invalid day".to_string();
}
```

Clean separation of error path from happy path.

### Minor: unreachable `_` arm

After the guard, `day` is guaranteed to be 1-7. The `_ => "Invalid day"` arm in `get_day` is dead code. Rust won't warn because `u32` has more values, but logically it's unreachable.

---

## Test Quality — if-controls

### What's good

- Each function has at least one happy test
- Tests are small and focused — one concern per test

### What's missing

1. **Zero as boundary** — `is_positive(0)` and `is_negative(0)` are not tested
2. **Negative numbers** — `is_even(-4)`, `is_odd(-3)`, `greater(-1, -5)` are not tested
3. **Equal inputs** — `greater_than(5, 5)`, `less_than(5, 5)` are not tested
4. **No message on assertions** — `assert_eq!(is_even(2), true)` should be `assert!(is_even(2), "2 should be even")` for clearer failure messages

### Pattern: `assert_eq!(fn(), true)` vs `assert!()`

```rust
// Verbose, leaks implementation
assert_eq!(is_zero(0), true);
assert_eq!(is_zero(1), false);

// Cleaner
assert!(is_zero(0));
assert!(!is_zero(1));
```

---

## Test Quality — matches

### What's good

- Data-driven tests with `for` loops (DRY, easy to extend)
- Boundary tests (day 7 valid, day 8 invalid)
- Edge case: `u32::MAX`
- Separate tests for valid vs invalid paths

### What's good practice to copy

The pattern of grouping expected values in an array and iterating is **exactly** what you should keep doing. It makes adding new cases trivial.

---

## Recommendations — Priority Order

### 1. Refactor boolean functions (high impact, easy)

Replace every `if x { true } else { false }` with just `x`. This is the single biggest improvement you can make.

### 2. Add boundary tests (medium impact)

- `is_positive(0)` → `false`
- `is_negative(0)` → `false`
- `is_even(-4)` → `true`
- `is_odd(-3)` → `true`
- `greater(5, 5)` → `5`

### 3. Use `assert!()` for booleans (low impact, style)

`assert!(is_zero(0))` instead of `assert_eq!(is_zero(0), true)`.

### 4. Consider a lib.rs structure (architectural)

Right now the crate is a binary (`main.rs`). Integration tests load source via `#[path]`. For a real project, use `src/lib.rs` so tests link against the library naturally without `#[path]` hacks.

---

## What to Pay Attention To

- **Don't over-wrap operators** — `greater_than(a, b)` exists to learn `if`, not to ship. In production code, write `a > b`.
- **Test boundaries, not just centers** — zero, negatives, equal inputs, min/max. You're already doing this in matches; carry it to if-controls.
- **Data-driven tests scale** — the array-of-tuples pattern in matches is how real Rust tests look. Keep doing that.
- **Return the expression** — Rust is an expression language. `if condition { value }` already evaluates to a value. No need for explicit `return` or `true`/`else`/`false`.
