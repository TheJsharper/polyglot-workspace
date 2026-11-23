/// Advanced ownership: `Clone`, `Copy`, references, `Box`, `Rc`, and ownership syntax patterns.

// --- Clone vs Copy Deep Dive ---

/// Demonstrates `.clone()` — the explicit way to duplicate a non-Copy type.
/// Both `a` and `b` remain valid because `clone` creates an independent copy on the heap.
pub fn demonstrate_clone() -> (String, String) {
    let a = String::from("Hello");
    let b = a.clone(); // deep copy — allocates new heap memory
    (a, b) // both are valid and independent
}

/// Demonstrates that `.clone()` works on nested structures — clones recursively.
pub fn demonstrate_clone_vec() -> (Vec<i32>, Vec<i32>) {
    let a = vec![1, 2, 3];
    let b = a.clone(); // copies the entire Vec (pointer + contents)
    (a, b)
}

// --- Reference Types ---

/// Immutable reference to a `String`. The function borrows, doesn't own.
pub fn reference_string(s: &String) -> usize {
    s.len()
}

/// Immutable reference to a `&str` (string slice). More flexible than `&String`.
pub fn reference_str(s: &str) -> usize {
    s.len()
}

/// Mutable reference — exclusive write access.
pub fn mutate_via_reference(s: &mut String) {
    s.push_str(" — mutated!");
}

/// Demonstrates that you can't have a mutable and immutable reference at the same time.
/// This function shows the correct pattern: limit the mutable borrow scope.
pub fn mixed_borrowing_pattern() -> (String, usize) {
    let mut s = String::from("Hello");
    let length;
    {
        let r = &mut s;
        r.push_str(", world!");
    } // mutable borrow ends here
    length = s.len(); // now we can read
    (s, length)
}

// --- Box<T> — Heap Allocation with Ownership ---

/// `Box<T>` owns heap-allocated data. It moves like `String`, doesn't copy.
pub fn demonstrate_box() -> Box<i32> {
    let b = Box::new(42);
    b // ownership of the heap allocation moves out
}

/// Demonstrates boxing a `String` — double indirection (Box → String → heap).
pub fn demonstrate_box_string() -> Box<String> {
    let b = Box::new(String::from("Hello from the heap"));
    b
}

/// Takes a `Box<i32>`, dereferences it, and returns the value.
/// The Box is consumed (dropped) — the heap memory is freed.
pub fn unbox_value(b: Box<i32>) -> i32 {
    *b // dereference — moves the i32 out, drops the Box
}

// --- Rc<T> — Shared Ownership ---

/// `Rc<T>` enables multiple owners via reference counting.
/// Cloning an `Rc` increments the count — no deep copy of the data.
pub fn demonstrate_rc() -> (std::rc::Rc<String>, std::rc::Rc<String>) {
    let a = std::rc::Rc::new(String::from("shared"));
    let b = a.clone(); // increments ref count, doesn't copy the String
    (a, b) // both point to the same heap data
}

/// Returns the reference count of an `Rc<String>`.
pub fn rc_strong_count(rc: &std::rc::Rc<String>) -> usize {
    std::rc::Rc::strong_count(rc)
}

// --- Ownership in Structs ---

/// A struct that owns its data.
pub struct OwnedData {
    pub name: String,
    pub values: Vec<i32>,
}

impl OwnedData {
    pub fn new(name: &str, values: Vec<i32>) -> Self {
        OwnedData {
            name: name.to_string(), // borrows &str, creates owned String
            values,                  // takes ownership of the Vec
        }
    }
}

/// Creates an owned struct, returns it.
pub fn demonstrate_owned_struct() -> OwnedData {
    OwnedData::new("example", vec![1, 2, 3])
}

/// A struct that borrows data (uses references). Lifetime annotation required.
pub struct BorrowedData<'a> {
    pub name: &'a str,
    pub values: &'a [i32],
}

impl<'a> BorrowedData<'a> {
    pub fn new(name: &'a str, values: &'a [i32]) -> Self {
        BorrowedData { name, values }
    }
}

/// Creates a borrowed struct — no allocation, just references.
pub fn demonstrate_borrowed_struct<'a>() -> BorrowedData<'a> {
    // We can't return BorrowedData with local `values` because it would dangle.
    // Instead, we demonstrate the pattern with static data:
    BorrowedData::new("static", &[1, 2, 3])
}

// --- Ownership Transfer Patterns ---

/// Takes ownership, transforms, returns new value (input is consumed).
pub fn transform_consume(s: String) -> String {
    s.to_uppercase() // s is dropped after this
}

/// Takes ownership, mutates in place, returns (no reallocation).
pub fn transform_mutate_in_place(mut s: String) -> String {
    s.push_str(" — appended");
    s
}

/// Borrows, returns a new owned value (input preserved).
pub fn transform_borrow(s: &str) -> String {
    s.to_uppercase() // returns a new String, s is untouched
}

// --- Deref Coercion ---

/// Demonstrates that `&String` automatically coerces to `&str`.
/// The function takes `&str`, but you can pass `&String`, `&str`, or `Box<str>`.
pub fn deref_coercion_example(s: &str) -> usize {
    s.len()
}

/// Shows all three ways to call a `&str` function.
pub fn demonstrate_deref_coercion() -> (usize, usize, usize) {
    let string = String::from("Hello");
    let str_slice: &str = "Hello";
    let boxed: Box<str> = String::from("Hello").into_boxed_str();

    let from_string = deref_coercion_example(&string);   // &String → &str
    let from_str = deref_coercion_example(str_slice);     // &str → &str
    let from_box = deref_coercion_example(&boxed);        // &Box<str> → &str

    (from_string, from_str, from_box)
}

// --- Move vs Copy Type Registry ---

/// Returns a tuple showing which types are Copy and which are Move.
/// All Copy types remain valid after assignment; Move types don't.
pub fn copy_vs_move_summary() -> (
    i32, i32,       // Copy: both valid
    f64, f64,       // Copy: both valid
    bool, bool,     // Copy: both valid
    char, char,     // Copy: both valid
    (i32, bool),    // Copy: tuple of Copy types
) {
    let a = 42; let b = a;
    let c = 3.14; let d = c;
    let e = true; let f = e;
    let g = 'Z'; let h = g;
    let i = (1, true); let _j = i;
    (a, b, c, d, e, f, g, h, i) // all still valid — all Copy
}
