use macro_demo_crate::*;

#[test]
fn test_format_basic() {

    let formatted_result = format_demo("Alice", 30);

    assert_eq!(formatted_result, "Alice is 30 years old");

}

#[test]
fn test_format_positional_named() {

    let formatted_result = format_positional_named();

    assert_eq!(formatted_result, "Alice loves Rust and coding");

}

#[test]
fn test_format_specifiers() {

    let formatted_result = format_specifiers();

    assert!(formatted_result.contains("42.12"));

    assert!(formatted_result.contains("00042"));

    assert!(formatted_result.contains("101010"));

    assert!(formatted_result.contains("ff"));

    assert!(formatted_result.contains("100"));

}

#[test]
fn test_format_debug() {

    let formatted_result = format_debug();

    assert_eq!(formatted_result, "[1, 2, 3]");

}

#[test]
fn test_format_pretty_debug() {

    let formatted_result = format_pretty_debug();

    assert!(formatted_result.contains('"'));

    assert!(formatted_result.contains("a"));

    assert!(formatted_result.contains("b"));

}

#[test]
fn test_format_empty() {

    let formatted_result = format!("");

    assert_eq!(formatted_result, "");

}

#[test]
fn test_format_with_expressions() {

    let formatted_result = format!("2 + 2 = {}", 2 + 2);

    assert_eq!(formatted_result, "2 + 2 = 4");

}

#[test]
fn test_assert_positive_pass() {

    assert!(assert_positive(5).is_ok());

}

#[test]
fn test_assert_positive_fail() {

    assert!(std::panic::catch_unwind(|| assert_positive(-1)).is_err());

}

#[test]
fn test_assert_eq_pass() {

    assert!(assert_eq_demo(10, 10).is_ok());

}

#[test]
fn test_assert_eq_fail() {

    assert!(std::panic::catch_unwind(|| assert_eq_demo(10, 20)).is_err());

}

#[test]
fn test_assert_ne_pass() {

    assert!(assert_ne_demo(1, 2).is_ok());

}

#[test]
fn test_assert_ne_fail() {

    assert!(std::panic::catch_unwind(|| assert_ne_demo(5, 5)).is_err());

}

#[test]
fn test_assert_matches_pass_some() {

    assert!(assert_matches_demo(Some(42)).is_ok());

}

#[test]
fn test_assert_matches_fail_none() {

    assert!(std::panic::catch_unwind(|| assert_matches_demo(None)).is_err());

}

#[test]
fn test_assert_matches_fail_negative() {

    assert!(std::panic::catch_unwind(|| assert_matches_demo(Some(-5))).is_err());

}

#[test]
fn test_debug_assert_is_active() {

    let assertion_result = debug_assert_is_active();

    assert!(assertion_result);

}

#[test]
fn test_panic_message() {

    let panic_description = panic_message("something went wrong");

    assert_eq!(panic_description, "Would panic with: something went wrong");

}

#[test]
fn test_unreachable_demo_even() {

    let path_result = unreachable_demo(true).unwrap();

    assert_eq!(path_result, "even");

}

#[test]
fn test_unreachable_demo_odd() {

    let error_result = unreachable_demo(false);

    assert!(error_result.is_err());

    let error_message = error_result.unwrap_err();

    assert!(error_message.contains("unreachable"));

}

#[test]
fn test_todo_demo() {

    let todo_description = todo_demo();

    assert!(todo_description.contains("not yet implemented"));

}

#[test]
fn test_unimplemented_demo() {

    let unimplemented_description = unimplemented_demo();

    assert!(unimplemented_description.contains("not implemented"));

}

#[test]
fn test_cfg_windows() {

    let is_windows = cfg_demo();

    if cfg!(target_os = "windows") {
        assert!(is_windows);
    } else {
        assert!(!is_windows);
    }

}

#[test]
fn test_cfg_multiple() {

    let cfg_result = cfg_multiple();

    if cfg!(all(target_os = "windows", debug_assertions)) {
        assert!(cfg_result);
    }

}

#[test]
fn test_env_cargo_pkg_name() {

    let package_name = env_cargo_pkg_name();

    assert_eq!(package_name, "macro_demo_crate");

}

#[test]
fn test_env_cargo_pkg_version() {

    let package_version = env_cargo_pkg_version();

    assert_eq!(package_version, "0.1.0");

}

#[test]
fn test_option_env_none() {

    let optional_result = option_env_demo();

    assert!(optional_result.is_none());

}

#[test]
fn test_option_env_some() {

    let optional_result = option_env_cargo();

    assert_eq!(optional_result, Some("macro_demo_crate"));

}

#[test]
fn test_stringify_expression() {

    let stringified_result = stringify_demo();

    assert_eq!(stringified_result, "2 + 2 == 4");

}

#[test]
fn test_stringify_function_call() {

    let stringified_result = stringify_function_call();

    assert_eq!(stringified_result, "some_function(arg1, arg2)");

}

#[test]
fn test_concat_demo() {

    let concatenated_result = concat_demo();

    assert_eq!(concatenated_result, "hello world!");

}

#[test]
fn test_concat_with_stringify() {

    let concatenated_result = concat_with_stringify();

    assert_eq!(concatenated_result, "The expression is: 1 + 2 and result is 3");

}

#[test]
fn test_concat_empty() {

    let concatenated_result = concat!("");

    assert_eq!(concatenated_result, "");

}

#[test]
fn test_concat_single() {

    let concatenated_result = concat!("solo");

    assert_eq!(concatenated_result, "solo");

}

#[test]
fn test_file_demo() {

    let source_path = file_demo();

    assert!(source_path.contains("macro_demo.rs"));

}

#[test]
fn test_line_demo() {

    let source_line = line_demo();

    assert!(source_line > 0);

}

#[test]
fn test_column_demo() {

    let source_column = column_demo();

    assert!(source_column > 0);

}

#[test]
fn test_module_path_demo() {

    let module_path = module_path_demo();

    assert_eq!(module_path, "macro_demo_crate::macros::macro_demo");

}

#[test]
fn test_dbg_demo() {

    let debug_description = dbg_demo(42);

    assert!(debug_description.contains("42"));

}

#[test]
fn test_dbg_multiple() {

    let debug_description = dbg_multiple(10, 20);

    assert!(debug_description.contains("10"));

    assert!(debug_description.contains("20"));

}

#[test]
fn test_format_args_demo() {

    let formatted_result = format_args_demo("World");

    assert_eq!(formatted_result, "Hello, World!");

}

#[test]
fn test_write_demo() {

    let write_result = write_demo().unwrap();

    assert_eq!(write_result, "value: 42");

}

#[test]
fn test_writeln_demo() {

    let writeln_result = writeln_demo().unwrap();

    assert_eq!(writeln_result, "line one\nline two\n");

}

#[test]
fn test_vec_demo() {

    let number_vec = vec_demo();

    assert_eq!(number_vec, vec![1, 2, 3, 4, 5]);

    assert_eq!(number_vec.len(), 5);

}

#[test]
fn test_vec_repeat() {

    let repeated_vec = vec_repeat();

    assert_eq!(repeated_vec, vec!["hello", "hello", "hello"]);

    assert_eq!(repeated_vec.len(), 3);

}

#[test]
fn test_vec_empty() {

    let empty_vec: Vec<i32> = vec![];

    assert!(empty_vec.is_empty());

}

#[test]
fn test_vec_single() {

    let single_vec = vec![42];

    assert_eq!(single_vec, vec![42]);

}

#[test]
fn test_matches_option_some_gt_10() {

    assert!(matches_option(Some(15)));

}

#[test]
fn test_matches_option_some_lt_10() {

    assert!(!matches_option(Some(5)));

}

#[test]
fn test_matches_option_none() {

    assert!(!matches_option(None));

}

#[test]
fn test_matches_multiple_hello() {

    assert!(matches_multiple("hello"));

}

#[test]
fn test_matches_multiple_rust() {

    assert!(matches_multiple("rust"));

}

#[test]
fn test_matches_multiple_other() {

    assert!(!matches_multiple("other"));

}

#[test]
fn test_matches_result_ok_positive() {

    assert!(matches_result(Ok(42)));

}

#[test]
fn test_matches_result_ok_zero() {

    assert!(matches_result(Ok(0)));

}

#[test]
fn test_matches_result_err() {

    assert!(!matches_result(Err("error".to_string())));

}

include!("../resources/included_code.rs");

#[test]
fn test_include_str_macro() {

    let included_content = include_str!("../resources/included_text.txt");

    assert!(included_content.contains("Hello from included file!"));

    assert!(included_content.contains("This is line 2."));

    assert!(included_content.contains("This is line 3."));

}

#[test]
fn test_include_bytes_macro() {

    let included_bytes = include_bytes!("../resources/included_text.txt");

    assert!(!included_bytes.is_empty());

    assert_eq!(&included_bytes[..5], b"Hello");

}

#[test]
fn test_include_macro() {

    let included_result = included_function();

    assert_eq!(included_result, "I was included at compile time!");

}

#[test]
fn test_compile_error_description() {

    let error_description = compile_error_description();

    assert!(error_description.contains("compile_error"));

}

#[test]
fn test_thread_local_description() {

    let thread_description = thread_local_description();

    assert!(thread_description.contains("thread_local"));

}

#[test]
fn test_try_description() {

    let try_description = try_description();

    assert!(try_description.contains("deprecated"));

}

#[test]
fn test_panic_macro_actually_panics() {

    let panic_result = std::panic::catch_unwind(|| {
        panic!("test panic");
    });

    assert!(panic_result.is_err());

}

#[test]
fn test_unreachable_macro_actually_panics() {

    let unreachable_result = std::panic::catch_unwind(|| {
        unreachable!("this should not be reached");
    });

    assert!(unreachable_result.is_err());

}

#[test]
fn test_todo_macro_actually_panics() {

    let todo_result = std::panic::catch_unwind(|| {
        todo!();
    });

    assert!(todo_result.is_err());

}

#[test]
fn test_unimplemented_macro_actually_panics() {

    let unimplemented_result = std::panic::catch_unwind(|| {
        unimplemented!();
    });

    assert!(unimplemented_result.is_err());

}
