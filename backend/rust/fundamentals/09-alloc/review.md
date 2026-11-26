# 09-alloc — Raw Memory Allocation

Demonstrates `std::alloc` global allocator functions for manual memory management.

## Functions Demonstrated

| Function | What it does |
|----------|-------------|
| `alloc(layout)` | Allocates uninitialized memory of the given size and alignment |
| `alloc_zeroed(layout)` | Allocates zero-initialized memory |
| `dealloc(ptr, layout)` | Frees previously allocated memory |
| `realloc(ptr, old_layout, new_size)` | Resizes an existing allocation, preserving content |
| `handle_alloc_error(layout)` | Prints allocation failure message and aborts — never returns |

## Key Concepts

- **`Layout`**: Describes memory block requirements — `size` (bytes) and `align` (power of two)
- **Raw pointers**: All functions work with `*mut u8` — no Rust safety guarantees
- **Null checks**: Every allocation must check for null before use; null means out of memory
- **Matched pairs**: Every `alloc`/`alloc_zeroed`/`realloc` needs a matching `dealloc` with the **current** layout
- **`realloc` layout**: Pass the **old** layout and **new size** — the returned pointer has the new size

## Safety Rules

1. Never dereference a null pointer from allocation
2. Never `dealloc` with a different layout than what was allocated
3. After `realloc`, the old pointer is invalid — use only the returned pointer
4. `handle_alloc_error` diverges (never returns) — useful for OOM-critical paths

## When to Use

Raw allocation is the foundation of `Vec`, `Box`, `String`, and all collection types.
Use it directly only when building custom data structures, FFI bindings, or allocators.
