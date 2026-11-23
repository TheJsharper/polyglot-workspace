# Advanced Ownership — Test Review & Function Explanations

## Current State

| Metric          | Value         |
| --------------- | ------------- |
| Total tests     | 19            |
| All passing     | yes           |
| Functions tested| 16            |
| Happy path tests| 19            |
| Error path tests| 0             |

---

## Function Explanations

### 1. `demonstrate_clone() -> (String, String)`

**What it does:** Creates a `String`, clones it, returns both — proving they're independent.

**Why it matters:** `.clone()` is the explicit escape hatch from move semantics. When you need two independent copies of a non-`Copy` type, you clone. This allocates new heap memory — it's not free.

**Key concept:** `clone()` performs a **deep copy** — the new `String` gets its own heap buffer with the same contents. Modifying one doesn't affect the other. This is different from `let b = a` (which moves) and `let b = &a` (which borrows).

**Example:** `let a = String::from("Hi"); let b = a.clone();` → both valid, separate allocations.

---

### 2. `demonstrate_clone_vec() -> (Vec<i32>, Vec<i32>)`

**What it does:** Clones a `Vec`, proving the entire contents are copied recursively.

**Why it matters:** `Vec` cloning copies both the Vec header (pointer, length, capacity) **and** all elements on the heap. For `Vec<i32>`, that's a memcpy of the integers. For `Vec<String>`, each String would also be cloned — potentially many allocations.

**Key concept:** `Vec::clone` is O(n) — it copies every element. Be aware of the cost when cloning large collections.

---

### 3. `reference_string(s: &String) -> usize`

**What it does:** Borrows a `String` immutably, returns its length.

**Why it matters:** Demonstrates that `&String` borrows the `String` struct (the header), not the underlying data. This works, but it's less flexible than `&str`.

**Key concept:** `&String` is a reference **to the String struct** (3 words: pointer, length, capacity). It's one level of indirection more than `&str` (2 words: pointer, length). Prefer `&str` in function parameters.

---

### 4. `reference_str(s: &str) -> usize`

**What it does:** Borrows a string slice, returns its length.

**Why it matters:** `&str` is the **universal** string borrowing type. It accepts `&String` (via deref coercion), `&str` literals, `&Box<str>`, and `&Arc<str>`. This is why you should prefer `&str` over `&String` in function signatures.

**Key concept:** `&str` is a "fat pointer" — two words: a pointer to UTF-8 bytes and a length. It can point into a `String`'s heap buffer, a string literal's static memory, or a `Box<str>`'s allocation.

---

### 5. `mutate_via_reference(s: &mut String)`

**What it does:** Takes a mutable reference and appends text.

**Why it matters:** `&mut` gives exclusive write access. While the mutable borrow exists, no other reference (mutable or immutable) can coexist. This is the borrow checker's core guarantee — no data races.

**Key concept:** `&mut` is **exclusive**. The compiler enforces that only one mutable reference exists at a time. This is why `s` must be declared `mut` at the call site — mutability is part of the binding, not the type.

---

### 6. `mixed_borrowing_pattern() -> (String, usize)`

**What it does:** Mutates via `&mut` in a scoped block, then reads via implicit `&` after the borrow ends.

**Why it matters:** This is the standard pattern for mixing mutable and immutable access to the same data. The mutable borrow is limited to a `{ }` block so it ends before the immutable read.

**Key concept:** The borrow checker tracks borrow lifetimes at the statement level. Once the mutable borrow's scope ends (`}`), the borrow is released and `s` can be used again — both mutably and immutably.

---

### 7. `demonstrate_box() -> Box<i32>`

**What it does:** Creates a `Box<i32>` — a heap-allocated integer with single ownership.

**Why it matters:** `Box<T>` is the simplest smart pointer. It owns heap data and moves like `String`. Unlike `i32` (which is `Copy` and lives on the stack), `Box<i32>` is **not** `Copy` — assignment moves ownership.

**Key concept:** `Box::new(42)` allocates on the heap, stores `42` there, and returns a pointer. The `Box` owns that allocation. When the `Box` is dropped, the heap memory is freed. Use `Box` when you need heap allocation, recursive types, or trait objects.

---

### 8. `demonstrate_box_string() -> Box<String>`

**What it does:** Boxes a `String` — double indirection (Box → String → heap).

**Why it matters:** This shows that `Box` can wrap any type, including types that already own heap data. `Box<String>` has two heap allocations: one for the Box's pointer, one for the String's buffer. This is rarely useful in practice — usually you'd just use `String`.

**Key concept:** `Box<T>` adds one level of indirection. `Box<String>` is: Box pointer → String header (ptr + len + cap) → String's heap buffer.

---

### 9. `unbox_value(b: Box<i32>) -> i32`

**What it does:** Takes ownership of a `Box<i32>`, dereferences it with `*b`, returns the value. The Box is dropped.

**Why it matters:** Dereferencing a `Box` moves the value out (for `Copy` types like `i32`) and drops the Box allocation. For non-Copy types, you'd use `*b` differently — often with pattern matching.

**Key concept:** `*b` on a `Box<i32>` reads the heap value and moves it into the return position. The `Box` is then dropped — its heap slot is freed. The `i32` lives on in the caller's stack frame.

---

### 10. `demonstrate_rc() -> (Rc<String>, Rc<String>)`

**What it does:** Creates an `Rc<String>`, clones the `Rc`, returns both — they share the same heap data.

**Why it matters:** `Rc<T>` (Reference Counted) enables **shared ownership**. Multiple `Rc` pointers can own the same data. The data is freed only when the last `Rc` is dropped. This is how you build graphs, trees with shared nodes, or any structure where one value has multiple owners.

**Key concept:** `Rc::clone()` does **not** deep-copy the data. It increments a reference count and returns a new pointer to the same allocation. This is O(1) — much cheaper than `.clone()` on the underlying type. But `Rc` only works in single-threaded code. For multi-threaded sharing, use `Arc<T>`.

---

### 11. `rc_strong_count(rc: &Rc<String>) -> usize`

**What it does:** Returns the current reference count of an `Rc`.

**Why it matters:** The strong count tells you how many `Rc` owners exist. It's useful for debugging memory leaks — if the count never reaches zero, the data is never freed.

**Key concept:** `Rc::strong_count()` returns the number of `Rc` pointers to the same data. When it reaches 0, the data is dropped. `Rc` also supports "weak" references (`Weak<T>`) that don't increment the count.

---

### 12. `OwnedData` struct

**What it does:** A struct with `name: String` and `values: Vec<i32>` — both owned fields.

**Why it matters:** Structs with owned fields follow the same move semantics as their field types. `OwnedData` moves (doesn't copy) because `String` and `Vec` move. The struct owns all its data — when it's dropped, all fields are dropped.

**Key concept:** The `new` constructor takes `&str` (borrowed) and `Vec<i32>` (owned). `name.to_string()` converts the borrowed `&str` into an owned `String`. `values` is moved directly into the struct — the caller loses access.

---

### 13. `BorrowedData<'a>` struct

**What it does:** A struct with `name: &'a str` and `values: &'a [i32]` — both borrowed fields with lifetime annotation.

**Why it matters:** This is the struct that borrows instead of owns. No allocation — just references. The lifetime `'a` ties the struct's validity to the data it borrows.

**Key concept:** See the Lifetime Annotations section below for a detailed explanation of `&'a str`.

---

### 14-16. Transform Patterns

| Function | Input | Behavior | Caller keeps access? |
|----------|-------|----------|---------------------|
| `transform_consume` | `String` (owned) | Consumes, returns new | No |
| `transform_mutate_in_place` | `mut String` | Mutates same allocation | No (moved in) |
| `transform_borrow` | `&str` (borrowed) | Reads, returns new | Yes |

**Why it matters:** These three patterns cover every way a function can handle its input: consume it, mutate it in place, or borrow it. Choosing the right one affects performance (allocation vs. no allocation) and API ergonomics (caller keeps access or not).

---

### 17. `demonstrate_deref_coercion() -> (usize, usize, usize)`

**What it does:** Calls a `&str` function with `&String`, `&str`, and `&Box<str>` — all work via deref coercion.

**Why it matters:** Deref coercion is why you can pass `&String` to a function that expects `&str`. Rust automatically dereferences `&String` → `&str` so you don't have to write `.as_str()` everywhere.

**Key concept:** `String` implements `Deref<Target = str>`. When the compiler sees `&String` where `&str` is expected, it inserts `&*string` automatically. The same works for `Box<str>` and `Arc<str>`.

---

### 18. `copy_vs_move_summary()`

**What it does:** Creates pairs of every `Copy` type (`i32`, `f64`, `bool`, `char`, tuple) — all remain valid after assignment.

**Why it matters:** This is the definitive registry of `Copy` types. If a type is `Copy`, assignment copies. If not, it moves. This function proves it for all the common cases.

---

## Lifetime Annotations — What `&'a str` Means

When you see this in a struct:

```rust
pub struct BorrowedData<'a> {
    pub name: &'a str,
}
```

It means: **"a string slice that lives at least as long as `'a`."**

Breaking it down:

- `&str` — a borrowed string slice (pointer + length, no ownership)
- `'a` — a **lifetime parameter** — a named scope that tells the compiler how long the reference must be valid
- `&'a str` — a `&str` that is guaranteed to be valid for the duration of `'a`

### Why it's needed in a struct

When a struct holds a reference, the compiler needs to know: **"How long does this reference need to stay alive?"** The `'a` ties the struct's lifetime to the lifetime of the data it borrows.

### Concrete example

```rust
let text = String::from("hello");
let data = BorrowedData { name: &text };
// `data.name` borrows from `text`
// `data` cannot outlive `text` — the compiler enforces this
```

If `text` is dropped while `data` still exists, `data.name` would be a **dangling pointer**. The lifetime `'a` prevents this at compile time.

### The lifetime is usually inferred

You rarely write `'a` explicitly. The compiler infers it:

```rust
fn process(s: &str) -> &str { s }
// Compiler sees: fn process<'a>(s: &'a str) -> &'a str
```

You only need it when the struct **stores** a reference, or when the compiler can't figure out the relationship between input and output lifetimes.

### Owned vs Borrowed structs — the tradeoff

| | `OwnedData` | `BorrowedData<'a>` |
|---|---|---|
| `name` type | `String` | `&'a str` |
| Allocation | Yes (heap) | No (references only) |
| Moves | Yes | Yes (but borrowed data must outlive) |
| Independent of source | Yes | No — tied to source lifetime |
| Can be stored long-term | Yes | Only as long as the source |
| `new()` takes | `&str` (converts) + `Vec` (owns) | `&'a str` + `&'a [i32]` (borrows) |

**Rule of thumb:** Use owned structs for data that needs to live independently. Use borrowed structs for zero-copy views into data you don't own (parsing, temporary processing, serialization).

---

## Test Coverage Map

| Function | Happy | Edge Cases | Missing |
|----------| :---: | :--------: | ------- |
| `demonstrate_clone` | yes | — | empty string, large string |
| `demonstrate_clone_vec` | yes | — | empty vec, nested vec |
| `reference_string` | yes | — | empty string |
| `reference_str` | yes | empty, literal | — |
| `mutate_via_reference` | yes | — | multiple mutations |
| `mixed_borrowing_pattern` | yes | — | nested mutable borrows |
| `demonstrate_box` | yes | — | negative numbers |
| `demonstrate_box_string` | yes | — | empty string |
| `unbox_value` | yes | — | zero |
| `demonstrate_rc` | yes | — | — |
| `rc_strong_count` | yes | 1→2→1 lifecycle | weak references |
| `demonstrate_owned_struct` | yes | — | empty vec |
| `demonstrate_borrowed_struct` | yes | — | dynamic borrowed data |
| `transform_consume` | yes | — | empty string |
| `transform_mutate_in_place` | yes | — | empty string |
| `transform_borrow` | yes | — | empty string, non-ASCII |
| `demonstrate_deref_coercion` | yes | 3 input types | `Arc<str>` |
| `copy_vs_move_summary` | yes | all Copy types | — |

---

## What works well

### Three transform patterns side by side

`transform_consume`, `transform_mutate_in_place`, and `transform_borrow` together show the complete set of ownership transfer strategies. This is the most practical API design lesson in the module.

### `Rc` strong count lifecycle test

`test_rc_strong_count` verifies 1 → 2 → 1 by cloning and dropping. This proves reference counting works as described — not just theory, but observable behavior.

### Deref coercion tests all three input types

`&String`, `&str`, and `&Box<str>` all produce the same result. This proves the coercion mechanism works across the full set of string-like smart pointers.

### Owned vs Borrowed struct contrast

Having both `OwnedData` and `BorrowedData<'a>` in the same module makes the tradeoff concrete: allocation vs. zero-copy, independence vs. lifetime coupling.

---

## Issues found

### 1. No `Arc<T>` demonstration

`Rc<T>` is single-threaded. The multi-threaded equivalent is `Arc<T>` (Atomic Reference Counted). This is a critical gap — most real-world shared ownership needs `Arc`, not `Rc`.

### 2. No `Weak<T>` demonstration

`Rc::downgrade()` creates a `Weak` reference that doesn't increment the count. This is essential for avoiding reference cycles (parent→child→parent graphs).

### 3. No `Box` with non-Copy type

All `Box` examples use `i32` (Copy). A `Box<String>` or `Box<Vec<i32>>` would show how dereferencing works with non-Copy types — you get a reference, not a moved value.

### 4. `BorrowedData` test uses static data only

`demonstrate_borrowed_struct` returns a struct borrowing from `&[1, 2, 3]` (a static slice). It doesn't show the lifetime constraint with dynamically-scoped data — which is where lifetime errors actually occur.

### 5. No `RefCell<T>` demonstration

`RefCell<T>` provides interior mutability — mutable access through an immutable reference. Combined with `Rc`, it enables the `Rc<RefCell<T>>` pattern for shared mutable state.

---

## Summary of recommendations

| Priority | Action                                          | Effort   |
| -------- | ----------------------------------------------- | -------- |
| High     | Add `Arc<T>` demo and test                      | 5 min    |
| High     | Add `Weak<T>` cycle-breaking demo               | 5 min    |
| Medium   | Add `Box<String>` deref test                    | 2 min    |
| Medium   | Show dynamic borrowed data with explicit scope  | 3 min    |
| Low      | Add `RefCell<T>` interior mutability demo       | 5 min    |
| Low      | Test `clone` with empty string and large string | 2 min    |
