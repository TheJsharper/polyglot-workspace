# Constants Module — Review Notes

## Q&A from Code Review

### 1. What is `Option<T>`?
**Answer:** Rust's nullable type. A value can be `Some(value)` or `None`. Unlike Java/TS where `null` causes NullPointerExceptions, Rust forces you to handle both cases with `match`. You can't accidentally use a `None` value — the compiler won't allow it.

```rust
let maybe_number: Option<i32> = Some(5);
match maybe_number {
    Some(n) => println!("Got: {}", n),
    None => println!("Nothing here"),
}
```

### 2. What is `Result<T, E>`?
**Answer:** Represents success (`Ok(value)`) or failure (`Err(error)`). Used everywhere in Rust for error handling instead of exceptions. No `try/catch` — you pattern match.

```rust
let success: Result<i32, &str> = Ok(42);
let failure: Result<i32, &str> = Err("Something went wrong");
```

### 3. What is `#[...]` syntax?
**Answer:** Attributes — like Java annotations or TypeScript decorators, but processed at compile time. They add metadata or change code generation.

| Attribute | Purpose |
|-----------|---------|
| `#[test]` | Marks function as a test |
| `#[derive(Debug)]` | Auto-generates debug printing |
| `#[allow(dead_code)]` | Suppresses unused warnings |
| `#[path = "..."]` | Points to a file for a module |

### 4. What does `mod` mean?
**Answer:** Declares a module. Tells Rust "there's code in another file I want to use." Like Java packages or TS `import`, but Rust's module system is explicit:
- `mod constants;` → declares the module structure
- `use constants::MY_NUMBER;` → brings items into scope

### 5. Why `#[path = "../src/constants.rs"]` instead of `lib.rs`?
**Answer:** The `#[path]` attribute works for integration tests but isn't idiomatic. Standard pattern:
```
src/
  lib.rs          ← pub mod constants;
  constants.rs    ← your constants
```
Then tests import via crate name: `use primitive_types::constants::MY_NUMBER;`

### 6. Why epsilon comparison for floats?
**Answer:** Floating point numbers have precision issues. `5.99` might be stored as `5.9900000000000002`. Using epsilon checks if values are "close enough":
```rust
assert!((MY_DOUBLE - 5.99).abs() < f64::EPSILON);
```

### 7. Why `assert!(!MY_BOOL)` for false?
**Answer:** `assert!()` passes when the expression is `true`. To assert a value is `false`, negate it with `!`:
```rust
assert!(!MY_BOOL);  // Passes if MY_BOOL is false
```

---

## Test Structure Pattern

```rust
// Value assertions — does the constant have the right value?
#[test]
fn test_constants() {
    assert_eq!(MY_NUMBER, 5);
    // ...
}

// Type assertions — does the constant have the right type?
#[test]
fn test_constants_values() {
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_NUMBER)
    );
    // ...
}
```

---

*Last reviewed: 2026-11-10*
