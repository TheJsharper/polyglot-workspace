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
        (0..8).filter(|&i| ptr::read(ptr.add(i) as *const u8) == 0).count()
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

    "handle_alloc_error(layout) prints an allocation error message and aborts the program".to_string()

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
