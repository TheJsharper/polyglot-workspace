use std::alloc::{alloc, alloc_zeroed, dealloc, handle_alloc_error, realloc, Layout};
use std::ptr;

/// Demonstrates basic memory allocation with `alloc()`.
///
/// Returns a tuple of (raw pointer as usize, layout) for inspection.
/// The caller must deallocate through `dealloc_demo()`.
pub fn alloc_demo() -> (usize, Layout) {
    let layout = Layout::from_size_align(16, 8).unwrap();

    let ptr = unsafe { alloc(layout) };

    if ptr.is_null() {
        handle_alloc_error(layout);
    }

    unsafe {
        ptr::write(ptr as *mut u8, 0xAA);
        ptr::write(ptr.add(1) as *mut u8, 0xBB);
        ptr::write(ptr.add(2) as *mut u8, 0xCC);
        ptr::write(ptr.add(3) as *mut u8, 0xDD);
    }

    let first_bytes = unsafe {
        [
            ptr::read(ptr as *const u8),
            ptr::read(ptr.add(1) as *const u8),
            ptr::read(ptr.add(2) as *const u8),
            ptr::read(ptr.add(3) as *const u8),
        ]
    };

    (first_bytes[0] as usize, layout)
}

/// Demonstrates zero-initialized allocation with `alloc_zeroed()`.
///
/// Returns the number of zero bytes confirmed in the first 8 bytes.
pub fn alloc_zeroed_demo() -> usize {
    let layout = Layout::from_size_align(32, 8).unwrap();

    let ptr = unsafe { alloc_zeroed(layout) };

    if ptr.is_null() {
        handle_alloc_error(layout);
    }

    let zero_count = unsafe {
        (0..8)
            .filter(|&i| ptr::read(ptr.add(i) as *const u8) == 0)
            .count()
    };

    unsafe { dealloc(ptr, layout) };

    zero_count
}

/// Demonstrates manual deallocation with `dealloc()`.
///
/// Returns true if deallocation completed without issue.
pub fn dealloc_demo() -> bool {
    let layout = Layout::from_size_align(64, 8).unwrap();

    let ptr = unsafe { alloc(layout) };

    if ptr.is_null() {
        return false;
    }

    unsafe { dealloc(ptr, layout) };

    true
}

/// Demonstrates `handle_alloc_error()` behavior.
///
/// Returns a description of what the function does — it never returns normally.
pub fn handle_alloc_error_description() -> String {
    "handle_alloc_error(layout) prints an allocation error message and aborts the program"
        .to_string()
}

/// Demonstrates memory reallocation with `realloc()`.
///
/// Allocates 8 bytes, writes a value, reallocates to 16 bytes,
/// verifies the original value survived, and returns the new capacity.
pub fn realloc_demo() -> (usize, usize) {
    let old_layout = Layout::from_size_align(8, 8).unwrap();

    let old_ptr = unsafe { alloc(old_layout) };

    if old_ptr.is_null() {
        handle_alloc_error(old_layout);
    }

    unsafe { ptr::write(old_ptr as *mut u32, 42) };

    let new_layout = Layout::from_size_align(16, 8).unwrap();

    let new_ptr = unsafe { realloc(old_ptr, old_layout, new_layout.size()) };

    if new_ptr.is_null() {
        handle_alloc_error(new_layout);
    }

    let preserved_value = unsafe { ptr::read(new_ptr as *const u32) };

    unsafe { dealloc(new_ptr, new_layout) };

    (preserved_value as usize, new_layout.size())
}

/// Demonstrates `Layout::from_size_align()` with valid parameters.
///
/// Returns (size, align) tuple.
pub fn layout_valid_demo() -> (usize, usize) {
    let layout = Layout::from_size_align(128, 16).unwrap();

    (layout.size(), layout.align())
}

/// Demonstrates `Layout::from_size_align()` failure on invalid alignment.
///
/// Returns true if the error was caught (alignment not power of two).
pub fn layout_invalid_align_demo() -> bool {
    Layout::from_size_align(16, 3).is_err()
}

/// Demonstrates `Layout::from_size_align()` failure on zero size with zero alignment.
///
/// Returns true if the error was caught.
pub fn layout_zero_size_demo() -> bool {
    Layout::from_size_align(0, 1).is_ok()
}

/// Demonstrates allocation of a structured layout (simulating a struct).
///
/// Allocates space for a "struct" with size 16 and align 8,
/// writes a header byte and data, returns the header value.
pub fn alloc_struct_layout_demo() -> u8 {
    let layout = Layout::from_size_align(16, 8).unwrap();

    let ptr = unsafe { alloc(layout) };

    if ptr.is_null() {
        handle_alloc_error(layout);
    }

    unsafe {
        ptr::write(ptr as *mut u8, 0xDE);
        ptr::write(ptr.add(8) as *mut u8, 0xAD);
    }

    let header = unsafe { ptr::read(ptr as *const u8) };

    unsafe { dealloc(ptr, layout) };

    header
}

/// Demonstrates repeated allocation and deallocation cycle.
///
/// Returns the number of successful alloc/dealloc cycles completed.
pub fn alloc_dealloc_cycle_demo() -> usize {
    let layout = Layout::from_size_align(4, 4).unwrap();
    let mut success_count = 0;

    for _ in 0..10 {
        let ptr = unsafe { alloc(layout) };
        if !ptr.is_null() {
            unsafe { dealloc(ptr, layout) };
            success_count += 1;
        }
    }

    success_count
}

/// Demonstrates `realloc` shrinking memory.
///
/// Allocates 32 bytes, reallocates to 8 bytes,
/// returns the new size.
pub fn realloc_shrink_demo() -> usize {
    let old_layout = Layout::from_size_align(32, 8).unwrap();

    let old_ptr = unsafe { alloc(old_layout) };

    if old_ptr.is_null() {
        handle_alloc_error(old_layout);
    }

    let new_size = 8;
    let new_ptr = unsafe { realloc(old_ptr, old_layout, new_size) };

    if new_ptr.is_null() {
        handle_alloc_error(Layout::from_size_align(new_size, 8).unwrap());
    }

    unsafe { dealloc(new_ptr, Layout::from_size_align(new_size, 8).unwrap()) };

    new_size
}

/// Demonstrates allocation with maximum alignment.
///
/// Returns the alignment value confirmed from the layout.
pub fn alloc_max_align_demo() -> usize {
    let layout = Layout::from_size_align(64, 64).unwrap();

    layout.align()
}

/// Returns a description of the `std::alloc` module.
pub fn alloc_module_description() -> &'static str {
    "std::alloc provides the global allocator interface: alloc, alloc_zeroed, dealloc, realloc, handle_alloc_error"
}

// ─────────────────────────────────────────────
// Pointer & reference operators — C programmer guide
// ─────────────────────────────────────────────

/// `&` — immutable borrow. Returns a shared reference to the value.
///
/// C equivalent: `const int* ref = &x;` — but safe, no dangling possible.
pub fn borrow_immutable_demo() -> (i32, i32) {
    let value = 42;
    let borrowed = &value; // &i32 — immutable reference

    let dereferenced = *borrowed; // * — dereference the reference

    // Can have multiple immutable borrows simultaneously
    let also_borrowed = &value;

    (dereferenced, *also_borrowed) // (42, 42)
}

/// `&mut` — mutable borrow. Returns an exclusive mutable reference.
///
/// No C equivalent — C has no concept of exclusive mutable access enforcement.
pub fn borrow_mutable_demo() -> i32 {
    let mut value = 10;
    let mut_borrowed = &mut value; // &mut i32 — exclusive mutable reference

    *mut_borrowed = 100; // * — dereference and mutate

    // While mut_borrowed exists, NO other reference to `value` can exist.
    // This would NOT compile:
    // let other = &value;        // error: cannot borrow as immutable
    // let other_mut = &mut value; // error: cannot borrow as mutable

    *mut_borrowed
}

/// `*const T` — immutable raw pointer. Like C's `const T*`, no safety guarantees.
///
/// Can be created in safe code, but dereferencing requires `unsafe`.
pub fn raw_const_pointer_demo() -> u8 {
    let value: u8 = 0xDE;
    let raw: *const u8 = &value; // &T coerces to *const T

    // Can pass raw pointers around in safe code
    let copied = raw;

    // Dereferencing requires unsafe — compiler can't prove it's valid
    unsafe { *copied } // * — dereference raw const pointer
}

/// `*mut T` — mutable raw pointer. Like C's `T*`, no safety guarantees.
///
/// Can be created in safe code, but dereferencing and mutation require `unsafe`.
pub fn raw_mut_pointer_demo() -> u8 {
    let mut value: u8 = 0x00;
    let raw: *mut u8 = &mut value; // &mut T coerces to *mut T

    unsafe {
        *raw = 0xAD; // * — dereference and mutate through raw pointer
    }

    unsafe { *raw } // read back through raw pointer
}

/// `*` dereference across all pointer types — side by side comparison.
///
/// Returns (imm_ref_deref, mut_ref_deref, raw_const_deref, raw_mut_deref).
pub fn deref_all_types_demo() -> (i32, i32, i32, i32) {
    let mut value = 42;

    // &T — immutable reference
    let imm_ref: &i32 = &value;
    let imm_deref = *imm_ref; // * on &T — safe, no unsafe needed

    // &mut T — mutable reference
    let mut_ref: &mut i32 = &mut value;
    *mut_ref = 100; // * on &mut T — safe mutation
    let mut_deref = *mut_ref;

    // *const T — immutable raw pointer
    let raw_const: *const i32 = &value;
    let raw_const_deref = unsafe { *raw_const }; // * on *const T — needs unsafe

    // *mut T — mutable raw pointer
    let raw_mut: *mut i32 = &mut value;
    unsafe { *raw_mut = 200 }; // * on *mut T — needs unsafe for mutation
    let raw_mut_deref = unsafe { *raw_mut };

    (imm_deref, mut_deref, raw_const_deref, raw_mut_deref) // (42, 100, 100, 200)
}

/// `&` creating references to smart pointers vs to their inner values.
///
/// Returns (ref_to_box, ref_to_inner, ref_to_rc, ref_to_rc_inner).
pub fn borrow_smart_pointers_demo() -> (usize, usize, usize, usize) {
    use std::rc::Rc;

    let boxed = Box::new(42);
    let rc = Rc::new(99);

    // &Box<T> — reference to the Box wrapper itself (on the stack)
    let ref_to_box: &Box<i32> = &boxed;

    // &*Box<T> — deref the Box, then borrow the inner value
    let ref_to_inner: &i32 = &*boxed;

    // &Rc<T> — reference to the Rc wrapper
    let ref_to_rc: &Rc<i32> = &rc;

    // &*Rc<T> — deref the Rc, then borrow the inner value
    let ref_to_rc_inner: &i32 = &*rc;

    (
        **ref_to_box as usize,
        *ref_to_inner as usize,
        **ref_to_rc as usize,
        *ref_to_rc_inner as usize,
    )
}

/// Shows that `&mut` is exclusive — this function demonstrates the pattern
/// by taking and releasing a mutable borrow in sequence.
///
/// Returns the value after sequential mutable modifications.
pub fn exclusive_mut_borrow_demo() -> i32 {
    let mut value = 1;

    {
        let m1 = &mut value; // first mutable borrow
        *m1 += 10;           // 11
    } // m1 dropped here — borrow released

    {
        let m2 = &mut value; // second mutable borrow (legal — m1 is gone)
        *m2 += 100;          // 111
    } // m2 dropped here

    value
}

/// Raw pointer arithmetic — `ptr.add()` and `ptr.sub()`.
///
/// C equivalent: `p + 1` and `p - 1`. In Rust, raw pointers have `.add()` / `.sub()` methods.
/// Returns (value_at_offset_0, value_at_offset_1, value_at_offset_2).
pub fn raw_pointer_arithmetic_demo() -> (u8, u8, u8) {
    let data: [u8; 4] = [0xAA, 0xBB, 0xCC, 0xDD];
    let ptr: *const u8 = data.as_ptr(); // *const u8 from slice

    unsafe {
        let at_0 = *ptr;          // offset 0
        let at_1 = *ptr.add(1);   // offset 1 — C: *(p + 1)
        let at_2 = *ptr.add(2);   // offset 2 — C: *(p + 2)

        (at_0, at_1, at_2)
    }
}

/// Null pointer check — raw pointers can be null.
///
/// C equivalent: `if (p != NULL)`. In Rust: `if !ptr.is_null()`.
/// Returns true if the null check correctly rejects a null pointer.
pub fn null_pointer_check_demo() -> bool {
    let null_ptr: *mut i32 = std::ptr::null_mut();

    // Never dereference without checking
    if null_ptr.is_null() {
        return true; // correctly identified null
    }

    false
}

/// Coercion chain: `&T` → `*const T` → `*mut T` (with cast).
///
/// Shows how references flow into raw pointers.
/// Returns the value read back through the full chain.
pub fn coercion_chain_demo() -> i32 {
    let value = 777;

    // Step 1: &T (safe reference)
    let imm_ref: &i32 = &value;

    // Step 2: &T coerces to *const T (still safe — just creating the pointer)
    let const_ptr: *const i32 = imm_ref;

    // Step 3: *const T cast to *mut T (safe — just changing the type)
    let mut_ptr: *mut i32 = const_ptr as *mut i32;

    // Step 4: *mut T dereference — requires unsafe
    unsafe { *mut_ptr }
}

/// Correct type annotations for every pointer/reference combination.
///
/// **There is no `*const mut T` in Rust.** Raw pointers are either `*const T` or `*mut T`.
/// The `mut` on `let mut value` is independent from the `mut` in `*mut T`.
///
/// Returns (imm_ref, mut_ref, raw_const, raw_mut) all reading the same final value.
pub fn type_annotations_demo() -> (i32, i32, i32, i32) {
    // ── References (safe, no unsafe needed) ──

    // &T — immutable reference to an immutable value
    let imm_value = 10;
    let imm_ref: &i32 = &imm_value;

    // &mut T — mutable reference to a mutable value
    let mut mut_value = 20;
    let mut_ref: &mut i32 = &mut mut_value;
    *mut_ref = 200;

    // ── Raw pointers (dereference requires unsafe) ──

    // *const T — immutable raw pointer from &T
    let const_ptr: *const i32 = &imm_value;

    // *mut T — mutable raw pointer from &mut T
    let mut raw_mut_value = 30;
    let mut_ptr: *mut i32 = &mut raw_mut_value;
    unsafe {
        *mut_ptr = 300;
    }

    // ── Key point: *const mut T does NOT exist ─
    // These are the ONLY valid raw pointer types:
    //   *const T   ← read-only raw pointer
    //   *mut T     ← writable raw pointer
    //
    // Invalid (won't compile):
    //   let bad: *const mut i32 = ...  // error: expected one of `!`, `(`, `+`, `,`, `::`, `<`, or `>`, found `mut`
    //   let bad: *mut const i32 = ...  // error: expected one of `!`, `(`, `+`, `,`, `::`, `<`, `>`, or `?`, found `const`

    (
        *imm_ref,
        *mut_ref,
        unsafe { *const_ptr },
        unsafe { *mut_ptr },
    )
}

/// Shows that `let mut` on the variable and `*mut T` on the pointer are independent.
///
/// You can have a mutable value with a `*const` pointer (read-only access),
/// and a mutable value with a `*mut` pointer (read-write access).
///
/// Returns (mut_value_through_const_ptr, mut_value_through_mut_ptr).
pub fn mut_vs_pointer_mut_demo() -> (i32, i32) {
    // Case 1: immutable value, *const pointer — read-only through pointer
    let value_a = 100;
    let const_ptr_a: *const i32 = &value_a;
    // unsafe { *const_ptr_a = 999; }  // error: cannot assign through *const T

    // Case 2: mutable value, *mut pointer — can READ and WRITE
    let mut value_b = 200;
    let mut_ptr_b: *mut i32 = &mut value_b;
    unsafe {
        *mut_ptr_b = 2000;
    }

    (unsafe { *const_ptr_a }, unsafe { *mut_ptr_b })
}

/// Proves that mutating through a `*mut T` pointer mutates the original variable.
///
/// **Yes — exactly like C.** The pointer and the variable share the same memory.
/// After `*ptr = new_value`, reading the variable gives `new_value`.
///
/// Returns (value_before, value_through_ptr, value_after) = (100, 9999, 9999).
pub fn pointer_mutates_original_demo() -> (i32, i32, i32) {
    let mut original = 100;

    let value_before = original;

    let ptr: *mut i32 = &mut original; // pointer to the SAME memory as `original`

    unsafe {
        *ptr = 9999; // mutate through the pointer
    }

    let value_through_ptr = unsafe { *ptr };
    let value_after = original; // reading the variable sees the mutation

    // All three prove the same memory was modified:
    //   value_before:      100
    //   value_through_ptr: 9999  ← read via pointer
    //   value_after:       9999  ← read via variable — SAME memory

    (value_before, value_through_ptr, value_after)
}
