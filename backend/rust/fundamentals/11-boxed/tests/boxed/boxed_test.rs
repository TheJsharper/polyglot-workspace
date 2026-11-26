use boxed_demo_crate::*;

#[test]
fn test_box_new_demo() {
    let (box_size, val_size) = box_new_demo();
    assert_eq!(box_size, 8);
    assert_eq!(val_size, 1);
}

#[test]
fn test_box_deref_demo() {
    let val = box_deref_demo();
    assert_eq!(val, 42);
}

#[test]
fn test_box_heap_address_demo() {
    let addr = box_heap_address_demo();
    assert!(addr > 0);
}

#[test]
fn test_box_mutate_demo() {
    let val = box_mutate_demo();
    assert_eq!(val, 200);
}

#[test]
fn test_box_recursive_list_demo() {
    let list = box_recursive_list_demo();
    assert_eq!(list, List::Cons(1, Box::new(List::Cons(2, Box::new(List::Nil)))));
}

#[test]
fn test_box_into_inner_demo() {
    let owned = box_into_inner_demo();
    assert_eq!(owned, "owned on heap");
}

#[test]
fn test_box_into_raw_demo() {
    let (addr, is_non_null) = box_into_raw_demo();
    assert!(addr > 0);
    assert!(is_non_null);
}

#[test]
fn test_box_from_raw_demo() {
    let val = box_from_raw_demo();
    assert_eq!(val, 999);
}

#[test]
fn test_box_leak_demo() {
    let leaked = box_leak_demo();
    assert_eq!(*leaked, 100);
}

#[test]
fn test_box_try_new_demo() {
    let ok = box_try_new_demo();
    assert!(ok);
}

#[test]
fn test_box_pin_demo() {
    let stable = box_pin_demo();
    assert!(stable);
}

#[test]
fn test_box_downcast_demo() {
    let (success, value) = box_downcast_demo();
    assert!(success);
    assert_eq!(value, 42);
}

#[test]
fn test_box_clone_demo() {
    let (original, cloned) = box_clone_demo();
    assert_eq!(*original, 100);
    assert_eq!(*cloned, 100);
}

#[test]
fn test_box_from_slice_demo() {
    let boxed = box_from_slice_demo();
    assert_eq!(&*boxed, &[1, 2, 3, 4, 5]);
}

#[test]
fn test_box_from_string_demo() {
    let boxed = box_from_string_demo();
    assert_eq!(&*boxed, "hello");
}

#[test]
fn test_box_from_vec_demo() {
    let boxed = box_from_vec_demo();
    assert_eq!(&*boxed, &[10, 20, 30]);
}

#[test]
fn test_box_memory_layout_demo() {
    let (box_size, value_size) = box_memory_layout_demo();
    assert_eq!(box_size, 8);
    assert_eq!(value_size, 8);
}

#[test]
fn test_boxed_module_description() {
    let desc = boxed_module_description();
    assert!(desc.contains("Box"));
    assert!(desc.contains("heap"));
    assert!(desc.contains("into_raw"));
}
