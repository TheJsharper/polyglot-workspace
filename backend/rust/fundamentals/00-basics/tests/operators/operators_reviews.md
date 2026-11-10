# Operators Module — Review Notes

## Q&A from Code Review

### 1. Why use subdirectories for tests?
**Answer:** Keeps tests organized by topic. Cargo only discovers test files directly in `tests/`, so we use a dispatcher pattern:
```
tests/
  operators/
    operators_test.rs     ← actual test code
  operators_test.rs       ← dispatcher that includes the module
```

### 2. What's the dispatcher pattern?
**Answer:** A one-line file in `tests/` that includes the real test from a subdirectory:
```rust
#[path = "./operators/operators_test.rs"]
mod operators_module;
```
Cargo sees `operators_test.rs` in `tests/`, runs it, and it pulls in the actual test code.

### 3. Arithmetic operators covered
| Operator | Example | Result |
|----------|---------|--------|
| `+` | `5 + 3` | 8 |
| `-` | `10 - 4` | 6 |
| `*` | `6 * 7` | 42 |
| `/` | `20 / 5` | 4 |
| `%` | `20 % 3` | 2 (remainder) |

### 4. Why no type assertions for operators?
**Answer:** Operator results are compile-time constants with explicit type annotations (`i32`). The type is guaranteed by the declaration. Value assertions are sufficient.

---

*Last reviewed: 2026-11-10*
