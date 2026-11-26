# 10-borrow — Borrowed Data Module

Demonstrates `std::borrow` — traits and types for working with borrowed data efficiently.

## What's in `std::borrow`

| Item | What it does |
|------|-------------|
| `Cow<'a, B>` | Clone-on-write smart pointer — borrows when possible, clones only on mutation |
| `Borrow` trait | Generic borrowing — lets code accept both `T` and `&T` (e.g., `HashMap::get`) |
| `BorrowMut` trait | Mutable version of `Borrow` |
| `ToOwned` trait | Creates owned data from borrowed (`&str` → `String`, `&[T]` → `Vec<T>`) |

## Cow — Clone-on-Write

The star of this module. `Cow` (Clone-On-Write) lets you hold either borrowed or owned data, and only clones when you mutate.

### The pattern

```rust
use std::borrow::Cow;

// Zero-copy: holds a reference, no allocation
let cow: Cow<str> = Cow::Borrowed("immutable");

// Mutation triggers clone: Borrowed → Owned
let mut cow = Cow::Borrowed("hello");
cow.to_mut().make_ascii_uppercase();
// Now cow is Cow::Owned("HELLO") — cloned exactly once
```

### When to use Cow

| Scenario | Use |
|----------|-----|
| Function accepts input, may or may not modify it | `Cow<str>` — borrow if no change, clone if mutation needed |
| Configuration that's usually static but can be overridden | `Cow<Path>` — borrow default path, own custom path |
| API that returns either a reference or a computed value | `Cow<'a, str>` — avoid allocation in the common case |

### Cow variants

```rust
Cow::Borrowed(&str)  // zero-copy, tied to lifetime 'a
Cow::Owned(String)   // owns the data, no lifetime constraint
```

## Borrow / BorrowMut — Generic Borrowing

These traits let generic code work with both owned and borrowed forms.

### The HashMap pattern

```rust
use std::collections::HashMap;
use std::borrow::Borrow;

let mut map: HashMap<String, i32> = HashMap::new();
map.insert("key".to_string(), 42);

// Lookup with &str — works because String: Borrow<str>
map.get("key");  // Some(&42)
```

`HashMap::get` accepts `impl Borrow<Q>` so you can query with `&str` even when keys are `String`. No need to allocate a `String` just for lookup.

### Borrow trait implementations

| Type | Implements `Borrow<...>` |
|------|------------------------|
| `String` | `Borrow<str>` |
| `PathBuf` | `Borrow<Path>` |
| `Vec<T>` | `Borrow<[T]>` |
| `T` | `Borrow<T>` (reflexive) |

## ToOwned — Borrowed → Owned

The reverse of `Borrow`. Creates an owned copy from borrowed data.

```rust
let borrowed: &str = "hello";
let owned: String = borrowed.to_owned();  // &str → String

let slice: &[i32] = &[1, 2, 3];
let vec: Vec<i32> = slice.to_owned();     // &[i32] → Vec<i32>
```

`ToOwned` is more general than `Clone` — it lets the owned type differ from the borrowed type (`&str` vs `String`).

## Key Differences

| Concept | Borrowed | Owned |
|---------|----------|-------|
| Lifetime | Tied to `'a` | No lifetime — owns its data |
| Allocation | Zero (references existing data) | Allocates new memory |
| Mutation | Immutable (`&T`) | Mutable (`T`) |
| Cow behavior | `to_mut()` clones | `to_mut()` returns self |

## Demo Functions

| Function | Demonstrates |
|----------|-------------|
| `to_owned_demo` | `&str` → `String` via `ToOwned` |
| `to_owned_slice_demo` | `&[i32]` → `Vec<i32>` via `ToOwned` |
| `borrow_trait_demo` | Generic function accepting `impl Borrow<str>` |
| `borrow_mut_trait_demo` | Mutable borrowing via `BorrowMut` |
| `borrow_hashmap_style_demo` | `HashMap` lookup with `&str` keys |
| `cow_borrowed_demo` | `Cow::Borrowed` — zero-copy |
| `cow_owned_demo` | `Cow::Owned` — owns data |
| `cow_clone_on_write_demo` | Mutation triggers Borrowed → Owned clone |
| `cow_owned_no_clone_demo` | Already owned — `to_mut()` doesn't clone |
| `cow_function_return_demo` | Return `Cow` — borrow or own based on content |
| `cow_slice_demo` | `Cow<[i32]>` — clone-on-write for slices |
| `cow_deref_demo` | Read through `Cow` without cloning |
| `cow_lifetime_demo` | Lifetime parameter on `Cow<'a, str>` |
| `cow_comparison_demo` | When to choose Borrowed vs Owned |
