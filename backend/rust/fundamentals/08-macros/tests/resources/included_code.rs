// This file is included via include!() macro at compile time.
// It becomes part of the test code as if typed directly.
pub fn included_function() -> &'static str {
    "I was included at compile time!"
}
