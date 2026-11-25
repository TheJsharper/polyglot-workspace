use std::assert_matches;

use std::fmt::Write;

pub fn format_demo(name: &str, age: u32) -> String {

    format!("{} is {} years old", name, age)

}

pub fn format_positional_named() -> String {

    format!("{1} loves {0} and {name}", "Rust", "Alice", name = "coding")

}

pub fn format_specifiers() -> String {

    let decimal_value = 42.12345_f64;

    format!(
        "decimal: {:.2}, padded: {:0>5}, binary: {:b}, hex: {:x}, octal: {:o}",
        decimal_value, 42, 42, 255, 64
    )

}

pub fn format_debug() -> String {

    let number_vec = vec![1, 2, 3];

    format!("{:?}", number_vec)

}

pub fn format_pretty_debug() -> String {

    let tuple_vec = vec![("a", 1), ("b", 2)];

    format!("{:#?}", tuple_vec)

}

pub fn assert_positive(value: i32) -> Result<(), String> {

    assert!(value > 0, "expected positive, got {}", value);

    Ok(())

}

pub fn assert_eq_demo(first_value: i32, second_value: i32) -> Result<(), String> {

    assert_eq!(first_value, second_value, "values don't match: {} != {}", first_value, second_value);

    Ok(())

}

pub fn assert_ne_demo(first_value: i32, second_value: i32) -> Result<(), String> {

    assert_ne!(first_value, second_value, "values should differ but both are {}", first_value);

    Ok(())

}

pub fn assert_matches_demo(maybe_value: Option<i32>) -> Result<(), String> {

    assert_matches!(maybe_value, Some(inner) if inner > 0, "expected Some(positive), got {:?}", maybe_value);

    Ok(())

}

pub fn debug_assert_is_active() -> bool {

    let mut assertion_checked = false;

    debug_assert!({
        assertion_checked = true;

        true
    });

    assertion_checked

}

pub fn panic_message(error_message: &str) -> String {

    format!("Would panic with: {}", error_message)

}

pub fn unreachable_demo(is_even: bool) -> Result<&'static str, String> {

    if is_even {
        Ok("even")
    } else {
        Err(format!("unreachable!() would fire here"))
    }

}

pub fn todo_demo() -> String {

    "todo!() would panic with 'not yet implemented'".to_string()

}

pub fn unimplemented_demo() -> String {

    "unimplemented!() would panic with 'not implemented'".to_string()

}

pub fn cfg_demo() -> bool {

    cfg!(target_os = "windows")

}

pub fn cfg_multiple() -> bool {

    cfg!(all(target_os = "windows", debug_assertions))

}

pub fn env_cargo_pkg_name() -> &'static str {

    env!("CARGO_PKG_NAME")

}

pub fn env_cargo_pkg_version() -> &'static str {

    env!("CARGO_PKG_VERSION")

}

pub fn option_env_demo() -> Option<&'static str> {

    option_env!("MY_CUSTOM_VAR")

}

pub fn option_env_cargo() -> Option<&'static str> {

    option_env!("CARGO_PKG_NAME")

}

pub fn stringify_demo() -> &'static str {

    stringify!(2 + 2 == 4)

}

pub fn stringify_function_call() -> &'static str {

    stringify!(some_function(arg1, arg2))

}

pub fn concat_demo() -> &'static str {

    concat!("hello", " ", "world", "!")

}

pub fn concat_with_stringify() -> &'static str {

    concat!("The expression is: ", stringify!(1 + 2), " and result is 3")

}

pub fn file_demo() -> &'static str {

    file!()

}

pub fn line_demo() -> u32 {

    line!()

}

pub fn column_demo() -> u32 {

    column!()

}

pub fn module_path_demo() -> &'static str {

    module_path!()

}

pub fn dbg_demo(input_value: i32) -> String {

    format!("dbg!({}) would print to stderr and return {}", input_value, input_value)

}

pub fn dbg_multiple(first_value: i32, second_value: i32) -> String {

    format!(
        "dbg!({}, {}) would print both and return ({}, {})",
        first_value, second_value, first_value, second_value
    )

}

pub fn format_args_demo(greeting_name: &str) -> String {

    let formatted_args = format_args!("Hello, {}!", greeting_name);

    formatted_args.to_string()

}

pub fn write_demo() -> Result<String, std::fmt::Error> {

    let mut output_buffer = String::new();

    write!(output_buffer, "value: {}", 42)?;

    Ok(output_buffer)

}

pub fn writeln_demo() -> Result<String, std::fmt::Error> {

    let mut output_buffer = String::new();

    writeln!(output_buffer, "line one")?;

    writeln!(output_buffer, "line two")?;

    Ok(output_buffer)

}

pub fn vec_demo() -> Vec<i32> {

    vec![1, 2, 3, 4, 5]

}

pub fn vec_repeat() -> Vec<&'static str> {

    vec!["hello"; 3]

}

pub fn matches_option(maybe_number: Option<i32>) -> bool {

    matches!(maybe_number, Some(inner) if inner > 10)

}

pub fn matches_multiple(input_string: &str) -> bool {

    matches!(input_string, "hello" | "world" | "rust")

}

pub fn matches_result(maybe_result: Result<i32, String>) -> bool {

    matches!(maybe_result, Ok(inner) if inner >= 0)

}

pub fn compile_error_description() -> &'static str {

    "compile_error!(\"message\") causes compilation to fail with the given message"

}

pub fn include_str_description() -> &'static str {

    "include_str!(\"path\") embeds a file as &'static str at compile time"

}

pub fn include_bytes_description() -> &'static str {

    "include_bytes!(\"path\") embeds a file as &'static [u8; N] at compile time"

}

pub fn include_description() -> &'static str {

    "include!(\"path.rs\") parses and inserts a Rust file at compile time"

}

pub fn thread_local_description() -> &'static str {

    "thread_local! { static X: RefCell<T> = RefCell::new(val) } creates per-thread storage"

}

pub fn try_description() -> &'static str {

    "try!(expr) is deprecated — use expr? instead"

}
