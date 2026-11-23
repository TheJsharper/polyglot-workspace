/// Advanced string operations: parsing, splitting, patterns, Unicode, and performance.

// --- Splitting Variants ---

/// Splits on whitespace (spaces, tabs, newlines), trimming automatically.
pub fn demonstrate_split_whitespace(s: &str) -> Vec<&str> {
    s.split_whitespace().collect()
}

/// Splits at most `n` times, returning at most `n+1` pieces.
pub fn demonstrate_splitn<'a>(s: &'a str, delimiter: &str, n: usize) -> Vec<&'a str> {
    s.splitn(n, delimiter).collect()
}

/// Splits from the right (reverse order).
pub fn demonstrate_rsplit<'a>(s: &'a str, delimiter: &str) -> Vec<&'a str> {
    s.rsplit(delimiter).collect()
}

/// Splits on lines, handling both `\n` and `\r\n`.
pub fn demonstrate_lines(s: &str) -> Vec<&str> {
    s.lines().collect()
}

/// Finds all starting positions of a substring.
pub fn demonstrate_match_indices<'a>(s: &'a str, pattern: &str) -> Vec<(usize, &'a str)> {
    s.match_indices(pattern).collect()
}

/// Finds the last occurrence of a substring.
pub fn demonstrate_rfind(s: &str, substring: &str) -> Option<usize> {
    s.rfind(substring)
}

// --- Trimming Variants ---

/// Trims whitespace from the start only.
pub fn demonstrate_trim_start(s: &str) -> &str {
    s.trim_start()
}

/// Trims whitespace from the end only.
pub fn demonstrate_trim_end(s: &str) -> &str {
    s.trim_end()
}

/// Trims specific characters from both ends.
pub fn demonstrate_trim_matches(s: &str, c: char) -> &str {
    s.trim_matches(c)
}

// --- Parsing ---

/// Parses a string into a number. Returns `Result<T, ParseIntError>`.
pub fn demonstrate_parse_i32(s: &str) -> Result<i32, std::num::ParseIntError> {
    s.parse::<i32>()
}

/// Parses a string into a float. Returns `Result<f64, ParseFloatError>`.
pub fn demonstrate_parse_f64(s: &str) -> Result<f64, std::num::ParseFloatError> {
    s.parse::<f64>()
}

/// Parses a string into a boolean. Accepts "true" and "false" (case-insensitive).
pub fn demonstrate_parse_bool(s: &str) -> Result<bool, std::str::ParseBoolError> {
    s.trim().to_lowercase().parse::<bool>()
}

// --- Byte Operations ---

/// Returns the raw UTF-8 bytes of a string.
pub fn demonstrate_as_bytes(s: &str) -> &[u8] {
    s.as_bytes()
}

/// Creates a string from raw bytes, validating UTF-8.
pub fn demonstrate_from_utf8(bytes: &[u8]) -> Result<&str, std::str::Utf8Error> {
    std::str::from_utf8(bytes)
}

/// Iterates over raw bytes (not characters).
pub fn demonstrate_bytes(s: &str) -> Vec<u8> {
    s.bytes().collect()
}

// --- Character Operations ---

/// Checks if all characters are ASCII.
pub fn demonstrate_is_ascii(s: &str) -> bool {
    s.is_ascii()
}

/// Converts only ASCII characters to uppercase (leaves non-ASCII untouched).
pub fn demonstrate_to_ascii_uppercase(s: &str) -> String {
    s.to_ascii_uppercase()
}

/// Counts the number of Unicode characters (not bytes).
pub fn demonstrate_char_count(s: &str) -> usize {
    s.chars().count()
}

/// Returns the first Unicode character, if any.
pub fn demonstrate_first_char(s: &str) -> Option<char> {
    s.chars().next()
}

// --- Performance ---

/// Builds a string efficiently with pre-allocated capacity.
pub fn demonstrate_with_capacity(parts: &[&str]) -> String {
    let total_len: usize = parts.iter().map(|s| s.len()).sum();
    let mut result = String::with_capacity(total_len);
    for part in parts {
        result.push_str(part);
    }
    result
}

/// Demonstrates `Cow<str>` — borrowed when possible, owned when modification is needed.
pub fn demonstrate_cow_strip(s: &str) -> std::borrow::Cow<'_, str> {
    if s.starts_with(' ') || s.ends_with(' ') {
        std::borrow::Cow::Owned(s.trim().to_string())
    } else {
        std::borrow::Cow::Borrowed(s)
    }
}

// --- Case-Insensitive Operations ---

/// Case-insensitive equality check.
pub fn demonstrate_eq_ignore_ascii_case(a: &str, b: &str) -> bool {
    a.eq_ignore_ascii_case(b)
}

/// Case-insensitive starts_with.
pub fn demonstrate_starts_with_ignore_case(s: &str, prefix: &str) -> bool {
    s.to_lowercase().starts_with(&prefix.to_lowercase())
}

// --- String Replacement Patterns ---

/// Replaces only the first occurrence of a pattern.
pub fn demonstrate_replacen(s: &str, from: &str, to: &str, count: usize) -> String {
    s.replacen(from, to, count)
}

/// Replaces each character matching a predicate.
pub fn demonstrate_replace_matches(s: &str, c: char, replacement: &str) -> String {
    s.replace(c, replacement)
}
