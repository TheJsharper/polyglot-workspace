#[path = "../../src/basics-fn/basics_fn.rs"]
mod basics_fn;
use basics_fn::*;

#[test]
fn test_demonstrate_basic_function() {
    assert_eq!(demonstrate_basic_function(), 42);
}

#[test]
fn test_demonstrate_basic_function_with_argument() {
    assert_eq!(demonstrate_basic_function_with_argument(5), 5);
}

#[test]
fn test_demonstrate_basic_function_with_multiple_arguments() {
    assert_eq!(demonstrate_basic_function_with_multiple_arguments(3, 4), 7);
}

#[test]
fn test_demonstrate_basic_function_with_return_type_annotation() {
    assert_eq!(demonstrate_basic_function_with_return_type_annotation(5), 5);
}

#[test]
fn test_demonstrate_basic_function_with_expression() {
    assert_eq!(demonstrate_basic_function_with_expression(5), 5);
}

#[test]
fn test_demonstrate_basic_function_with_default_argument() {
    assert_eq!(demonstrate_basic_function_with_default_argument(3, 4), 7);
}

#[test]
fn test_demonstrate_basic_function_with_optional_argument_some() {
    assert_eq!(demonstrate_basic_function_with_optional_argument(3, Some(4)), 7);
}

#[test]
fn test_demonstrate_basic_function_with_optional_argument_none() {
    assert_eq!(demonstrate_basic_function_with_optional_argument(3, None), 3);
}

#[test]
fn test_demonstrate_basic_function_with_variadic_arguments() {
    assert_eq!(demonstrate_basic_function_with_variadic_arguments(1, &[2, 3, 4]), 10);
}

#[test]
fn test_demonstrate_basic_function_with_closure_argument() {
    assert_eq!(demonstrate_basic_function_with_closure_argument(5, |x| x * 2), 10);
}

#[test]
fn test_demonstrate_basic_function_with_generic_argument() {
    assert_eq!(demonstrate_basic_function_with_generic_argument(3, 4), (3, 4));
}

#[test]
fn test_demonstrate_basic_function_with_generic_return_type() {
    assert_eq!(demonstrate_basic_function_with_generic_return_type(42), 42);
}

#[test]
fn test_demonstrate_basic_function_with_generic_closure_argument() {
    assert_eq!(demonstrate_basic_function_with_generic_closure_argument(5, |x| x + 10), 15);
}

#[test]
fn test_demonstrate_basic_function_with_generic_variadic_arguments() {
    let (first, rest) = demonstrate_basic_function_with_generic_variadic_arguments(1, &[2, 3]);
    assert_eq!(first, 1);
    assert_eq!(rest, vec![1, 2, 3]);
}
