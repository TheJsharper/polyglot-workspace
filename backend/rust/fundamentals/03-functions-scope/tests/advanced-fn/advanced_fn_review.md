# Advanced Fn — Test Review & Function Explanations

## Current State

| Metric          | Value         |
| --------------- | ------------- |
| Total tests     | 13            |
| All passing     | yes           |
| Functions tested| 13            |
| Happy path tests| 13            |
| Error path tests| 0             |

---

## Function Explanations

### 1. `apply_closure<T, F>(value: T, f: F) -> T`

**What it does:** Takes a value and a closure, applies the closure to the value, returns the result.

**Why it matters:** This is the simplest higher-order function. It demonstrates `FnOnce` — the closure is consumed when called, meaning it can only be invoked once. The closure may take ownership of captured variables.

**Key concept:** `FnOnce` is the most permissive trait bound. Every closure implements `FnOnce`, but not every closure implements `Fn` or `FnMut`. If a closure moves captured values (like a `String`), it can only be `FnOnce`.

**Example:** `apply_closure(5, |x| x * 2)` → `10`

**When to use:** When you want to accept any callable and only need to invoke it once. This is how `Option::map` and `Iterator::map` work internally.

---

### 2. `map_slice<F, T, U>(slice: &[T], f: F) -> Vec<U>`

**What it does:** Transforms each element of a slice using a closure, collecting results into a new `Vec`.

**Why it matters:** This is the fundamental transformation pattern. The input and output types can differ (`T` → `U`), and the closure borrows each element (`&T`) rather than consuming it.

**Key concept:** `slice.iter()` produces `&T` items. The closure `Fn(&T) -> U` receives a reference, so the original slice is untouched. The `.collect()` call relies on type inference to know it should produce `Vec<U>`.

**Example:** `map_slice(&[1, 2, 3], |x| x * 2)` → `vec![2, 4, 6]`

**When to use:** Converting data between types — e.g., `&str` to `String`, raw data to formatted output, model to view model.

---

### 3. `filter_slice<F, T>(slice: &[T], predicate: F) -> Vec<&T>`

**What it does:** Keeps only elements where the predicate returns `true`, returning references to the originals.

**Why it matters:** Filtering doesn't transform — it selects. The return type `Vec<&T>` means no copying occurs; you get references back to the original slice. The wrapper closure `|item| predicate(*item)` bridges the gap between `filter`'s `&&T` input and the user-facing `&T` predicate.

**Key concept:** Iterator adapters chain together. `filter` receives `&&T` (a reference to the iterator's `&T` item), so we dereference once to give the user a clean `&T` predicate. This is a common pattern when building ergonomic wrappers.

**Example:** `filter_slice(&[1, 2, 3, 4], |x| *x % 2 == 0)` → `vec![&2, &4]`

**When to use:** Subsetting data — finding even numbers, active users, valid entries, passing grades.

---

### 4. `fold_slice<F, T, B>(slice: &[T], init: B, f: F) -> B`

**What it does:** Reduces a slice to a single value by repeatedly applying a closure with an accumulator.

**Why it matters:** `fold` is the most general iterator consumer. `map`, `filter`, `sum`, `product` — they can all be expressed as `fold`. The accumulator type `B` can differ from the element type `T`.

**Key concept:** The closure signature `Fn(B, &T) -> B` takes the current accumulator and a reference to the next element, returning the new accumulator. The `init` value seeds the process. Each iteration replaces the accumulator entirely — ownership moves through the chain.

**Example:** `fold_slice(&[1, 2, 3, 4], 0, |acc, x| acc + x)` → `10` (sum)
**Example:** `fold_slice(&[1, 2, 3, 4], 1, |acc, x| acc * x)` → `24` (product)

**When to use:** Accumulating results — sum, product, building a string, computing statistics, finding max/min with custom logic.

---

### 5. `make_offset_adder(offset: i32) -> impl Fn(i32) -> i32`

**What it does:** Returns a closure that adds a captured `offset` to any input.

**Why it matters:** This demonstrates **closure factories** — functions that return closures. The `move` keyword forces the closure to take ownership of `offset`. The return type `impl Trait` hides the concrete closure type (which is anonymous and unnameable).

**Key concept:** `move |x| x + offset` captures `offset` by value. Without `move`, the closure would borrow `offset`, and returning it would be a borrow of a local variable (compile error). `move` makes the closure self-contained and safe to return.

**Example:** `let add_ten = make_offset_adder(10); add_ten(5)` → `15`

**When to use:** Creating parameterized callbacks — event handlers with pre-configured data, validators with configurable thresholds, formatters with preset options.

---

### 6. `make_counter() -> impl FnMut() -> u32`

**What it does:** Returns a closure that tracks how many times it has been called.

**Why it matters:** This demonstrates `FnMut` — closures that mutate captured state. The `mut count` variable is modified on each call. Unlike `FnOnce`, an `FnMut` closure can be called multiple times, but it requires `&mut self` internally.

**Key concept:** The `move` keyword captures `count` by value into the closure. Each call increments it. Because `count` is mutated, this closure implements `FnMut` but **not** `Fn` (which requires no mutation). The caller must declare the counter as `mut` to call it: `let mut counter = make_counter(); counter()`.

**Example:** `let mut c = make_counter(); c(); c(); c()` → `1, 2, 3`

**When to use:** Stateful callbacks — request counters, unique ID generators, rate limiters, animation frames, game tick counters.

---

### 7. `compose<F, G, T>(f: F, g: G) -> impl Fn(T) -> T`

**What it does:** Combines two functions into one that applies `g` first, then `f` — mathematical composition `f(g(x))`.

**Why it matters:** Function composition is a foundational concept in functional programming. Instead of `f(g(x))`, you get a reusable `composed(x)`. The `T: Clone` bound exists because `g` consumes `x` (taking ownership), but `f` also needs a `T` — we clone to satisfy both.

**Key concept:** The returned closure is `move |x| f(g(x.clone()))`. It captures both `f` and `g` by move. The `clone()` on `x` is necessary because `g(x)` consumes the original. Without `Clone`, you could only compose functions that borrow their input.

**Example:** `compose(|x| x + 1, |x| x * 2)(3)` → `7` (3×2=6, 6+1=7)

**When to use:** Building processing pipelines — parse then validate, transform then format, encrypt then encode.

---

### 8. `chain_apply<T, F>(value: T, functions: &[F]) -> T`

**What it does:** Applies a slice of functions to a value in sequence, threading the result through each.

**Why it matters:** This generalizes composition to a dynamic number of functions. While `compose` works with exactly two functions, `chain_apply` handles any number at runtime. It uses `fold` to thread the accumulator through each function.

**Key concept:** `functions.iter().fold(value, |acc, f| f(acc))` — the initial value flows through each function, with each output becoming the next input. All functions must have the same `T -> T` signature, meaning the type is preserved through the entire chain.

**Example:** `chain_apply(1, &[double, add_one, triple])` → `9` ((1×2)+1=3, 3×3=9)

**When to use:** Plugin architectures, middleware chains, data transformation pipelines where the number of steps is determined at runtime.

---

### 9. `curried_add(x: i32) -> impl Fn(i32) -> i32`

**What it does:** Takes the first argument and returns a function waiting for the second — curried addition.

**Why it matters:** **Currying** transforms a multi-argument function into a chain of single-argument functions. Instead of `add(x, y)`, you get `add(x)(y)`. This enables partial application — you can create specialized functions like `add_five = curried_add(5)`.

**Key concept:** The outer function captures `x` in the returned closure via `move`. The closure then only needs `y`. This is currying in its purest form — each function takes exactly one argument.

**Example:** `let add_five = curried_add(5); add_five(3)` → `8`
**Example:** `curried_add(10)(20)` → `30`

**When to use:** Creating specialized functions from general ones — adding tax to prices, applying a fixed discount, prepending a common prefix.

---

### 10. `partial_apply<F, T, U, R>(f: F, first: T) -> impl Fn(U) -> R`

**What it does:** Pre-fills the first argument of any two-argument function, returning a function that only needs the second.

**Why it matters:** This is the **generalized** version of currying. While `curried_add` is specific to addition, `partial_apply` works with any `Fn(T, U) -> R`. You give it a function and the first argument; it gives you back a function that only needs the second.

**Key concept:** The `T: Clone` bound lets us store `first` in the closure and clone it on each call. Without `Clone`, the closure would consume `first` on the first invocation, making it `FnOnce` instead of `Fn`. The returned closure captures both `f` and `first` by move.

**Example:** `let double = partial_apply(|a, b| a * b, 2); double(5)` → `10`

**When to use:** Any time you have a general function and want to create a specialized version — multiplying by a fixed factor, comparing against a threshold, logging with a fixed prefix.

---

### 11. `all_match<T, F>(slice: &[T], predicate: F) -> bool`

**What it does:** Returns `true` if **every** element satisfies the predicate.

**Why it matters:** This is the **universal quantifier** (∀) from logic. It short-circuits — stops at the first `false`. The predicate receives `&T` directly because `Iterator::all` takes `FnMut(Self::Item)` where `Self::Item` is `&T`.

**Key concept:** `all` is lazy. If any element fails the predicate, iteration stops immediately. An empty slice always returns `true` (vacuous truth — there are no counterexamples).

**Example:** `all_match(&[2, 4, 6], |x| *x % 2 == 0)` → `true`
**Example:** `all_match(&[2, 3, 6], |x| *x % 2 == 0)` → `false`

**When to use:** Validation — checking all inputs are valid, all permissions granted, all fields filled, all tests passing.

---

### 12. `any_match<T, F>(slice: &[T], predicate: F) -> bool`

**What it does:** Returns `true` if **at least one** element satisfies the predicate.

**Why it matters:** This is the **existential quantifier** (∃) from logic. Like `all`, it short-circuits — stops at the first `true`. It's the logical dual of `all_match`: `any!(p)` is equivalent to `!all!(not p)`.

**Key concept:** `any` is also lazy. If any element passes the predicate, iteration stops. An empty slice always returns `false` (no elements exist to satisfy the condition).

**Example:** `any_match(&[1, 3, 5], |x| *x % 2 == 0)` → `false`
**Example:** `any_match(&[1, 2, 5], |x| *x % 2 == 0)` → `true`

**When to use:** Search conditions — finding errors in logs, checking if a user has any role, detecting anomalies, verifying at least one option is selected.

---

### 13. `find_first<T, F>(slice: &[T], predicate: F) -> Option<&T>`

**What it does:** Returns the first element matching the predicate, or `None` if no element matches.

**Why it matters:** This combines filtering with early exit. Unlike `filter_slice` which scans the entire slice, `find_first` stops at the first match. The return type `Option<&T>` forces the caller to handle the "not found" case explicitly — no null pointer surprises.

**Key concept:** `Iterator::find` receives `&&T` (reference to the iterator item), so we wrap the predicate as `|item| predicate(*item)` to give the user a clean `&T`. The result is `Option<&T>` — a reference into the original slice, with no copying.

**Example:** `find_first(&[1, 3, 5, 6, 7], |x| *x % 2 == 0)` → `Some(&6)`
**Example:** `find_first(&[1, 3, 5], |x| *x % 2 == 0)` → `None`

**When to use:** Lookup operations — finding the first error, the first matching user, the first available slot, the first element meeting a criterion.

---

## Closure Trait Summary

| Trait       | Can call multiple times? | Can mutate captured state? | Can move captured values? |
| ----------- | :----------------------: | :------------------------: | :-----------------------: |
| `Fn`        | Yes                      | No                         | No                        |
| `FnMut`     | Yes                      | Yes                        | No                        |
| `FnOnce`    | No                       | Yes                        | Yes                       |

Every closure implements `FnOnce`. `FnMut` closures also implement `FnOnce`. `Fn` closures also implement both `FnMut` and `FnOnce`. The hierarchy is: `Fn` ⊂ `FnMut` ⊂ `FnOnce`.

---

## Test Coverage Map

| Function                                          | Happy | Edge Cases | Missing                     |
| ------------------------------------------------- | :---: | :--------: | --------------------------- |
| `apply_closure`                                   | yes   | no         | identity closure, no-op     |
| `map_slice`                                       | yes   | no         | empty slice, type transform |
| `filter_slice`                                    | yes   | no         | empty result, all filtered  |
| `fold_slice`                                      | yes   | no         | empty slice, single element |
| `make_offset_adder`                               | yes   | no         | negative offset, zero       |
| `make_counter`                                    | yes   | no         | reset, multiple counters    |
| `compose`                                         | yes   | no         | identity functions, triple  |
| `chain_apply`                                     | yes   | no         | empty chain, single fn      |
| `curried_add`                                     | yes   | no         | negative values, chained    |
| `partial_apply`                                   | yes   | no         | different types, multi-use  |
| `all_match`                                       | yes   | no         | empty slice, single element |
| `any_match`                                       | yes   | no         | empty slice, single match   |
| `find_first`                                      | yes   | no         | not found, first element    |

---

## What works well

### Consistent `assert_eq!` style

All tests use the compact `assert_eq!(actual, expected)` form. Clean, direct, informative failure messages.

### Counter test validates statefulness

`test_make_counter` calls the closure three times and checks `1, 2, 3`. This proves `FnMut` actually mutates — not just a theoretical property.

### `compose` test verifies order

`compose(|x| x + 1, |x| x * 2)(3) = 7` proves `g` runs first (3×2=6) then `f` (6+1=7). If the order were reversed, the result would be 8.

### `partial_apply` demonstrates the pattern clearly

Taking `|a, b| a * b` and pre-filling `2` gives a `double` function. This is the most concrete way to show what partial application means.

---

## Issues found

### 1. No empty slice tests

None of the slice functions (`map_slice`, `filter_slice`, `fold_slice`, `all_match`, `any_match`, `find_first`) test with an empty slice `&[]`. These are the edge cases that reveal whether the functions handle boundaries correctly:
- `map_slice` → should return empty `Vec`
- `fold_slice` → should return `init` unchanged
- `all_match` → should return `true` (vacuous truth)
- `any_match` → should return `false`
- `find_first` → should return `None`

### 2. No single-element slice tests

A slice with one element `[42]` tests the boundary between "nothing to iterate" and "normal iteration". Many off-by-one bugs hide here.

### 3. `fold_slice` only tests sum and product

The fold is powerful enough to express any accumulation, but only two uses are shown. Consider testing string concatenation or finding a maximum.

### 4. `compose` requires `Clone` — not tested with non-Copy types

The `T: Clone` bound means `compose` works with `String`, `Vec`, etc. But the test only uses `i32` (which is `Copy`). Testing with `String` would prove the clone actually fires.

### 5. `make_counter` only tests one instance

Creating two counters and verifying they track independently would prove the closure captures its own state, not some shared variable.

---

## Summary of recommendations

| Priority | Action                                          | Effort   |
| -------- | ----------------------------------------------- | -------- |
| High     | Add empty slice tests for all slice functions   | 10 min   |
| High     | Add single-element slice tests                  | 5 min    |
| Medium   | Test `compose` with `String` (non-Copy type)    | 2 min    |
| Medium   | Test two independent counters                   | 1 min    |
| Low      | Test `fold_slice` with string concat            | 2 min    |
| Low      | Test `find_first` with match on first element   | 1 min    |
