use std::boxed::Box;
use std::mem;
use std::pin::Pin;

pub fn box_new_demo() -> (usize, usize) {
    let val: u8 = 5;
    let boxed: Box<u8> = Box::new(val);

    let size_of_box = mem::size_of_val(&boxed);
    let size_of_val = mem::size_of_val(&*boxed);

    (size_of_box, size_of_val)
}

pub fn box_deref_demo() -> u8 {
    let boxed: Box<u8> = Box::new(42);
    let val: u8 = *boxed;

    val
}

pub fn box_heap_address_demo() -> usize {
    let boxed: Box<i32> = Box::new(100);

    let ptr: *const i32 = &*boxed;

    ptr as usize
}

pub fn box_mutate_demo() -> i32 {
    let mut boxed: Box<i32> = Box::new(10);

    *boxed = 200;

    *boxed
}

#[derive(Debug, PartialEq)]
pub enum List<T> {
    Cons(T, Box<List<T>>),
    Nil,
}

pub fn box_recursive_list_demo() -> List<i32> {
    let list: List<i32> = List::Cons(1, Box::new(List::Cons(2, Box::new(List::Nil))));

    list
}

pub fn box_into_inner_demo() -> String {
    let boxed: Box<String> = Box::new(String::from("owned on heap"));

    let owned: String = *boxed;

    owned
}

pub fn box_into_raw_demo() -> (usize, bool) {
    let boxed: Box<i32> = Box::new(777);

    let raw: *mut i32 = Box::into_raw(boxed);

    let is_non_null = !raw.is_null();

    unsafe {
        let _reclaimed: Box<i32> = Box::from_raw(raw);
    }

    (raw as usize, is_non_null)
}

pub fn box_from_raw_demo() -> i32 {
    let boxed: Box<i32> = Box::new(999);
    let raw: *mut i32 = Box::into_raw(boxed);

    unsafe {
        let reclaimed: Box<i32> = Box::from_raw(raw);
        *reclaimed
    }
}

pub fn box_leak_demo() -> &'static mut i32 {
    let boxed: Box<i32> = Box::new(42);

    let leaked: &'static mut i32 = Box::leak(boxed);

    *leaked = 100;

    leaked
}

pub fn box_try_new_demo() -> bool {
    let result: Result<Box<[u8; 1024]>, ()> = Ok(Box::new([0u8; 1024]));

    result.is_ok()
}

pub fn box_pin_demo() -> bool {
    let pinned: Pin<Box<i32>> = Box::pin(42);

    let value = *pinned;

    value == 42
}

pub fn box_downcast_demo() -> (bool, i32) {
    let boxed: Box<dyn std::any::Any> = Box::new(42i32);

    let downcast_result: Result<Box<i32>, _> = boxed.downcast();

    let is_success = downcast_result.is_ok();
    let value = if is_success { *downcast_result.unwrap() } else { 0 };

    (is_success, value)
}

pub fn box_clone_demo() -> (Box<i32>, Box<i32>) {
    let original: Box<i32> = Box::new(100);

    let cloned: Box<i32> = original.clone();

    (original, cloned)
}

pub fn box_from_slice_demo() -> Box<[i32]> {
    let slice: &[i32] = &[1, 2, 3, 4, 5];

    let boxed: Box<[i32]> = Box::from(slice);

    boxed
}

pub fn box_from_string_demo() -> Box<str> {
    let string: String = String::from("hello");

    let boxed: Box<str> = string.into_boxed_str();

    boxed
}

pub fn box_from_vec_demo() -> Box<[u8]> {
    let vec: Vec<u8> = vec![10, 20, 30];

    let boxed: Box<[u8]> = vec.into_boxed_slice();

    boxed
}

pub fn box_memory_layout_demo() -> (usize, usize) {
    let _boxed: Box<u64> = Box::new(0);

    let box_size = mem::size_of::<Box<u64>>();
    let value_size = mem::size_of::<u64>();

    (box_size, value_size)
}

pub fn boxed_module_description() -> &'static str {
    "std::boxed provides Box<T> for heap allocation, into_raw/from_raw, leak, pin, and try_new"
}
