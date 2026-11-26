# Pointers in Rust — A C Programmer's Guide

You know C pointers cold. Rust has the same underlying machinery but enforces rules at compile time.
This guide maps every pointer concept you already know to its Rust equivalent, with `*` and `&` examples for each.

---

## 1. The Operator Table — Quick Reference

| Operator | In C | In Rust | What it does |
|----------|------|---------|-------------|
| `&` | `&x` → address of `x` | `&x` → **borrow** `x` as `&T` | Takes a safe reference, not a raw address |
| `*` | `*p` → dereference `p` | `*p` → **dereference** any pointer-like type | Works on references, raw pointers, and smart pointers |
| `&mut` | — | `&mut x` → **mutable borrow** | Exclusive mutable reference (no C equivalent — C has no borrow checker) |
| `*const T` | `const T*` | `*const T` | Immutable raw pointer — like C, no safety guarantees |
| `*mut T` | `T*` | `*mut T` | Mutable raw pointer — like C, no safety guarantees |

---

## 2. References (`&T` / `&mut T`) — Rust's Default "Pointer"

In C you pass pointers everywhere. In Rust you pass **references** — they look like pointers but the compiler guarantees no dangling, no data races, no double-free.

### `&T` — Shared (immutable) reference

```rust
let value = 42;
let ref_to_value = &value;       // &i32 — borrow immutably

println!("{}", *ref_to_value);   // 42  ← * dereferences the reference
println!("{}", ref_to_value);    // 42  ← auto-deref in most contexts
```

**C equivalent:** `const int* ref = &value;` — but you can't make it dangle, and the compiler tracks lifetime.

### `&mut T` — Exclusive (mutable) reference

```rust
let mut value = 42;
let mut_ref = &mut value;        // &mut i32 — borrow mutably

*mut_ref = 100;                  // mutate through the reference
println!("{}", *mut_ref);        // 100
```

**C equivalent:** `int* mut_ref = &value;` — but Rust enforces **exclusive access**: while `&mut T` exists, no other reference (read or write) to `value` can exist.

### `*` on references — dereference

```rust
let x = 10;
let rx = &x;

let dereferenced = *rx;          // copies the value: i32 = 10
```

You rarely need explicit `*` with references — Rust auto-derefs in most expressions. But it's there when you need it.

### `&` on values — create a reference

```rust
fn takes_ref(s: &String) { ... }

let s = String::from("hello");
takes_ref(&s);                   // & creates &String from String
```

---

## 3. Raw Pointers (`*const T` / `*mut T`) — C Pointers, No Safety

These are the closest thing to C pointers. No lifetime tracking, no null checks, no aliasing rules. **Must be inside `unsafe` blocks to dereference.**

### `*const T` — immutable raw pointer

```rust
let value = 42;
let raw_ptr: *const i32 = &value;     // &T coerces to *const T

unsafe {
    println!("{}", *raw_ptr);          // 42  ← * dereferences raw pointer
}
```

### `*mut T` — mutable raw pointer

```rust
let mut value = 42;
let raw_mut: *mut i32 = &mut value;   // &mut T coerces to *mut T

unsafe {
    *raw_mut = 100;                    // mutate through raw pointer
    println!("{}", *raw_mut);          // 100
}
```

### Creating from integers (FFI pattern)

```rust
let ptr_from_addr: *mut i32 = 0x1000 as *mut i32;  // C-style: int → pointer

unsafe {
    // Don't do this unless it's a real address from the OS / FFI
}
```

### Null check

```rust
let maybe_null: *mut i32 = std::ptr::null_mut();

if !maybe_null.is_null() {
    unsafe { *maybe_null = 42; }
}
```

**Key difference from C:** you can **create** raw pointers in safe code (`&x as *const T`), but you can only **dereference** them in `unsafe`. This is the safety gate.

---

## 4. `unsafe` Blocks — The Trust Boundary

`unsafe` does **not** turn off the borrow checker. It only allows these five operations:

| Operation | Syntax |
|-----------|--------|
| Dereference raw pointer | `*ptr` |
| Call unsafe function | `unsafe_fn()` |
| Access mutable static | `STATIC_MUT = 1` |
| Implement unsafe trait | `unsafe impl Trait for T` |
| Access union field | `union.field` |

Everything else — borrows, lifetimes, ownership — still applies inside `unsafe`.

```rust
// This compiles (safe code):
let x = 10;
let p: *const i32 = &x;

// This does NOT compile (safe dereference of raw pointer):
// let val = *p;                          // error[E0133]

// This compiles (unsafe block grants permission):
unsafe {
    let val = *p;                        // OK inside unsafe
}
```

### Why `unsafe` for raw pointers?

Because the compiler can't prove:
- The pointer is not null
- The memory is still alive (not freed / out of scope)
- No other mutable alias exists
- The alignment is correct

You — the programmer — take responsibility for those guarantees.

---

## 5. Smart Pointers — Heap Allocation with Safety

In C you `malloc`/`free` raw pointers. Rust wraps heap allocation in types that guarantee cleanup and safety.

### `Box<T>` — Owned heap allocation

The simplest smart pointer. Like `malloc` + automatic `free` when it goes out of scope.

```rust
let b = Box::new(42);                    // Box<i32> on the heap

println!("{}", *b);                      // 42  ← * dereferences Box
let moved = b;                           // ownership moves, no copy

// b is invalid here — moved to `moved`
// println!("{}", *b);                  // error: use of moved value
```

**C equivalent:** `int* b = malloc(sizeof(int)); *b = 42;` — but no manual `free`, and ownership rules prevent double-free.

#### `&` and `*` with Box

```rust
let b = Box::new(10);

let ref_to_box = &b;                     // &Box<i32> — reference to the Box itself
let ref_to_value = &*b;                  // &i32 — deref Box, then borrow the value
let ref_auto = &b;                       // &i32 — auto-deref in most contexts

*b;                                      // i32 — explicit dereference
```

### `Rc<T>` — Reference-counted shared ownership

Multiple owners, single-threaded. Like C's manual refcount but automatic.

```rust
use std::rc::Rc;

let rc = Rc::new(42);
let rc_clone = Rc::clone(&rc);           // +1 to refcount (NOT a deep copy)

println!("{}", *rc);                     // 42
println!("{}", *rc_clone);               // 42

println!("{}", Rc::strong_count(&rc));   // 2

// When both go out of scope, refcount hits 0 → automatic deallocation
```

**C equivalent:** `struct { int val; int refcount; }` with manual `retain`/`release`.

### `Arc<T>` — Thread-safe reference counting

Same as `Rc<T>` but safe to share across threads (atomic refcount).

```rust
use std::sync::Arc;
use std::thread;

let arc = Arc::new(42);
let arc_clone = Arc::clone(&arc);

let handle = thread::spawn(move || {
    println!("{}", *arc_clone);          // 42  ← usable in another thread
});

handle.join().unwrap();
```

### `RefCell<T>` — Interior mutability (single-threaded)

Borrow rules enforced at **runtime** instead of compile time. Lets you mutate through an immutable reference.

```rust
use std::cell::RefCell;

let rc = RefCell::new(42);

{
    let mut borrowed = rc.borrow_mut();   // runtime mutable borrow
    *borrowed = 100;
}                                         // borrow released here

let borrowed = rc.borrow();               // runtime immutable borrow
println!("{}", *borrowed);                // 100
```

**Panics if you violate borrow rules at runtime:**

```rust
let rc = RefCell::new(42);
let b1 = rc.borrow();
let b2 = rc.borrow_mut();                 // PANIC: already borrowed immutably
```

### `Cell<T>` — Interior mutability for `Copy` types

No borrowing overhead. Stores a copy of `Copy` types.

```rust
use std::cell::Cell;

let cell = Cell::new(42);
cell.set(100);                            // mutate without &mut
println!("{}", cell.get());               // 100
```

---

## 6. `*` and `&` — Complete Cheat Sheet

### On regular values

```rust
let x = 10;
let rx = &x;           // & — borrow: &i32
let val = *rx;         // * — dereference: i32 = 10
```

### On mutable values

```rust
let mut x = 10;
let mx = &mut x;       // &mut — mutable borrow: &mut i32
*mx = 20;              // * — dereference and mutate
```

### On raw pointers

```rust
let x = 10;
let p: *const i32 = &x;       // &T → *const T coercion
unsafe { let val = *p; }      // * — dereference (must be unsafe)
```

### On `Box<T>`

```rust
let b = Box::new(10);
let rb = &b;           // &Box<i32> — reference to Box
let rv = &*b;          // &i32 — deref then borrow
let v = *b;            // i32 — dereference (moves if not Copy)
```

### On `Rc<T>` / `Arc<T>`

```rust
let rc = Rc::new(10);
let rrc = &rc;         // &Rc<i32> — reference to Rc
let rv = &*rc;         // &i32 — deref then borrow
let v = *rc;           // i32 — dereference (clones if not Copy)
```

### On `RefCell<T>`

```rust
let rc = RefCell::new(10);
let borrowed = rc.borrow();   // Ref<i32> — runtime borrow guard
let val = *borrowed;          // i32 — dereference the guard
```

---

## 7. From C to Rust — Translation Patterns

| C pattern | Rust equivalent | Why |
|-----------|----------------|-----|
| `int* p = &x` | `let p = &x` (reference) or `let p: *const i32 = &x` (raw) | References are the default; raw pointers need `unsafe` to dereference |
| `*p = 42` | `*p = 42` (inside `unsafe` for raw) or `*ref = 42` (for `&mut`) | Same syntax, different safety model |
| `malloc(sizeof(T))` | `Box::new(T)` or `Vec::with_capacity(n)` | Automatic cleanup, no manual `free` |
| `free(p)` | drop when scope ends | RAII — no manual deallocation |
| `const int*` | `*const i32` (raw) or `&i32` (reference) | Rust distinguishes safe refs from raw pointers |
| Pointer arithmetic `p + 1` | `ptr.add(1)` on raw pointers | Method syntax, still requires `unsafe` |
| `NULL` check | `ptr.is_null()` | Same concept, method instead of `== NULL` |
| `void*` | `*mut c_void` (FFI) or `&dyn Any` (safe downcasting) | Type-erased pointer for FFI |

---

## 8. When to Use What

| Situation | Use |
|-----------|-----|
| Passing data to a function | `&T` or `&mut T` (references) |
| Owning heap data | `Box<T>` |
| Shared ownership, single thread | `Rc<T>` |
| Shared ownership, multi-threaded | `Arc<T>` |
| Mutating through shared reference | `RefCell<T>` or `Cell<T>` |
| FFI, OS calls, custom allocators | `*mut T` / `*const T` in `unsafe` |
| Building Vec/Box/String from scratch | `std::alloc` functions in `unsafe` |

**Rule of thumb:** start with references. Only reach for raw pointers when you're building the abstractions (like `Vec` or `Box`) that everyone else uses.
