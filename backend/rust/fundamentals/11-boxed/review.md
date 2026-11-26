# 11-boxed — Box<T> Heap Allocation

Demonstrates `std::boxed` — the simplest form of heap allocation in Rust.

## What is Box<T>?

`Box<T>` owns heap-allocated data. When the `Box` goes out of scope, its contents are dropped and memory is freed. A `Box` is a single pointer on the stack pointing to heap data.

## Memory Layout

| Type | Stack size | Heap allocation |
|------|-----------|-----------------|
| `Box<u8>` | 8 bytes (pointer) | 1 byte |
| `Box<u64>` | 8 bytes (pointer) | 8 bytes |
| `Box<[i32]>` | 16 bytes (fat pointer) | N × 4 bytes |

`Box` is always one pointer wide — the data lives on the heap.

## Core Operations

### Stack → Heap: `Box::new()`

```rust
let val: u8 = 5;
let boxed: Box<u8> = Box::new(val);
```

Moves `val` from stack to heap. The original `val` is no longer accessible.

### Heap → Stack: dereference `*boxed`

```rust
let boxed: Box<u8> = Box::new(42);
let val: u8 = *boxed;
```

Moves the value back to the stack. The `Box` is consumed — heap is freed.

### Mutate through Box

```rust
let mut boxed: Box<i32> = Box::new(10);
*boxed = 200;
```

Same `*` syntax as raw pointers and references, but safe — no `unsafe` needed.

## Recursive Data Structures

Box enables recursive types by giving them a known size:

```rust
enum List<T> {
    Cons(T, Box<List<T>>),  // Box has known size (pointer)
    Nil,
}
```

Without `Box`, `Cons(T, List<T>)` would be infinitely large — the compiler can't determine how much memory to allocate.

## Raw Pointer Conversion

### `Box::into_raw()` — Box → raw pointer

Consumes the `Box`, returns a `*mut T`. The memory is NOT freed — you own it now.

```rust
let boxed: Box<i32> = Box::new(777);
let raw: *mut i32 = Box::into_raw(boxed);
// raw points to heap memory; boxed is gone
```

### `Box::from_raw()` — raw pointer → Box

Takes ownership of raw memory, wraps it in a `Box`. The `Box` will free it when dropped.

```rust
unsafe {
    let reclaimed: Box<i32> = Box::from_raw(raw);
}
// reclaimed drops → memory freed
```

**Rule:** every `into_raw` must have a matching `from_raw` (or `Box::from_raw_in` with allocator). Otherwise, memory leak.

## Box::leak() — Convert to &'static mut

Converts a `Box` into a mutable reference with `'static` lifetime. The memory is never freed.

```rust
let boxed: Box<i32> = Box::new(42);
let leaked: &'static mut i32 = Box::leak(boxed);
// leaked lives forever; no deallocation
```

Use case: global mutable state, FFI callbacks, or when you need a reference that outlives all scopes.

## Conversions

| From | Method | Result |
|------|--------|--------|
| `&[T]` | `Box::from(slice)` | `Box<[T]>` |
| `String` | `string.into_boxed_str()` | `Box<str>` |
| `Vec<T>` | `vec.into_boxed_slice()` | `Box<[T]>` |

These convert existing allocations into boxed forms without copying.

## Box::pin() — Pinned Allocation

Creates a `Pin<Box<T>>` — the value cannot be moved in memory after creation. Required for self-referential structs and async/await state machines.

```rust
let pinned: Pin<Box<i32>> = Box::pin(42);
```

## Box::downcast() — Trait Object Recovery

Downcasts a `Box<dyn Any>` back to a concrete type:

```rust
let boxed: Box<dyn Any> = Box::new(42i32);
let result: Result<Box<i32>, _> = boxed.downcast();
```

Returns `Ok(Box<T>)` if the type matches, `Err(Box<dyn Any>)` otherwise.

## Demo Functions

| Function | Demonstrates |
|----------|-------------|
| `box_new_demo` | `Box::new()` — stack to heap, sizes |
| `box_deref_demo` | `*boxed` — heap back to stack |
| `box_heap_address_demo` | Heap pointer address |
| `box_mutate_demo` | `*boxed = value` — safe mutation |
| `box_recursive_list_demo` | `Box` in recursive enum (Cons/Nil list) |
| `box_into_inner_demo` | `*boxed` — consume Box, extract owned value |
| `box_into_raw_demo` | `Box::into_raw()` — Box → raw pointer |
| `box_from_raw_demo` | `Box::from_raw()` — raw pointer → Box |
| `box_leak_demo` | `Box::leak()` — Box → &'static mut |
| `box_try_new_demo` | Fallible allocation pattern |
| `box_pin_demo` | `Box::pin()` — pinned allocation |
| `box_downcast_demo` | `Box<dyn Any>::downcast()` |
| `box_clone_demo` | `Clone` on `Box` — deep copy |
| `box_from_slice_demo` | `Box::from(&[T])` — slice to boxed slice |
| `box_from_string_demo` | `into_boxed_str()` — String to Box<str> |
| `box_from_vec_demo` | `into_boxed_slice()` — Vec to Box<[T]> |
| `box_memory_layout_demo` | `size_of::<Box<T>>()` — pointer width |
