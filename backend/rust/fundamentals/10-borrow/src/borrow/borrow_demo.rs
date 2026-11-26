use std::borrow::{Borrow, BorrowMut, Cow, ToOwned};

pub fn to_owned_demo() -> String {
    let borrowed: &str = "hello from borrowed";
    let owned: String = borrowed.to_owned();

    owned
}

pub fn to_owned_slice_demo() -> Vec<i32> {
    let borrowed: &[i32] = &[1, 2, 3, 4, 5];
    let owned: Vec<i32> = borrowed.to_owned();

    owned
}

pub fn borrow_trait_demo(key: impl Borrow<str>) -> usize {
    let borrowed: &str = key.borrow();
    borrowed.len()
}

pub fn borrow_mut_trait_demo(value: &mut impl BorrowMut<str>) -> usize {
    let borrowed: &mut str = value.borrow_mut();
    borrowed.make_ascii_uppercase();
    borrowed.len()
}

pub fn borrow_hashmap_style_demo() -> usize {
    use std::collections::HashMap;

    let mut map: HashMap<String, i32> = HashMap::new();
    map.insert("apple".to_string(), 1);
    map.insert("banana".to_string(), 2);
    map.insert("cherry".to_string(), 3);

    let mut found = 0;
    found += map.get("apple").is_some() as usize;
    found += map.get("banana").is_some() as usize;
    found += map.get("cherry").is_some() as usize;
    found += map.get("dragonfruit").is_some() as usize;

    found
}

pub fn cow_borrowed_demo() -> (bool, usize) {
    let borrowed: &str = "immutable data";
    let cow: Cow<str> = Cow::Borrowed(borrowed);

    let is_borrowed = matches!(cow, Cow::Borrowed(_));
    let len = cow.len();

    (is_borrowed, len)
}

pub fn cow_owned_demo() -> (bool, String) {
    let owned: String = "owned data".to_string();
    let cow: Cow<str> = Cow::Owned(owned.clone());

    let is_owned = matches!(cow, Cow::Owned(_));

    (is_owned, cow.into_owned())
}

pub fn cow_clone_on_write_demo() -> (bool, bool, String) {
    let borrowed: &str = "hello world";
    let mut cow: Cow<str> = Cow::Borrowed(borrowed);

    let was_borrowed = matches!(cow, Cow::Borrowed(_));

    let mutable: &mut str = cow.to_mut();
    mutable.make_ascii_uppercase();

    let is_owned_after = matches!(cow, Cow::Owned(_));

    (was_borrowed, is_owned_after, cow.into_owned())
}

pub fn cow_owned_no_clone_demo() -> (bool, bool, String) {
    let owned: String = "already owned".to_string();
    let mut cow: Cow<str> = Cow::Owned(owned);

    let was_owned = matches!(cow, Cow::Owned(_));

    let mutable: &mut str = cow.to_mut();
    mutable.make_ascii_uppercase();

    let is_owned_after = matches!(cow, Cow::Owned(_));

    (was_owned, is_owned_after, cow.into_owned())
}

pub fn cow_function_return_demo(input: &str) -> (bool, String) {
    let cow: Cow<str> = if input.chars().all(|c| c.is_lowercase() || c == ' ') {
        Cow::Borrowed(input)
    } else {
        Cow::Owned(input.to_lowercase())
    };

    let is_borrowed = matches!(cow, Cow::Borrowed(_));

    (is_borrowed, cow.into_owned())
}

pub fn cow_slice_demo() -> (bool, bool, Vec<i32>) {
    let borrowed: &[i32] = &[1, 2, 3];
    let mut cow: Cow<[i32]> = Cow::Borrowed(borrowed);

    let was_borrowed = matches!(cow, Cow::Borrowed(_));

    let mutable: &mut [i32] = cow.to_mut();
    mutable[0] = 100;

    let is_owned_after = matches!(cow, Cow::Owned(_));

    (was_borrowed, is_owned_after, cow.into_owned())
}

pub fn cow_deref_demo() -> (usize, char) {
    let borrowed: &str = "deref access";
    let cow: Cow<str> = Cow::Borrowed(borrowed);

    let len = cow.len();
    let first = cow.chars().next().unwrap();

    (len, first)
}

pub fn cow_lifetime_demo<'a>(data: &'a str) -> String {
    let cow: Cow<'a, str> = Cow::Borrowed(data);
    cow.into_owned()
}

pub fn cow_comparison_demo() -> (Cow<'static, str>, Cow<'static, str>) {
    let efficient: Cow<str> = Cow::Borrowed("no changes needed");
    let when_needed: Cow<str> = Cow::Owned(format!("computed: {}", 42));

    (efficient, when_needed)
}

pub fn borrow_module_description() -> &'static str {
    "std::borrow provides Cow (clone-on-write), Borrow, BorrowMut, and ToOwned traits"
}
