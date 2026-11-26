# 11-boxed — Test Case Explanations

Each test verifies one demo function. No doc comments in source — all documentation lives here.

## Test: `test_box_new_demo`

**What it proves:** `Box::new()` moves a value from stack to heap.

**Setup:** `let val: u8 = 5; let boxed: Box<u8> = Box::new(val);`

**Assertions:**
- `box_size == 8` — Box is one pointer on the stack (64-bit)
- `val_size == 1` — the u8 value on the heap is 1 byte

**Key insight:** The Box itself is 8 bytes (pointer), the data lives separately on the heap.

---

## Test: `test_box_deref_demo`

**What it proves:** `*boxed` moves the value from heap back to stack, consuming the Box.

**Setup:** `let boxed: Box<u8> = Box::new(42); let val: u8 = *boxed;`

**Assertions:** `val == 42` — dereference extracts the owned value.

**Key insight:** After `*boxed`, the Box is gone — heap memory is freed.

---

## Test: `test_box_heap_address_demo`

**What it proves:** Box data lives on the heap at a non-zero address.

**Setup:** `let boxed: Box<i32> = Box::new(100); let ptr: *const i32 = &*boxed;`

**Assertions:** `addr > 0` — heap address is valid (non-null).

**Key insight:** `&*boxed` takes a reference to the heap data, not the stack pointer.

---

## Test: `test_box_mutate_demo`

**What it proves:** `*boxed = value` mutates heap data safely — no `unsafe` needed.

**Setup:** `let mut boxed: Box<i32> = Box::new(10); *boxed = 200;`

**Assertions:** `*boxed == 200` — mutation succeeded.

**Key insight:** Unlike raw pointers, Box mutation is safe. The borrow checker tracks access.

---

## Test: `test_box_recursive_list_demo`

**What it proves:** Box enables recursive data structures by giving variants a known size.

**Setup:**
```rust
enum List<T> { Cons(T, Box<List<T>>), Nil }
let list = List::Cons(1, Box::new(List::Cons(2, Box::new(List::Nil))));
```

**Assertions:** `list == List::Cons(1, Box::new(List::Cons(2, Box::new(List::Nil))))`

**Key insight:** Without `Box`, `Cons(T, List<T>)` would be infinitely large. Box breaks the cycle — it's always one pointer wide.

---

## Test: `test_box_into_inner_demo`

**What it proves:** `*boxed` (deref) consumes the Box and extracts the owned value.

**Setup:** `let boxed: Box<String> = Box::new(String::from("owned on heap")); let owned = *boxed;`

**Assertions:** `owned == "owned on heap"` — value moved out, Box consumed.

**Key insight:** This is the stable equivalent of `Box::into_inner()` (unstable). The value moves to stack.

---

## Test: `test_box_into_raw_demo`

**What it proves:** `Box::into_raw()` converts Box to raw pointer without freeing memory.

**Setup:** `let raw: *mut i32 = Box::into_raw(boxed);`

**Assertions:**
- `addr > 0` — pointer is non-null
- `is_non_null == true` — memory still allocated

**Key insight:** After `into_raw`, you own the memory. Must call `Box::from_raw()` or leak occurs.

---

## Test: `test_box_from_raw_demo`

**What it proves:** `Box::from_raw()` takes ownership of raw memory and will free it on drop.

**Setup:**
```rust
let raw = Box::into_raw(Box::new(999));
let reclaimed: Box<i32> = Box::from_raw(raw);
```

**Assertions:** `*reclaimed == 999` — value preserved through raw→Box round-trip.

**Key insight:** The pairing is `into_raw` → `from_raw`. The Box reclaims ownership and frees on drop.

---

## Test: `test_box_leak_demo`

**What it proves:** `Box::leak()` converts Box into `&'static mut` — memory is never freed.

**Setup:**
```rust
let boxed: Box<i32> = Box::new(42);
let leaked: &'static mut i32 = Box::leak(boxed);
*leaked = 100;
```

**Assertions:** `*leaked == 100` — mutation through leaked reference works.

**Key insight:** The memory lives forever. Use for global state or FFI callbacks. No deallocation.

---

## Test: `test_box_try_new_demo`

**What it proves:** Fallible allocation pattern — `Box::try_new` (unstable) wrapped as `Ok(Box::new())`.

**Setup:** `let result: Result<Box<[u8; 1024]>, ()> = Ok(Box::new([0u8; 1024]));`

**Assertions:** `result.is_ok() == true` — allocation succeeded.

**Key insight:** `Box::try_new` is unstable. In production, check `is_ok()` before using the Box.

---

## Test: `test_box_pin_demo`

**What it proves:** `Box::pin()` creates a `Pin<Box<T>>` — value cannot be moved in memory.

**Setup:** `let pinned: Pin<Box<i32>> = Box::pin(42);`

**Assertions:** `*pinned == 42` — pinned value is accessible.

**Key insight:** Pinning is required for self-referential structs and async/await state machines.

---

## Test: `test_box_downcast_demo`

**What it proves:** `Box<dyn Any>::downcast()` recovers the concrete type from a trait object.

**Setup:**
```rust
let boxed: Box<dyn Any> = Box::new(42i32);
let result: Result<Box<i32>, _> = boxed.downcast();
```

**Assertions:**
- `is_success == true` — downcast succeeded
- `value == 42` — original value recovered

**Key insight:** Downcast returns `Ok(Box<T>)` on type match, `Err(Box<dyn Any>)` on mismatch.

---

## Test: `test_box_clone_demo`

**What it proves:** `Clone` on Box does a deep copy — allocates new heap memory.

**Setup:**
```rust
let original: Box<i32> = Box::new(100);
let cloned: Box<i32> = original.clone();
```

**Assertions:**
- `*original == 100` — original untouched
- `*cloned == 100` — clone has same value

**Key insight:** Clone allocates a second heap block. Two independent Boxes, same value.

---

## Test: `test_box_from_slice_demo`

**What it proves:** `Box::from(&[T])` creates a boxed slice from a borrowed slice.

**Setup:** `let boxed: Box<[i32]> = Box::from(&[1, 2, 3, 4, 5]);`

**Assertions:** `&*boxed == &[1, 2, 3, 4, 5]` — data copied to heap.

**Key insight:** The slice data moves to heap. Original slice is still valid (borrowed).

---

## Test: `test_box_from_string_demo`

**What it proves:** `String::into_boxed_str()` converts String to `Box<str>` without copying.

**Setup:** `let boxed: Box<str> = String::from("hello").into_boxed_str();`

**Assertions:** `&*boxed == "hello"` — same data, different type.

**Key insight:** Zero-copy conversion. The String's heap buffer becomes the Box<str>'s buffer.

---

## Test: `test_box_from_vec_demo`

**What it proves:** `Vec::into_boxed_slice()` converts Vec to `Box<[T]>` without copying.

**Setup:** `let boxed: Box<[u8]> = vec![10, 20, 30].into_boxed_slice();`

**Assertions:** `&*boxed == &[10, 20, 30]` — same data, different type.

**Key insight:** Zero-copy. Vec's capacity is trimmed to length — no wasted space.

---

## Test: `test_box_memory_layout_demo`

**What it proves:** `Box<T>` is always one pointer wide on the stack, regardless of T's size.

**Setup:** `mem::size_of::<Box<u64>>()` and `mem::size_of::<u64>()`

**Assertions:**
- `box_size == 8` — Box is pointer-sized
- `value_size == 8` — u64 is 8 bytes (coincidence — data lives on heap)

**Key insight:** `Box<u8>` and `Box<[u8; 1000]>` are both 8 bytes on the stack. Heap size varies.
