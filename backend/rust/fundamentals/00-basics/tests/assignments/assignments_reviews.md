# Assignments Module — Review Notes

## Q&A from Code Review

### 1. What are assignment operators in Rust?
**Answer:** Rust supports compound assignment operators that modify a variable in place. Since we use `const` here, we demonstrate the expressions that would be used with `let mut`:

| Operator | Example | Meaning |
|----------|---------|---------|
| `+=` | `x += 3` | `x = x + 3` |
| `-=` | `x -= 2` | `x = x - 2` |
| `*=` | `x *= 2` | `x = x * 2` |
| `/=` | `x /= 2` | `x = x / 2` |
| `%=` | `x %= 2` | `x = x % 2` |

### 2. Why use `const` instead of `let mut` for assignments?
**Answer:** In a test module, we're verifying the **expressions**, not mutation. The constants show what the result would be after assignment operations. For actual mutation in real code:
```rust
let mut x = 5;
x += 3;  // x is now 8
```

### 3. Why does `5 / 2 = 2` and not `2.5`?
**Answer:** Integer division truncates toward zero. `5` and `2` are `i32`, so the result is `i32`. For floating-point division, use `5.0 / 2.0` or cast: `(5 as f64) / 2.0`.

### 4. What is `use assignments::*;`?
**Answer:** Wildcard import — brings all public items from the `assignments` module into scope. Convenient for tests, but in production code prefer explicit imports (`use assignments::{ITEM1, ITEM2};`) for clarity.

---

## Test Structure

```rust
#[test]
fn test_assignments() {
    // Value assertions
    assert_eq!(MY_ASSIGNMENT_NUMBER, 5);
    assert_eq!(MY_ASSIGNMENT_INCREMENT, MY_ASSIGNMENT_NUMBER + 3);
    // ... compound operations verified against base value
}
```

---

*Last reviewed: 2026-11-10*
