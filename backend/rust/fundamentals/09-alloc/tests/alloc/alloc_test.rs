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

// ─────────────────────────────────────────────
// Pointer & reference operator tests
// ─────────────────────────────────────────────

#[test]
fn test_borrow_immutable_demo() {

    let (first, second) = borrow_immutable_demo();

    assert_eq!(first, 42);
    assert_eq!(second, 42);

}

#[test]
fn test_borrow_mutable_demo() {

    let result = borrow_mutable_demo();

    assert_eq!(result, 100);

}

#[test]
fn test_raw_const_pointer_demo() {

    let value = raw_const_pointer_demo();

    assert_eq!(value, 0xDE);

}

#[test]
fn test_raw_mut_pointer_demo() {

    let value = raw_mut_pointer_demo();

    assert_eq!(value, 0xAD);

}

#[test]
fn test_deref_all_types_demo() {

    let (imm, mut_ref, raw_const, raw_mut) = deref_all_types_demo();

    assert_eq!(imm, 42);
    assert_eq!(mut_ref, 100);
    assert_eq!(raw_const, 100);
    assert_eq!(raw_mut, 200);

}

#[test]
fn test_borrow_smart_pointers_demo() {

    let (box_ref, box_inner, rc_ref, rc_inner) = borrow_smart_pointers_demo();

    assert_eq!(box_ref, 42);
    assert_eq!(box_inner, 42);
    assert_eq!(rc_ref, 99);
    assert_eq!(rc_inner, 99);

}

#[test]
fn test_exclusive_mut_borrow_demo() {

    let result = exclusive_mut_borrow_demo();

    assert_eq!(result, 111);

}

#[test]
fn test_raw_pointer_arithmetic_demo() {

    let (at_0, at_1, at_2) = raw_pointer_arithmetic_demo();

    assert_eq!(at_0, 0xAA);
    assert_eq!(at_1, 0xBB);
    assert_eq!(at_2, 0xCC);

}

#[test]
fn test_null_pointer_check_demo() {

    assert!(null_pointer_check_demo());

}

#[test]
fn test_coercion_chain_demo() {

    let value = coercion_chain_demo();

    assert_eq!(value, 777);

}

#[test]
fn test_type_annotations_demo() {

    let (imm_ref, mut_ref, raw_const, raw_mut) = type_annotations_demo();

    assert_eq!(imm_ref, 10);
    assert_eq!(mut_ref, 200);
    assert_eq!(raw_const, 10);
    assert_eq!(raw_mut, 300);

}

#[test]
fn test_mut_vs_pointer_mut_demo() {

    let (through_const, through_mut) = mut_vs_pointer_mut_demo();

    assert_eq!(through_const, 100);
    assert_eq!(through_mut, 2000);

}
