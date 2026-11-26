use alloc_demo_crate::*;

#[test]
fn test_alloc_demo_writes_bytes() {

    let (first_byte, layout) = alloc_demo();

    assert_eq!(first_byte, 0xAA);
    assert_eq!(layout.size(), 16);
    assert_eq!(layout.align(), 8);

}

#[test]
fn test_alloc_zeroed_all_zeros() {

    let zero_count = alloc_zeroed_demo();

    assert_eq!(zero_count, 8);

}

#[test]
fn test_dealloc_demo_completes() {

    assert!(dealloc_demo());

}

#[test]
fn test_handle_alloc_error_description() {

    let description = handle_alloc_error_description();

    assert!(description.contains("handle_alloc_error"));
    assert!(description.contains("abort"));

}

#[test]
fn test_realloc_demo_preserves_value() {

    let (preserved_value, new_capacity) = realloc_demo();

    assert_eq!(preserved_value, 42);
    assert_eq!(new_capacity, 16);

}

#[test]
fn test_layout_valid_demo() {

    let (size, align) = layout_valid_demo();

    assert_eq!(size, 128);
    assert_eq!(align, 16);

}

#[test]
fn test_layout_invalid_align_demo() {

    assert!(layout_invalid_align_demo());

}

#[test]
fn test_layout_zero_size_demo() {

    assert!(layout_zero_size_demo());

}

#[test]
fn test_alloc_struct_layout_demo() {

    let header = alloc_struct_layout_demo();

    assert_eq!(header, 0xDE);

}

#[test]
fn test_alloc_dealloc_cycle_demo() {

    let success_count = alloc_dealloc_cycle_demo();

    assert_eq!(success_count, 10);

}

#[test]
fn test_realloc_shrink_demo() {

    let new_size = realloc_shrink_demo();

    assert_eq!(new_size, 8);

}

#[test]
fn test_alloc_max_align_demo() {

    let alignment = alloc_max_align_demo();

    assert_eq!(alignment, 64);

}

#[test]
fn test_alloc_module_description() {

    let description = alloc_module_description();

    assert!(description.contains("std::alloc"));
    assert!(description.contains("alloc_zeroed"));
    assert!(description.contains("dealloc"));
    assert!(description.contains("realloc"));

}
