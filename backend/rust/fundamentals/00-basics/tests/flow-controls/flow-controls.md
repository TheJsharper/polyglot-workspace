# Flow Control — Comparison Operators

## What are they?

Comparison operators evaluate two values and return a **`bool`** (`true` or `false`). They're called "flow control" because their results drive conditional logic: `if`, `while`, `match`, etc.

## The six operators

| Operator | Meaning | Example | Result |
|----------|---------|---------|--------|
| `<` | Less than | `10 < 11` | `true` |
| `>` | Greater than | `15 > 12` | `true` |
| `<=` | Less than or equal | `7 <= 7` | `true` |
| `>=` | Greater than or equal | `10 >= 9` | `true` |
| `==` | Equal to | `5 == 5` | `true` |
| `!=` | Not equal to | `3 != 4` | `true` |

## Key points

1. **They return `bool`**, not the compared values. `10 < 11` is `true`, not `10`.
2. **`=` vs `==`**: single `=` assigns, double `==` compares. This is one of the most common beginner mistakes.
3. **`<=` and `>=` include equality**: `7 <= 7` is `true` because 7 equals 7.
4. **Type matters**: you can't compare different types directly. `5 == 5.0` won't compile — one is `i32`, the other is `f64`. Cast first: `5 as f64 == 5.0`.

## How they control flow

```rust
let age = 18;
if age >= 18 {
    println!("Adult"); // runs because 18 >= 18 is true
}
```

The `if` keyword needs a `bool` expression — comparison operators are the most common way to produce one.

---

## Review Notes

### Semicolons destroy return values
A semicolon turns an expression into a statement and discards its value, returning `()` instead.

```rust
// WRONG — semicolons discard the String, function returns ()
fn bad() -> String {
    if true {
        format!("hi");   // ← semicolon = throws away String, returns ()
    } else {
        format!("bye");  // ← same problem
    }
}

// RIGHT — no semicolon, the expression IS the return value
fn good() -> String {
    if true {
        format!("hi")    // ← no semicolon = this flows out as the return value
    } else {
        format!("bye")
    }
}
```

**Rule:** last expression in a block = return value. Semicolon = "I'm throwing this away."

### String interpolation only works in macros
`"{a}"` in a plain string literal is literal text `"{a}"`. It only interpolates inside `format!()`, `println!()`, etc.

```rust
let a = 7;
"{a}"          // → literal "{a}"
format!("{a}") // → "7"
```

### `&str` vs `String` for return types
Prefer `&'static str` when returning string literals. No heap allocation, no copying.

```rust
// Preferred — &str, zero allocation
fn greeting(time: i16) -> &'static str {
    if time < 18 { "Good Day" } else { "Good evening" }
}

// Only use String when you need ownership or modification
fn greeting_owned(time: i16) -> String {
    if time < 18 { String::from("Good Day") } else { String::from("Good evening") }
}
```

### Rust has no `void`
Every function returns something. No explicit return type means `-> ()` (the unit type).

```rust
fn print_msg() { println!("hi"); }       // returns ()
fn print_msg() -> () { println!("hi"); } // same thing, explicit
```
