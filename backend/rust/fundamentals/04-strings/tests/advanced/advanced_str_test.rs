#[path = "../../src/advanced/advanced_str.rs"]
mod advanced_str;

// --- Splitting Variants ---

#[test]
fn test_demonstrate_split_whitespace() {
    assert_eq!(
        advanced_str::demonstrate_split_whitespace("  hello   world  foo "),
        vec!["hello", "world", "foo"]
    );
    assert_eq!(advanced_str::demonstrate_split_whitespace(""), vec![] as Vec<&str>);
}

#[test]
fn test_demonstrate_splitn() {
    assert_eq!(
        advanced_str::demonstrate_splitn("a,b,c,d", ",", 3),
        vec!["a", "b", "c,d"]
    );
    assert_eq!(
        advanced_str::demonstrate_splitn("a,b,c", ",", 1),
        vec!["a,b,c"]
    );
}

#[test]
fn test_demonstrate_rsplit() {
    assert_eq!(
        advanced_str::demonstrate_rsplit("a.b.c", "."),
        vec!["c", "b", "a"]
    );
}

#[test]
fn test_demonstrate_lines() {
    assert_eq!(
        advanced_str::demonstrate_lines("line1\nline2\r\nline3"),
        vec!["line1", "line2", "line3"]
    );
}

#[test]
fn test_demonstrate_match_indices() {
    assert_eq!(
        advanced_str::demonstrate_match_indices("banana", "an"),
        vec![(1, "an"), (3, "an")]
    );
    assert_eq!(
        advanced_str::demonstrate_match_indices("hello", "z"),
        vec![] as Vec<(usize, &str)>
    );
}

#[test]
fn test_demonstrate_rfind() {
    assert_eq!(advanced_str::demonstrate_rfind("banana", "an"), Some(3));
    assert_eq!(advanced_str::demonstrate_rfind("hello", "z"), None);
}

// --- Trimming Variants ---

#[test]
fn test_demonstrate_trim_start() {
    assert_eq!(advanced_str::demonstrate_trim_start("  hello  "), "hello  ");
}

#[test]
fn test_demonstrate_trim_end() {
    assert_eq!(advanced_str::demonstrate_trim_end("  hello  "), "  hello");
}

#[test]
fn test_demonstrate_trim_matches() {
    assert_eq!(advanced_str::demonstrate_trim_matches("***hello***", '*'), "hello");
}

// --- Parsing ---

#[test]
fn test_demonstrate_parse_i32() {
    assert_eq!(advanced_str::demonstrate_parse_i32("42"), Ok(42));
    assert!(advanced_str::demonstrate_parse_i32("not_a_number").is_err());
}

#[test]
fn test_demonstrate_parse_f64() {
    assert_eq!(advanced_str::demonstrate_parse_f64("3.14"), Ok(3.14));
    assert!(advanced_str::demonstrate_parse_f64("nan").is_ok()); // "nan" parses to NaN
}

#[test]
fn test_demonstrate_parse_bool() {
    assert_eq!(advanced_str::demonstrate_parse_bool("true"), Ok(true));
    assert_eq!(advanced_str::demonstrate_parse_bool("FALSE"), Ok(false));
    assert!(advanced_str::demonstrate_parse_bool("maybe").is_err());
}

// --- Byte Operations ---

#[test]
fn test_demonstrate_as_bytes() {
    assert_eq!(advanced_str::demonstrate_as_bytes("abc"), &[97, 98, 99]);
}

#[test]
fn test_demonstrate_from_utf8() {
    assert_eq!(advanced_str::demonstrate_from_utf8(&[72, 101, 108, 108, 111]), Ok("Hello"));
    assert!(advanced_str::demonstrate_from_utf8(&[0xFF, 0xFE]).is_err());
}

#[test]
fn test_demonstrate_bytes() {
    assert_eq!(advanced_str::demonstrate_bytes("ab"), vec![97, 98]);
}

// --- Character Operations ---

#[test]
fn test_demonstrate_is_ascii() {
    assert!(advanced_str::demonstrate_is_ascii("Hello"));
    assert!(!advanced_str::demonstrate_is_ascii("Héllo"));
}

#[test]
fn test_demonstrate_to_ascii_uppercase() {
    assert_eq!(advanced_str::demonstrate_to_ascii_uppercase("hello"), "HELLO");
    assert_eq!(advanced_str::demonstrate_to_ascii_uppercase("café"), "CAFé"); // é stays
}

#[test]
fn test_demonstrate_char_count() {
    assert_eq!(advanced_str::demonstrate_char_count("hello"), 5);
    assert_eq!(advanced_str::demonstrate_char_count("🦀🦀"), 2); // 2 chars, 8 bytes
}

#[test]
fn test_demonstrate_first_char() {
    assert_eq!(advanced_str::demonstrate_first_char("hello"), Some('h'));
    assert_eq!(advanced_str::demonstrate_first_char(""), None);
    assert_eq!(advanced_str::demonstrate_first_char("🦀rust"), Some('🦀'));
}

// --- Performance ---

#[test]
fn test_demonstrate_with_capacity() {
    assert_eq!(
        advanced_str::demonstrate_with_capacity(&["Hello", ", ", "world", "!"]),
        "Hello, world!"
    );
    assert_eq!(advanced_str::demonstrate_with_capacity(&[]), "");
}

#[test]
fn test_demonstrate_cow_strip() {
    let borrowed = advanced_str::demonstrate_cow_strip("hello");
    assert!(matches!(borrowed, std::borrow::Cow::Borrowed(s) if s == "hello"));

    let owned = advanced_str::demonstrate_cow_strip(" hello ");
    assert!(matches!(owned, std::borrow::Cow::Owned(ref s) if s == "hello"));
}

// --- Case-Insensitive Operations ---

#[test]
fn test_demonstrate_eq_ignore_ascii_case() {
    assert!(advanced_str::demonstrate_eq_ignore_ascii_case("Hello", "HELLO"));
    assert!(!advanced_str::demonstrate_eq_ignore_ascii_case("Hello", "World"));
}

#[test]
fn test_demonstrate_starts_with_ignore_case() {
    assert!(advanced_str::demonstrate_starts_with_ignore_case("Hello World", "hello"));
    assert!(!advanced_str::demonstrate_starts_with_ignore_case("Hello World", "world"));
}

// --- String Replacement Patterns ---

#[test]
fn test_demonstrate_replacen() {
    assert_eq!(
        advanced_str::demonstrate_replacen("banana", "a", "X", 1),
        "bXnana"
    );
    assert_eq!(
        advanced_str::demonstrate_replacen("banana", "a", "X", 2),
        "bXnXna"
    );
}

#[test]
fn test_demonstrate_replace_matches() {
    assert_eq!(
        advanced_str::demonstrate_replace_matches("hello world", 'l', "LL"),
        "heLLLLo worLLd"
    );
}
