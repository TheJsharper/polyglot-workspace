pub fn demonstrate_basic_string() -> &'static str {
    "Hello, world!"
}

pub fn demonstrate_basic_from_str() -> String {
    "Hello, world!".to_string()
}

pub fn demostrate_basic_from_method() -> String {
    String::from("Hello, world!")
}

pub fn demonstrate_basic_string_with_format() -> String {
    format!("Hello, world!")
}

pub fn demonstrate_basic_string_with_argument_and_format(name: &str) -> String {
    format!("Hello, {}!", name)
}

pub fn demonstrate_basic_string_with_multiple_arguments_and_format(name: &str, age: u32) -> String {
    format!("Hello, {}! You are {} years old.", name, age)
}

pub fn demonstrate_basic_string_with_named_arguments_and_format(name: &str, age: u32) -> String {
    format!(
        "Hello, {name}! You are {age} years old.",
        name = name,
        age = age
    )
}

pub fn demostrate_basic_mutate_string() -> String {
    let mut s = String::from("Hello");
    s.push_str(", world!");
    s
}
pub fn demonstrate_basic_concatenate_strings() -> String {
    let s1 = String::from("Hello");
    let s2 = String::from(", world!");
    let s3 = s1 + &s2;
    s3
}
pub fn demonstrate_basic_concatenate_strings_with_format() -> String {
    let s1 = String::from("Hello");
    let s2 = String::from(", world!");
    format!("{}{}", s1, s2)
}

// --- Basic String Methods ---

pub fn demonstrate_string_length(s: &str) -> usize {
    s.len()
}

pub fn demonstrate_string_is_empty(s: &str) -> bool {
    s.is_empty()
}

pub fn demonstrate_string_contains(s: &str, substring: &str) -> bool {
    s.contains(substring)
}

pub fn demonstrate_string_starts_with(s: &str, prefix: &str) -> bool {
    s.starts_with(prefix)
}

pub fn demonstrate_string_ends_with(s: &str, suffix: &str) -> bool {
    s.ends_with(suffix)
}

pub fn demonstrate_string_replace(s: &str, from: &str, to: &str) -> String {
    s.replace(from, to)
}

pub fn demonstrate_string_to_uppercase(s: &str) -> String {
    s.to_uppercase()
}

pub fn demonstrate_string_to_lowercase(s: &str) -> String {
    s.to_lowercase()
}

pub fn demonstrate_string_trim(s: &str) -> &str {
    s.trim()
}

pub fn demonstrate_string_split<'a>(s: &'a str, delimiter: &str) -> Vec<&'a str> {
    s.split(delimiter).collect()
}

pub fn demonstrate_string_find(s: &str, substring: &str) -> Option<usize> {
    s.find(substring)
}

pub fn demonstrate_string_slice(s: &str, start: usize, end: usize) -> &str {
    &s[start..end]
}

pub fn demonstrate_string_push_char(s: &mut String, c: char) {
    s.push(c);
}

pub fn demonstrate_string_clear(s: &mut String) {
    s.clear();
}

pub fn demonstrate_string_chars(s: &str) -> Vec<char> {
    s.chars().collect()
}

pub fn demonstrate_string_repeat(s: &str, n: usize) -> String {
    s.repeat(n)
}
