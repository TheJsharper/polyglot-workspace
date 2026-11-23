/// Ownership basics: move, clone, copy, and why some types behave differently.

// --- Move Semantics ---

/// Demonstrates ownership transfer: `b` takes ownership of the String, `a` is invalidated.
///
/// ```compile_fail
/// let a = String::from("Hello");
/// let b = a;
/// println!("{}", a); // Error: value borrowed after move
/// ```
///
/// The correct version:
/// ```
/// let a = String::from("Hello");
/// let b = a;
/// // println!("{}", a); // Error: a no longer owns the value
/// println!("{}", b); // Ok: b now owns the value
/// ```
pub fn demonstrate_move() -> String {
    let a = String::from("Hello");
    let b = a; // ownership moves from a to b
    // a is no longer valid — using it would be a compile error
    b // b owns the value and returns it
}

/// Demonstrates that primitive types (like i32) implement the `Copy` trait.
/// Assignment copies the value — both variables remain valid.
///
/// This is the w3schools example:
/// ```
/// let a = 5;
/// let b = a;
/// println!("a = {}", a);  // Works
/// println!("b = {}", b);  // Works
/// ```
///
/// Unlike `String`, integers are stored on the stack and are cheap to copy.
/// Rust automatically implements `Copy` for: i8, i16, i32, i64, i128,
/// u8, u16, u32, u64, u128, f32, f64, bool, char, and tuples of Copy types.
pub fn demonstrate_copy_type() -> (i32, i32) {
    let a = 5;
    let b = a; // copies the value — both a and b are valid
    (a, b) // no move occurred
}

/// Demonstrates that `bool` is also `Copy`.
pub fn demonstrate_copy_bool() -> (bool, bool) {
    let a = true;
    let b = a; // copies, doesn't move
    (a, b)
}

/// Demonstrates that `char` is also `Copy`.
pub fn demonstrate_copy_char() -> (char, char) {
    let a = 'A';
    let b = a; // copies, doesn't move
    (a, b)
}

// --- Ownership + Functions ---

/// Returns the length without taking ownership — uses `&str` (borrow).
pub fn string_length(s: &str) -> usize {
    s.len()
}

/// Takes ownership and returns it back — caller retains access.
pub fn give_back(s: String) -> String {
    s
}

/// Takes ownership and does NOT return it — caller loses access.
pub fn consume_and_drop(s: String) -> usize {
    s.len() // s is dropped here
}

// --- Ownership Rules ---

/// Demonstrates: only one owner at a time.
/// When the scope ends, `s` is dropped and memory is freed.
pub fn demonstrate_scope_drop() -> usize {
    let length = {
        let s = String::from("Hello");
        s.len() // s is still valid here
    }; // s goes out of scope and is dropped
    length // we kept the computed value
}

/// Demonstrates: mutable borrow requires exclusive access.
/// While `r` exists, `s` cannot be used directly.
pub fn demonstrate_mutable_borrow_exclusive() -> String {
    let mut s = String::from("Hello");
    {
        let r = &mut s;
        r.push_str(", world!");
    } // mutable borrow ends here
    s // s is usable again
}

/// Demonstrates: multiple immutable borrows are allowed simultaneously.
pub fn demonstrate_multiple_immutable_borrows() -> (usize, usize) {
    let s = String::from("Hello, world!");
    let r1 = &s;
    let r2 = &s;
    let r3 = &s;
    (r1.len(), r2.len() + r3.len()) // all three borrows coexist
}
