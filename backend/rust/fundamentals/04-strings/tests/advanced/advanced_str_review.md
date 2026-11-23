# Advanced Str — Test Review & Function Explanations

## Current State

| Metric          | Value         |
| --------------- | ------------- |
| Total tests     | 25            |
| All passing     | yes           |
| Functions tested| 24            |
| Happy path tests| 25            |
| Error path tests| 4             |

---

## Function Explanations

### 1. `demonstrate_split_whitespace(s: &str) -> Vec<&str>`

**What it does:** Splits on any whitespace (spaces, tabs, newlines) and trims automatically.

**Why it matters:** This is the most common split operation in practice. Unlike `split(" ")`, it handles multiple spaces, tabs, and newlines in one pass, and never produces empty strings from consecutive whitespace.

**Key concept:** `split_whitespace` is built on `char::is_whitespace`. It skips leading/trailing whitespace automatically — no need to call `.trim()` first. The return type `Vec<&str>` borrows from the original string, so no allocation for the pieces themselves.

**Example:** `split_whitespace("  hello   world  ")` → `vec!["hello", "world"]`

**When to use:** Parsing user input, splitting command-line arguments, tokenizing text, processing CSV-like data with variable spacing.

---

### 2. `demonstrate_splitn<'a>(s: &'a str, delimiter: &str, n: usize) -> Vec<&'a str>`

**What it does:** Splits at most `n` times, returning at most `n+1` pieces. The last piece contains the unsplit remainder.

**Why it matters:** When you only care about the first few fields, splitting the entire string is wasteful. `splitn` stops early, saving time on long strings. This is essential for parsing formats like `"key:value:more:stuff"` where you want `["key", "value:more:stuff"]`.

**Key concept:** The lifetime `'a` ties the returned `&str` slices to the input `s`. The `delimiter` parameter doesn't share this lifetime because the returned slices never borrow from the delimiter. If `n` is 1, the entire string is returned as a single element — no splitting occurs.

**Example:** `splitn("a,b,c,d", ",", 3)` → `vec!["a", "b", "c,d"]`

**When to use:** Parsing headers (`"Content-Type: text/html; charset=utf-8"`), extracting the first N fields from a delimited string, key-value parsing where the value may contain the delimiter.

---

### 3. `demonstrate_rsplit<'a>(s: &'a str, delimiter: &str) -> Vec<&'a str>`

**What it does:** Splits from right to left, producing pieces in reverse order.

**Why it matters:** When the interesting data is at the end of a string, `rsplit` gives you direct access without reversing or indexing. File extensions are the classic example — `"file.tar.gz"` split from the right gives `["gz", "tar", "file"]`.

**Key concept:** `rsplit` produces the same pieces as `split`, just in reverse order. The iterator yields `&str` from right to left. When collected into a `Vec`, the last piece of the original string appears first.

**Example:** `rsplit("a.b.c", ".")` → `vec!["c", "b", "a"]`

**When to use:** Extracting file extensions, parsing reverse-domain names (`com.example.app`), getting the last segment of a path, processing suffix-based data.

---

### 4. `demonstrate_lines(s: &str) -> Vec<&str>`

**What it does:** Splits on line boundaries, handling both `\n` (Unix) and `\r\n` (Windows).

**Why it matters:** Cross-platform text processing requires handling both line ending conventions. `lines()` strips the line terminators automatically — you get clean content without `\n` or `\r\n` attached. It does **not** include a final empty string if the input ends with a newline (unlike `split('\n')`).

**Key concept:** `lines()` is a specialized iterator that recognizes `\n`, `\r\n`, and lone `\r` (old Mac convention). It produces `&str` slices without the line endings. If the string ends with a newline, no empty trailing element is produced — `"a\nb\n"` gives `["a", "b"]`, not `["a", "b", ""]`.

**Example:** `lines("line1\nline2\r\nline3")` → `vec!["line1", "line2", "line3"]`

**When to use:** Reading file contents, processing log files, parsing multi-line input, splitting paragraphs.

---

### 5. `demonstrate_match_indices<'a>(s: &'a str, pattern: &str) -> Vec<(usize, &'a str)>`

**What it does:** Returns every occurrence of a pattern as `(start_position, matched_string)` pairs.

**Why it matters:** When you need to know **where** matches occur, not just **what** they are. This is essential for syntax highlighting, search-and-replace with position awareness, or building an index of occurrences.

**Key concept:** Each tuple contains the byte offset (not character index) and a reference to the matched substring. The `usize` is a byte position because Rust strings are UTF-8. For ASCII text, byte position equals character position. For multi-byte characters, they diverge.

**Example:** `match_indices("banana", "an")` → `vec![(1, "an"), (3, "an")]`

**When to use:** Building a search results index, syntax highlighting (finding all keywords), measuring match density, implementing find-and-replace with position tracking.

---

### 6. `demonstrate_rfind(s: &str, substring: &str) -> Option<usize>`

**What it does:** Returns the byte position of the **last** occurrence, or `None` if not found.

**Why it matters:** `find` gives you the first match; `rfind` gives you the last. This is critical for extracting file extensions (last `.`), getting the final path segment (last `/`), or finding the most recent occurrence of a marker.

**Key concept:** Returns `Option<usize>` — the byte offset, not a character index. For `"banana".rfind("an")`, the answer is `3` (the second "an"), not `1` (the first). The `Option` forces the caller to handle the "not found" case explicitly.

**Example:** `rfind("banana", "an")` → `Some(3)`

**When to use:** Extracting file extensions (`file.rfind('.')`), getting the last path separator, finding the final occurrence of a delimiter, reverse search in logs.

---

### 7. `demonstrate_trim_start(s: &str) -> &str`

**What it does:** Removes leading whitespace, returns a `&str` slice.

**Why it matters:** Sometimes you only need to strip the beginning — preserving trailing whitespace intentionally (e.g., in indented code blocks or formatted output). No allocation: returns a slice into the original string.

**Key concept:** `trim_start` advances the start pointer past whitespace characters. The returned `&str` shares storage with the input — zero-copy. Whitespace includes space, tab, newline, carriage return, and various Unicode space characters.

**Example:** `trim_start("  hello  ")` → `"hello  "`

**When to use:** Processing indented text, cleaning user input prefixes, stripping leading indentation from code blocks.

---

### 8. `demonstrate_trim_end(s: &str) -> &str`

**What it does:** Removes trailing whitespace, returns a `&str` slice.

**Why it matters:** The counterpart to `trim_start`. Useful when trailing whitespace is noise but leading whitespace is meaningful (e.g., preserving indentation in source code while removing trailing spaces).

**Key concept:** Shrinks the end pointer backward past whitespace. Like `trim_start`, this is zero-copy — the returned `&str` borrows from the input.

**Example:** `trim_end("  hello  ")` → `"  hello"`

**When to use:** Removing trailing newlines from file reads, cleaning up log lines, stripping trailing whitespace from user input while preserving leading indentation.

---

### 9. `demonstrate_trim_matches(s: &str, c: char) -> &str`

**What it does:** Removes a specific character from both ends, repeatedly until a different character is found.

**Why it matters:** `trim()` only removes whitespace. `trim_matches(c)` lets you strip any character — quotes, asterisks, dashes, custom delimiters. It strips **repeatedly**, not just once.

**Key concept:** Removes the character from both ends as long as it matches. `"***hello***".trim_matches('*')` strips all three asterisks from each side, not just one. This is different from `strip_prefix`/`strip_suffix` which only remove once.

**Example:** `trim_matches("***hello***", '*')` → `"hello"`

**When to use:** Stripping quote characters from parsed strings, removing markdown emphasis markers (`***bold***`), cleaning custom delimiters, parsing wrapped content.

---

### 10. `demonstrate_parse_i32(s: &str) -> Result<i32, ParseIntError>`

**What it does:** Parses a decimal string into an `i32`. Returns `Ok(n)` or `Err(ParseIntError)`.

**Why it matters:** This is the standard way to convert string input into numbers in Rust. The `Result` type forces you to handle parse failures — no silent `NaN` or exception-based error handling.

**Key concept:** `s.parse::<i32>()` uses the `FromStr` trait. The turbofish `::<i32>` specifies the target type. The error type `ParseIntError` distinguishes between invalid syntax (`"abc"`) and overflow (`"99999999999999"`). Leading/trailing whitespace causes a parse error — unlike some languages, Rust's `parse` is strict.

**Example:** `parse_i32("42")` → `Ok(42)`
**Example:** `parse_i32("abc")` → `Err(ParseIntError)`

**When to use:** Reading numbers from config files, parsing command-line arguments, converting user input to numeric types.

---

### 11. `demonstrate_parse_f64(s: &str) -> Result<f64, ParseFloatError>`

**What it does:** Parses a string into a 64-bit floating-point number.

**Why it matters:** Float parsing handles decimal points, scientific notation (`1e-10`), and special values (`"inf"`, `"-inf"`, `"NaN"`). Unlike integers, floats can represent a much wider range of values.

**Key concept:** `parse::<f64>()` accepts `"inf"`, `"-inf"`, and `"NaN"` as valid inputs — these return `Ok(f64::INFINITY)`, `Ok(f64::NEG_INFINITY)`, and `Ok(f64::NAN)` respectively. This differs from `parse_i32` where any non-numeric string is an error.

**Example:** `parse_f64("3.14")` → `Ok(3.14)`
**Example:** `parse_f64("1e-10")` → `Ok(0.0000000001)`

**When to use:** Parsing scientific data, reading floating-point config values, processing numerical user input.

---

### 12. `demonstrate_parse_bool(s: &str) -> Result<bool, ParseBoolError>`

**What it does:** Parses `"true"` or `"false"` (case-insensitive) into a `bool`.

**Why it matters:** Boolean parsing is stricter than you might expect — only `"true"` and `"false"` are accepted (not `"1"`/`"0"` or `"yes"`/`"no"`). The implementation trims and lowercases first, making it tolerant of whitespace and case variations.

**Key concept:** Rust's `bool::from_str` accepts exactly `"true"` and `"false"`. By calling `.trim().to_lowercase().parse::<bool>()`, we handle `" TRUE "`, `"False"`, `"  true  "` etc. The `.trim()` and `.to_lowercase()` allocate a new `String` before parsing — this is a tradeoff for convenience.

**Example:** `parse_bool("TRUE")` → `Ok(true)`
**Example:** `parse_bool("maybe")` → `Err(ParseBoolError)`

**When to use:** Reading boolean flags from config files, environment variables (`"true"`/`"false"`), feature toggles.

---

### 13. `demonstrate_as_bytes(s: &str) -> &[u8]`

**What it does:** Returns the raw UTF-8 byte slice underlying the string.

**Why it matters:** Strings in Rust are UTF-8 encoded. Sometimes you need the raw bytes — for hashing, network transmission, or interoperating with C APIs. This is a zero-cost view: no copying, just a reinterpretation of the string's memory.

**Key concept:** `&str` is already UTF-8, so `as_bytes()` is just a pointer cast — zero allocation, zero copy. The returned `&[u8]` has the same lifetime as the input `&str`. Each byte is a `u8` (0-255). For ASCII strings, bytes equal characters. For Unicode, multi-byte characters produce multiple bytes.

**Example:** `as_bytes("abc")` → `&[97, 98, 99]`
**Example:** `as_bytes("🦀")` → `&[240, 159, 166, 128]` (4 bytes)

**When to use:** Computing checksums, writing to binary protocols, FFI with C, cryptographic operations.

---

### 14. `demonstrate_from_utf8(bytes: &[u8]) -> Result<&str, Utf8Error>`

**What it does:** Validates a byte slice as UTF-8 and returns a `&str` if valid.

**Why it matters:** This is the inverse of `as_bytes()`. When you receive raw bytes (from a file, network, or FFI), you must validate them as UTF-8 before treating them as text. Not all byte sequences are valid UTF-8.

**Key concept:** `std::str::from_utf8` checks the byte sequence for UTF-8 validity without copying. If valid, it returns `Ok(&str)` — a borrowed view. If invalid, it returns `Err(Utf8Error)` with details about the first invalid byte position. This is the safe alternative to `std::str::from_utf8_unchecked` (which is `unsafe`).

**Example:** `from_utf8(&[72, 101, 108, 108, 111])` → `Ok("Hello")`
**Example:** `from_utf8(&[0xFF, 0xFE])` → `Err(Utf8Error)`

**When to use:** Reading binary files that may contain text, processing network protocols, validating user-supplied byte data, FFI boundary validation.

---

### 15. `demonstrate_bytes(s: &str) -> Vec<u8>`

**What it does:** Collects all bytes of a string into a `Vec<u8>`.

**Why it matters:** `as_bytes()` returns a borrowed slice (`&[u8]`); `bytes()` produces an owned `Vec<u8>`. Use this when you need to transfer ownership of the bytes to another function, store them, or send them across threads.

**Key concept:** Unlike `as_bytes()` (zero-copy), this allocates and copies all bytes into a new `Vec`. The difference: `&[u8]` borrows from the string, `Vec<u8>` owns its data. For owned `String`, prefer `into_bytes()` which consumes the string without copying.

**Example:** `bytes("ab")` → `vec![97, 98]`

**When to use:** Transferring string data to binary APIs, serializing text for storage, passing bytes to cryptographic functions that require owned buffers.

---

### 16. `demonstrate_is_ascii(s: &str) -> bool`

**What it does:** Returns `true` if every byte in the string is an ASCII character (0-127).

**Why it matters:** ASCII validation is a fast way to check if a string is "simple" text. ASCII strings have useful properties: byte length equals character count, no multi-byte sequences, safe to index by byte position, compatible with C strings.

**Key concept:** This checks every byte, not every character. A string like `"café"` contains the non-ASCII character `é` (encoded as bytes `0xC3 0xA9`), so it returns `false`. The check is O(n) but very fast — it's a simple range comparison per byte.

**Example:** `is_ascii("Hello")` → `true`
**Example:** `is_ascii("Héllo")` → `false`

**When to use:** Validating input before passing to ASCII-only APIs, optimizing code paths (ASCII strings are simpler to process), checking compatibility with legacy systems.

---

### 17. `demonstrate_to_ascii_uppercase(s: &str) -> String`

**What it does:** Converts only ASCII letters (a-z) to uppercase, leaving non-ASCII characters unchanged.

**Why it matters:** `to_uppercase()` handles Unicode case mapping (e.g., German `ß` → `SS`), which can change the string length and requires locale-aware rules. `to_ascii_uppercase()` is simpler and faster — it only touches bytes in the range `a`-`z`, converting them to `A`-`Z`.

**Key concept:** This allocates a new `String` because the result may differ in content. Non-ASCII characters pass through unchanged — `café` becomes `CAFé` (the `é` is untouched). This is different from `to_uppercase()` which would also handle Unicode case mappings.

**Example:** `to_ascii_uppercase("hello")` → `"HELLO"`
**Example:** `to_ascii_uppercase("café")` → `"CAFé"`

**When to use:** Case-insensitive ASCII comparisons, normalizing identifiers, HTTP header processing, case-folding for ASCII-only lookups.

---

### 18. `demonstrate_char_count(s: &str) -> usize`

**What it does:** Counts Unicode characters (code points), not bytes.

**Why it matters:** `s.len()` returns byte count, which differs from character count for non-ASCII text. `"🦀🦀"` is 8 bytes but 2 characters. `char_count` iterates over Unicode scalar values, giving the actual number of readable characters.

**Key concept:** `chars()` produces an iterator over Unicode scalar values (`char` is a 32-bit type). Each `char` represents one Unicode code point. This is still not the full story — some "visual characters" (like emojis with skin tone modifiers) are multiple code points combined. But for most practical purposes, `chars().count()` gives the expected character count.

**Example:** `char_count("hello")` → `5`
**Example:** `char_count("🦀🦀")` → `2` (but `len()` returns `8`)

**When to use:** Display character counts to users, enforcing length limits on text input, measuring "readable" text length, pagination.

---

### 19. `demonstrate_first_char(s: &str) -> Option<char>`

**What it does:** Returns the first Unicode character, or `None` for empty strings.

**Why it matters:** You can't do `s[0]` to get the first character of a Rust string — byte indexing can panic if the index falls in the middle of a multi-byte character. `chars().next()` safely extracts the first Unicode scalar value.

**Key concept:** `chars()` returns an iterator over `char` values. `.next()` returns `Option<char>` — `None` for empty strings, `Some(c)` otherwise. This handles multi-byte characters correctly: `"🦀rust".chars().next()` returns `Some('🦀')`, not a garbled byte slice.

**Example:** `first_char("hello")` → `Some('h')`
**Example:** `first_char("🦀rust")` → `Some('🦀')`

**When to use:** Checking the first character for validation (e.g., must start with a letter), extracting initials, detecting encoding markers (BOM), quick type inspection.

---

### 20. `demonstrate_with_capacity(parts: &[&str]) -> String`

**What it does:** Builds a string by concatenating parts, pre-allocating the exact needed capacity.

**Why it matters:** Without pre-allocation, `String` grows its buffer incrementally (doubling each time), causing unnecessary reallocations and copies. By computing the total length upfront and calling `String::with_capacity()`, we allocate exactly once.

**Key concept:** `String::with_capacity(n)` allocates a buffer of `n` bytes but starts with length 0. Each `push_str` writes into the pre-allocated buffer without reallocation. The capacity calculation `parts.iter().map(|s| s.len()).sum()` is O(n) but cheap — it's just adding integers, not copying data.

**Example:** `with_capacity(&["Hello", ", ", "world", "!"])` → `"Hello, world!"`

**When to use:** Building large strings in loops, templating, serializing data structures, any situation where you know the final size in advance.

---

### 21. `demonstrate_cow_strip(s: &str) -> Cow<'_, str>`

**What it does:** Returns a borrowed `Cow` if no trimming is needed, or an owned `Cow` if trimming was required.

**Why it matters:** `Cow` (Copy-on-Write) is Rust's smart zero-allocation pattern. If the string has no leading/trailing spaces, we return `Cow::Borrowed(s)` — no allocation. If it does, we return `Cow::Owned(trimmed)` — one allocation. The caller gets a uniform interface regardless.

**Key concept:** `Cow<'_, str>` is an enum with two variants: `Borrowed(&str)` and `Owned(String)`. Both implement `Deref<Target=str>`, so the caller can use it as `&str` regardless of which variant it is. This is the ideal API for "modify only if necessary" operations.

**Example:** `cow_strip("hello")` → `Cow::Borrowed("hello")` (no allocation)
**Example:** `cow_strip(" hello ")` → `Cow::Owned("hello")` (one allocation)

**When to use:** Text normalization pipelines, input sanitization, any "clean if needed" operation where the common case requires no change.

---

### 22. `demonstrate_eq_ignore_ascii_case(a: &str, b: &str) -> bool`

**What it does:** Compares two strings for equality, ignoring ASCII case differences.

**Why it matters:** This is the fast path for case-insensitive comparison. Unlike `a.to_lowercase() == b.to_lowercase()` (which allocates two new Strings), `eq_ignore_ascii_case` compares byte-by-byte without allocation. It's O(n) time and O(1) extra space.

**Key concept:** Only compares ASCII letters (a-z vs A-Z). Non-ASCII characters must match exactly — `"café"` does not equal `"CAFÉ"` because `é` ≠ `É` in ASCII comparison. For full Unicode case-insensitive comparison, you'd need `unicase` or similar crates.

**Example:** `eq_ignore_ascii_case("Hello", "HELLO")` → `true`
**Example:** `eq_ignore_ascii_case("café", "CAFÉ")` → `false`

**When to use:** Case-insensitive lookups in hash maps, comparing HTTP headers, matching command names, user input validation.

---

### 23. `demonstrate_starts_with_ignore_case(s: &str, prefix: &str) -> bool`

**What it does:** Checks if the string starts with a prefix, ignoring ASCII case.

**Why it matters:** Rust's standard library doesn't have a built-in case-insensitive `starts_with`. This implementation lowercases both strings before comparing. It's simple and correct, but allocates two temporary Strings.

**Key concept:** This allocates because `to_lowercase()` returns a new `String`. For performance-critical code, you'd want a byte-by-byte comparison that converts each character on the fly. But for most use cases, the clarity of this approach outweighs the minor allocation cost.

**Example:** `starts_with_ignore_case("Hello World", "hello")` → `true`

**When to use:** URL prefix matching, command parsing, protocol detection, file extension checks (case-insensitive).

---

### 24. `demonstrate_replacen(s: &str, from: &str, to: &str, count: usize) -> String`

**What it does:** Replaces at most `count` occurrences of `from` with `to`.

**Why it matters:** `replace()` replaces ALL occurrences. `replacen()` gives you control — replace the first one, the first two, or any number. This is essential when you want to selectively replace patterns.

**Key concept:** Returns a new `String` — the original is unchanged. If `count` is 0, no replacements occur (returns a clone of the original). If `count` exceeds the number of occurrences, all occurrences are replaced (same as `replace()`).

**Example:** `replacen("banana", "a", "X", 1)` → `"bXnana"`
**Example:** `replacen("banana", "a", "X", 2)` → `"bXnXna"`

**When to use:** Template substitution (replace first occurrence of each placeholder), sanitizing input (remove first N instances of a problematic pattern), incremental text transformation.

---

### 25. `demonstrate_replace_matches(s: &str, c: char, replacement: &str) -> String`

**What it does:** Replaces every occurrence of a single character with a string.

**Why it matters:** `replace()` works with `&str` patterns. This function is specialized for single-character replacement, which is the most common case. The replacement can be a multi-character string — replacing `'l'` with `"LL"` doubles each `l`.

**Key concept:** The first argument is a `char`, not a `&str`. This is more efficient than `replace` for single characters because the search can work character-by-character rather than pattern-matching. The replacement string can be any length — it doesn't need to be a single character.

**Example:** `replace_matches("hello world", 'l', "LL")` → `"heLLLLo worLLd"`

**When to use:** Character escaping (replacing `'<'` with `"&lt;"`), emphasis expansion (replacing `*` with `**`), custom delimiter substitution.

---

## Lifetime Analysis

Five functions required explicit lifetime annotations because they return `&str` slices borrowed from the input:

| Function | Lifetime | Why |
|----------|----------|-----|
| `splitn` | `'a` on `s` and return | Slices borrow from `s`, not `delimiter` |
| `rsplit` | `'a` on `s` and return | Same — slices are views into `s` |
| `match_indices` | `'a` on `s` and tuple's `&str` | Matched substrings borrow from `s` |
| `split` (basics) | `'a` on `s` and return | Same pattern |
| `find` (basics) | elided (only returns `&str`) | Single input — lifetime is inferred |

The pattern: when a function takes **two** `&str` parameters and returns a `&str`, Rust can't guess which one the return borrows from. Explicit `<'a>` resolves the ambiguity.

---

## Test Coverage Map

| Function | Happy | Edge Cases | Error Path | Missing |
|----------| :---: | :--------: | :--------: | ------- |
| `split_whitespace` | yes | empty | — | Unicode whitespace |
| `splitn` | yes | n=1 | — | n=0 |
| `rsplit` | yes | — | — | empty string |
| `lines` | yes | mixed `\n`/`\r\n` | — | trailing newline |
| `match_indices` | yes | not found | — | overlapping matches |
| `rfind` | yes | not found | — | empty pattern |
| `trim_start` | yes | — | — | only start whitespace |
| `trim_end` | yes | — | — | only end whitespace |
| `trim_matches` | yes | — | — | no matches |
| `parse_i32` | yes | — | invalid input | overflow |
| `parse_f64` | yes | `nan` | — | overflow |
| `parse_bool` | yes | case-insensitive | invalid | whitespace |
| `as_bytes` | yes | — | — | Unicode string |
| `from_utf8` | yes | invalid bytes | error case | empty |
| `bytes` | yes | — | — | Unicode string |
| `is_ascii` | yes | non-ASCII | — | empty |
| `to_ascii_uppercase` | yes | mixed ASCII/non-ASCII | — | empty |
| `char_count` | yes | emoji (2 chars) | — | combining chars |
| `first_char` | yes | empty, emoji | — | multi-byte char |
| `with_capacity` | yes | empty | — | single element |
| `cow_strip` | yes | borrowed + owned | — | multiple whitespace types |
| `eq_ignore_ascii_case` | yes | not equal | — | Unicode case |
| `starts_with_ignore_case` | yes | not matching | — | empty prefix |
| `replacen` | yes | count=1, count=2 | — | count=0, count > occurrences |
| `replace_matches` | yes | multi-char replacement | — | empty string |

---

## What works well

### Lifetime annotations are correct

All five functions that return `&str` from multi-reference inputs have explicit `<'a>` annotations. The returned slices correctly borrow from the source string, not from temporary values.

### `Cow` pattern demonstrates zero-allocation design

`demonstrate_cow_strip` shows the right way to handle "modify only if necessary" — borrowed for the common case (no trimming), owned for the exceptional case. The test verifies both variants.

### `splitn` tests the edge case of n=1

Testing `splitn("a,b,c", ",", 1)` → `["a,b,c"]` proves that n=1 means "no splitting" — the entire string is returned as one piece. This is a non-obvious behavior that deserves a test.

### Parse error cases are tested

`parse_i32("not_a_number").is_err()`, `parse_bool("maybe").is_err()`, and `from_utf8(&[0xFF, 0xFE]).is_err()` all verify the error paths. Parse functions should always be tested with invalid input.

### Unicode awareness

`char_count("🦀🦀")` → `2` and `first_char("🦀rust")` → `Some('🦀')` prove the code handles multi-byte characters correctly. The `to_ascii_uppercase("café")` → `"CAFé"` test shows ASCII-only behavior.

---

## Issues found

### 1. `parse_f64("nan")` test is fragile

The test asserts `parse_f64("nan").is_ok()` — this passes because `"nan"` parses to `NaN`. But `NaN != NaN` in floating-point math, so you can't use `assert_eq!` to verify the value. The current test is correct (`is_ok()`), but it doesn't verify the actual value is NaN. Consider adding `result.unwrap().is_nan()` for completeness.

### 2. No `parse_i32` overflow test

`"99999999999999"` should fail with an overflow error, not a syntax error. The current test only checks syntax errors. Testing overflow validates the full error handling path of `ParseIntError`.

### 3. `lines()` doesn't test trailing newline behavior

`"a\nb\n"` with `lines()` returns `["a", "b"]` — no trailing empty string. But `"a\nb\n".split('\n')` returns `["a", "b", ""]`. This difference is important and should be tested explicitly.

### 4. `cow_strip` doesn't test internal whitespace

`"hello world"` (spaces in the middle, no leading/trailing) should return `Cow::Borrowed`. The current test only checks the two extremes (fully clean, fully dirty). Internal whitespace is the "tricky" case — it has spaces but doesn't need trimming.

### 5. `replacen` doesn't test count=0 or count > occurrences

`replacen("banana", "a", "X", 0)` should return `"banana"` unchanged. `replacen("banana", "a", "X", 100)` should replace all three `a`s, behaving identically to `replace()`.

### 6. `split_whitespace` doesn't test tabs or newlines

The test uses spaces only. Tabs (`\t`), newlines (`\n`), and mixed whitespace should also be tested to prove `split_whitespace` handles all whitespace types.

---

## Summary of recommendations

| Priority | Action                                          | Effort   |
| -------- | ----------------------------------------------- | -------- |
| High     | Add `parse_i32` overflow test                   | 1 min    |
| High     | Add `replacen` count=0 and count > occurrences  | 1 min    |
| Medium   | Test `split_whitespace` with tabs and newlines  | 1 min    |
| Medium   | Test `cow_strip` with internal whitespace       | 1 min    |
| Medium   | Test `lines()` trailing newline behavior        | 1 min    |
| Low      | Verify `parse_f64("nan")` produces actual NaN   | 1 min    |
| Low      | Test `match_indices` with overlapping patterns  | 2 min    |
