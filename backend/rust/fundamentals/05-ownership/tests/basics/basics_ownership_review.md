# Basics Ownership — Test Review & Function Explanations

## Current State

| Metric          | Value         |
| --------------- | ------------- |
| Total tests     | 10            |
| All passing     | yes           |
| Functions tested| 10            |
| Happy path tests| 10            |
| Error path tests| 0             |

---

## Why This Module Matters

Ownership is **the** defining feature of Rust. It's what eliminates use-after-free bugs, data races, and memory leaks at compile time — no garbage collector needed. Every other concept in Rust (lifetimes, borrowing, `Arc`, `Mutex`, channels) builds on these three rules:

1. **Each value has exactly one owner.**
2. **When the owner goes out of scope, the value is dropped.**
3. **You can have either one mutable reference OR any number of immutable references — never both at the same time.**

This module demonstrates all three rules with concrete, testable examples.

---

## Function Explanations

### 1. `demonstrate_move() -> String`

**What it does:** Creates a `String`, assigns it to a second variable (which moves ownership), and returns the second variable.

**Why it matters:** This is the first thing every Rust learner stumbles on. In languages like Python or JavaScript, `b = a` creates a reference — both variables point to the same object. In Rust, `b = a` **moves** ownership for non-`Copy` types. After the assignment, `a` is invalidated. The compiler enforces this — trying to use `a` is a compile error, not a runtime crash.

**Key concept:** `String` owns heap-allocated data (the actual text). When you write `let b = a;`, Rust transfers the pointer, length, and capacity from `a` to `b`. `a` is now uninitialized — it can't be used. This is zero-cost: no data is copied, no reference count is incremented.

**The compile_fail doctest proves the error:**
```rust
let a = String::from("Hello");
let b = a;
println!("{}", a); // Error: value borrowed after move
```

**When you'll hit this:** Any time you pass a `String` to a function, assign it to another variable, or store it in a collection. It's everywhere.

---

### 2. `demonstrate_copy_type() -> (i32, i32)`

**What it does:** Assigns an `i32` to a second variable, then returns both — proving neither was invalidated.

**Why it matters:** This is the contrast to `demonstrate_move()`. `i32` implements the `Copy` trait, which means assignment copies the bits instead of moving ownership. Both `a` and `b` are fully valid after `let b = a;`.

**Key concept:** `Copy` types are stored entirely on the stack — a fixed number of bytes with no heap allocation. Copying is a single CPU instruction. The `Copy` trait is automatically implemented for:
- All integers: `i8` through `i128`, `u8` through `u128`
- Floats: `f32`, `f64`
- `bool`, `char`
- Tuples of `Copy` types: `(i32, bool)`, `(u8, u8, u8)`
- Arrays of `Copy` types: `[i32; 5]`

Types that **cannot** implement `Copy`: `String`, `Vec<T>`, `Box<T>`, `File`, `Mutex<T>`, `RefCell<T>` — anything that owns heap data or manages a resource.

**The w3schools example this mirrors:**
```rust
let a = 5;
let b = a;
println!("a = {}", a);  // Works — i32 is Copy
println!("b = {}", b);  // Works — independent copy
```

**When you'll hit this:** Literally every time you assign an integer, bool, or char. The good news is you don't need to think about it — it just works.

---

### 3. `demonstrate_copy_bool() -> (bool, bool)`

**What it does:** Same pattern as `demonstrate_copy_type()`, but with `bool`.

**Why it matters:** Reinforces that `Copy` isn't just for numbers. `bool` is a single byte — trivially copyable. No move semantics apply.

**Key concept:** `bool` is represented as a single byte (0 or 1). Copying is one byte-to-byte assignment.

---

### 4. `demonstrate_copy_char() -> (char, char)`

**What it does:** Same pattern, but with `char`.

**Why it matters:** `char` is 4 bytes (a Unicode scalar value). Even though it's larger than `i32` or `bool`, it's still a fixed-size stack value — `Copy` applies. This is different from `String`, which is a pointer + length + capacity (24 bytes on 64-bit) that points to heap data.

**Key concept:** `char` is always exactly 4 bytes, regardless of which Unicode character it represents. It stores the code point directly, not a UTF-8 encoding. This is why it's `Copy` — no indirection, no heap.

---

### 5. `string_length(s: &str) -> usize`

**What it does:** Returns the byte length of a string slice without taking ownership.

**Why it matters:** This is the **borrowing** pattern — the alternative to moving. Instead of taking `String` (which would move ownership), it takes `&str` (which borrows). The caller keeps full access to their string after the call.

**Key concept:** `&str` is a "string slice" — a borrowed view into string data. It's two words: a pointer to the first byte and a length. No ownership transfer occurs. The caller's `String` remains valid. This is how you write functions that work with both `String` and string literals (`&'static str`).

**Example:**
```rust
let s = String::from("Hello");
let len = string_length(&s);  // borrows s
println!("{}", s);            // s is still valid!
```

**When you'll hit this:** Every time you write a function that reads but doesn't modify a string. Use `&str` as the parameter type, not `&String` or `String`.

---

### 6. `give_back(s: String) -> String`

**What it does:** Takes ownership of a `String` and returns it unchanged.

**Why it matters:** This demonstrates the "take and return" pattern. The function temporarily owns the value, does something (in this case, nothing), then gives ownership back. The caller gets their value back — it's just wrapped in a different variable.

**Key concept:** This pattern is common in Rust when a function needs to own the value temporarily (e.g., to mutate it, or to pass it to another owning function) but wants to return ownership to the caller. The standard library uses this extensively — `Vec::push` doesn't do this, but `String::to_uppercase` takes `&self` and returns a new `String` (different pattern).

**When you'll hit this:** When you need to temporarily transfer ownership but ultimately want the caller to retain access.

---

### 7. `consume_and_drop(s: String) -> usize`

**What it does:** Takes ownership of a `String`, computes its length, and drops the string — returning only the length.

**Why it matters:** This is the "consume" pattern — the function takes ownership and does **not** return it. The caller loses access to their `String` permanently. This is what happens when you pass a `String` to a function that doesn't return it.

**Key concept:** At the end of the function, `s` goes out of scope and is dropped. The heap memory is freed. The caller only gets back the computed `usize`. This is intentional — if the function doesn't need the string anymore, it shouldn't return it. Returning it would be wasteful (the caller would own a copy they already had).

**When you'll hit this:** Functions that process data and produce a result — parsing, validating, computing. The input is consumed to produce the output.

---

### 8. `demonstrate_scope_drop() -> usize`

**What it does:** Creates a `String` inside a block scope, computes its length, then lets the string drop when the block ends. Returns the computed length.

**Why it matters:** This demonstrates rule #2: **when the owner goes out of scope, the value is dropped**. The `{ }` block creates a new scope. `s` is created inside it, and when the block ends, `s` is destroyed and its memory is freed. But `length` was computed before that happened, so we can still use it.

**Key concept:** Scope in Rust is defined by `{ }`. Every variable is dropped at the end of the scope where it was declared. This is deterministic — you can predict exactly when memory is freed by reading the code. No garbage collector, no reference counting (for local variables). The `Drop` trait runs automatically.

**When you'll hit this:** Every time you use a block, a function, a loop, or a conditional. Scope-based dropping is Rust's memory management foundation.

---

### 9. `demonstrate_mutable_borrow_exclusive() -> String`

**What it does:** Creates a mutable `String`, takes a mutable reference to modify it, then returns the modified string after the borrow ends.

**Why it matters:** This demonstrates rule #3: **mutable borrows are exclusive**. While `r` (`&mut s`) exists, no other reference to `s` can exist — not even an immutable one. The `{ }` block limits the scope of the mutable borrow so `s` can be used afterward.

**Key concept:** `&mut` gives temporary write access. The borrow checker enforces exclusivity: if `&mut s` exists, no `&s` can coexist. This prevents data races at compile time. The block `{ let r = &mut s; ... }` is the standard pattern — limit the borrow's lifetime to the minimum necessary scope.

**When you'll hit this:** Any time you need to modify data through a reference. You'll use `&mut` with explicit scoping to satisfy the borrow checker.

---

### 10. `demonstrate_multiple_immutable_borrows() -> (usize, usize)`

**What it does:** Creates three immutable references to the same `String` and uses all of them simultaneously.

**Why it matters:** This is the other half of rule #3: **multiple immutable borrows are allowed at the same time**. You can have `r1`, `r2`, `r3` all pointing to `s` simultaneously because none of them can modify the data. No data race is possible if nobody can write.

**Key concept:** `&T` is `Copy` — you can create as many immutable references as you want. They all point to the same underlying data, but none can mutate it. This is how Rust achieves the "reader-writer" pattern at the type level: multiple readers OR one writer, never both.

**When you'll hit this:** Passing the same data to multiple functions that only read it, building views into the same data structure, implementing read-only APIs.

---

## The Three Ownership Rules — Demonstrated

| Rule | Demonstrated by |
|------|----------------|
| **Each value has one owner** | `demonstrate_move()` — `b = a` transfers ownership |
| **Owner drops value at scope end** | `demonstrate_scope_drop()` — `s` dies at `}` |
| **One `&mut` OR many `&`** | `mutable_borrow_exclusive()` + `multiple_immutable_borrows()` |

---

## Move vs Copy — The Decision Tree

```
Does the type implement Copy?
├── YES → assignment copies, both variables valid
│   └── i32, f64, bool, char, tuples of Copy types
│
└── NO → assignment moves, original invalidated
    └── String, Vec<T>, Box<T>, File, Mutex<T>, etc.
```

**To force a copy of a non-Copy type:** use `.clone()`.
**To force a move explicitly:** use `std::mem::take()` or `std::mem::replace()`.

---

## Borrow vs Own — The Decision Tree

```
Does your function need to own the value?
├── YES (will store it, return it later, or modify it)
│   └── Take String (or generic T)
│
└── NO (just reading or computing)
    └── Take &str (or &T)
```

---

## Test Coverage Map

| Function | Happy | Edge Cases | Missing |
|----------| :---: | :--------: | ------- |
| `demonstrate_move` | yes | — | `.clone()` alternative |
| `demonstrate_copy_type` | yes | — | `i64`, `f64` variants |
| `demonstrate_copy_bool` | yes | — | — |
| `demonstrate_copy_char` | yes | — | Unicode char |
| `string_length` | yes | empty, emoji | — |
| `give_back` | yes | — | mutation before return |
| `consume_and_drop` | yes | empty string | — |
| `demonstrate_scope_drop` | yes | — | nested scopes |
| `demonstrate_mutable_borrow_exclusive` | yes | — | concurrent immutable + mutable (compile error) |
| `demonstrate_multiple_immutable_borrows` | yes | — | 10+ borrows |

---

## What works well

### The move vs copy contrast is explicit

`demonstrate_move()` and `demonstrate_copy_type()` are placed side by side. The learner sees: "String moves, i32 copies." This is the exact contrast w3schools teaches, and it's the most important concept in Rust ownership.

### The `compile_fail` doctest proves the error

The doc comment on `demonstrate_move()` includes a `compile_fail` block that shows the exact compiler error when you try to use a moved value. This is self-documenting — the test proves the error exists.

### Borrowing patterns cover all three cases

1. `&str` (read-only borrow) — `string_length`
2. `&mut` (exclusive mutable borrow) — `demonstrate_mutable_borrow_exclusive`
3. Multiple `&` (shared read-only borrows) — `demonstrate_multiple_immutable_borrows`

This is the complete set of borrowing patterns a beginner needs.

### Scope drop is isolated

`demonstrate_scope_drop()` uses a `{ }` block to create an explicit scope. The drop happens at the closing `}`, and the test proves the computed value survives. This makes the concept concrete — not abstract theory.

---

## Issues found

### 1. No `.clone()` demonstration

The file mentions clone in the module doc comment but doesn't have a function that demonstrates it. Learners need to see: "If I want to keep both values, I clone." This is the primary escape hatch from move semantics.

**Fix:** Add `demonstrate_clone()` that returns `(String, String)` using `.clone()`.

### 2. No `&String` vs `&str` explanation

`string_length` takes `&str`, but beginners often write `&String`. These are different: `&str` is a string slice (2 words), `&String` is a reference to a `String` (1 word pointing to a String header). `&str` is preferred because it accepts both `String` and string literals.

**Fix:** Add a function that takes `&String` alongside one that takes `&str`, showing why `&str` is more flexible.

### 3. No test for the compile_fail doctest

The `compile_fail` doctest in the doc comment is documentation, not a running test. Rust's doctest runner does verify `compile_fail` blocks, but it's worth noting.

### 4. `string_length` returns byte length, not character count

`string_length("🦀")` returns `4` (bytes), not `1` (character). This is correct for `&str::len()`, but the test should document that this is **byte** length, not character count. The emoji test case covers this, but the function name `string_length` is ambiguous.

**Fix:** Rename to `string_byte_length` or add a doc comment clarifying it returns bytes.

### 5. `give_back` doesn't demonstrate anything interesting

The function takes a `String` and returns it unchanged. While technically correct, it doesn't show the "take and return" pattern with a meaningful operation. A function like `fn give_back(mut s: String) -> String { s.push_str("!"); s }` would be more instructive.

---

## Summary of recommendations

| Priority | Action                                          | Effort   |
| -------- | ----------------------------------------------- | -------- |
| High     | Add `demonstrate_clone()` function and test     | 3 min    |
| High     | Add `&String` vs `&str` comparison              | 3 min    |
| Medium   | Make `give_back` actually mutate the string     | 1 min    |
| Medium   | Rename `string_length` to `string_byte_length`  | 1 min    |
| Low      | Add `i64` and `f64` copy demonstrations         | 2 min    |
| Low      | Add nested scope drop example                   | 2 min    |
