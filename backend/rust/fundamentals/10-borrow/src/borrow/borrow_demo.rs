use std::borrow::{Borrow, BorrowMut, Cow, ToOwned};

// ─────────────────────────────────────────────
// ToOwned — generalization of Clone to borrowed data
// ─────────────────────────────────────────────

/// `ToOwned` — creates an owned version from a borrowed one.
///
/// `str` implements `ToOwned<Owned = String>`.
/// `Path` implements `ToOwned<Owned = PathBuf>`.
/// `&[T]` implements `ToOwned<Owned = Vec<T>>` (when T: Clone).
///
/// Returns the owned String created from a borrowed &str.
pub fn to_owned_demo() -> String {
    let borrowed: &str = "hello from borrowed";
    let owned: String = borrowed.to_owned();

    owned
}

/// `ToOwned` on slice — `&[i32]` → `Vec<i32>`.
///
/// Returns the Vec created from a borrowed slice.
pub fn to_owned_slice_demo() -> Vec<i32> {
    let borrowed: &[i32] = &[1, 2, 3, 4, 5];
    let owned: Vec<i32> = borrowed.to_owned();

    owned
}

// ─────────────────────────────────────────────
// Borrow / BorrowMut — generic borrowing traits
// ─────────────────────────────────────────────

/// `Borrow` trait — allows generic code to accept both owned and borrowed forms.
///
/// `String` implements `Borrow<str>`, so `&String` can be used where `&str` is expected.
/// This function demonstrates the trait by accepting anything that borrows as `str`.
/// Returns the length of the borrowed content.
pub fn borrow_trait_demo(key: impl Borrow<str>) -> usize {
    let borrowed: &str = key.borrow();
    borrowed.len()
}

/// `BorrowMut` trait — mutable version of `Borrow`.
///
/// Takes a mutable reference to something that implements `BorrowMut<str>`,
/// mutates it to uppercase, and returns the length.
pub fn borrow_mut_trait_demo(value: &mut impl BorrowMut<str>) -> usize {
    let borrowed: &mut str = value.borrow_mut();
    borrowed.make_ascii_uppercase();
    borrowed.len()
}

/// Demonstrates `HashMap`-style lookup using `Borrow`.
///
/// Returns the number of successful lookups when mixing `String` keys and `&str` queries.
pub fn borrow_hashmap_style_demo() -> usize {
    use std::collections::HashMap;

    let mut map: HashMap<String, i32> = HashMap::new();
    map.insert("apple".to_string(), 1);
    map.insert("banana".to_string(), 2);
    map.insert("cherry".to_string(), 3);

    // Look up with &str (Borrow<str>) even though keys are String
    let mut found = 0;
    found += map.get("apple").is_some() as usize;
    found += map.get("banana").is_some() as usize;
    found += map.get("cherry").is_some() as usize;
    found += map.get("dragonfruit").is_some() as usize; // not in map

    found // 3
}

// ─────────────────────────────────────────────
// Cow — Clone-on-Write smart pointer
// ─────────────────────────────────────────────

/// `Cow::Borrowed` — zero-copy when no mutation is needed.
///
/// Returns (is_borrowed, content_length).
pub fn cow_borrowed_demo() -> (bool, usize) {
    let borrowed: &str = "immutable data";
    let cow: Cow<str> = Cow::Borrowed(borrowed);

    let is_borrowed = matches!(cow, Cow::Borrowed(_));
    let len = cow.len();

    (is_borrowed, len)
}

/// `Cow::Owned` — owns the data when created from an owned value.
///
/// Returns (is_owned, content).
pub fn cow_owned_demo() -> (bool, String) {
    let owned: String = "owned data".to_string();
    let cow: Cow<str> = Cow::Owned(owned.clone());

    let is_owned = matches!(cow, Cow::Owned(_));

    (is_owned, cow.into_owned())
}

/// `Cow` clone-on-write — mutates only when needed.
///
/// Starting with borrowed data, calling `to_mut()` clones only if mutation happens.
/// Returns (was_borrowed_before, is_owned_after_mutate, content).
pub fn cow_clone_on_write_demo() -> (bool, bool, String) {
    let borrowed: &str = "hello world";
    let mut cow: Cow<str> = Cow::Borrowed(borrowed);

    let was_borrowed = matches!(cow, Cow::Borrowed(_));

    // This triggers the clone — Borrowed → Owned
    let mutable: &mut str = cow.to_mut();
    mutable.make_ascii_uppercase();

    let is_owned_after = matches!(cow, Cow::Owned(_));

    (was_borrowed, is_owned_after, cow.into_owned())
}

/// `Cow` with `String` input — already owned, no clone on `to_mut()`.
///
/// Returns (was_owned_before, is_owned_after, content).
pub fn cow_owned_no_clone_demo() -> (bool, bool, String) {
    let owned: String = "already owned".to_string();
    let mut cow: Cow<str> = Cow::Owned(owned);

    let was_owned = matches!(cow, Cow::Owned(_));

    // to_mut() on Owned returns the same data — no clone
    let mutable: &mut str = cow.to_mut();
    mutable.make_ascii_uppercase();

    let is_owned_after = matches!(cow, Cow::Owned(_));

    (was_owned, is_owned_after, cow.into_owned())
}

/// `Cow` as function return — accept borrowed or owned, return Cow.
///
/// If the input contains uppercase, returns borrowed (zero-copy).
/// If it needs modification, returns owned.
/// Returns (is_borrowed, content).
pub fn cow_function_return_demo(input: &str) -> (bool, String) {
    let cow: Cow<str> = if input.chars().all(|c| c.is_lowercase() || c == ' ') {
        Cow::Borrowed(input)
    } else {
        Cow::Owned(input.to_lowercase())
    };

    let is_borrowed = matches!(cow, Cow::Borrowed(_));

    (is_borrowed, cow.into_owned())
}

/// `Cow<[T]>` — clone-on-write for slices.
///
/// Returns (was_borrowed, is_owned_after, content).
pub fn cow_slice_demo() -> (bool, bool, Vec<i32>) {
    let borrowed: &[i32] = &[1, 2, 3];
    let mut cow: Cow<[i32]> = Cow::Borrowed(borrowed);

    let was_borrowed = matches!(cow, Cow::Borrowed(_));

    // Mutate — triggers clone from &[i32] to Vec<i32>
    let mutable: &mut [i32] = cow.to_mut();
    mutable[0] = 100;

    let is_owned_after = matches!(cow, Cow::Owned(_));

    (was_borrowed, is_owned_after, cow.into_owned())
}

/// `Cow` with `Deref` — read access without cloning.
///
/// You can read through `Cow` without triggering a clone.
/// Returns (length, first_char).
pub fn cow_deref_demo() -> (usize, char) {
    let borrowed: &str = "deref access";
    let cow: Cow<str> = Cow::Borrowed(borrowed);

    // Deref — no clone, just reads the data
    let len = cow.len();
    let first = cow.chars().next().unwrap();

    (len, first)
}

/// `Cow` lifetime — borrowed variant tied to a lifetime.
///
/// The `Cow<'a, str>` borrows from data with lifetime `'a`.
/// Returns the content (owned after into_owned).
pub fn cow_lifetime_demo<'a>(data: &'a str) -> String {
    let cow: Cow<'a, str> = Cow::Borrowed(data);
    cow.into_owned()
}

/// `Cow` comparison — when to use Borrowed vs Owned.
///
/// Returns (borrowed_efficient, owned_when_needed) showing the pattern.
pub fn cow_comparison_demo() -> (Cow<'static, str>, Cow<'static, str>) {
    // Borrowed: zero-copy, efficient when no mutation
    let efficient: Cow<str> = Cow::Borrowed("no changes needed");

    // Owned: necessary when you must mutate or own the data
    let when_needed: Cow<str> = Cow::Owned(format!("computed: {}", 42));

    (efficient, when_needed)
}

/// Returns a description of the `std::borrow` module.
pub fn borrow_module_description() -> &'static str {
    "std::borrow provides Cow (clone-on-write), Borrow, BorrowMut, and ToOwned traits"
}
