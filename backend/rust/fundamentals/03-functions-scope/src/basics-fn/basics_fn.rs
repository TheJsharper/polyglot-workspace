pub fn demonstrate_basic_function() -> i32 {
    42
}

pub fn demonstrate_basic_function_with_argument(x: i32) -> i32 {
    x
}
pub fn demonstrate_basic_function_with_multiple_arguments(x: i32, y: i32) -> i32 {
    x + y
}

pub fn demonstrate_basic_function_with_return_type_annotation(x: i32) -> i32 {
    return x;
}

pub fn demonstrate_basic_function_with_expression(x: i32) -> i32 {
    x
}

pub fn demonstrate_basic_function_with_default_argument(x: i32, y: i32) -> i32 {
    x + y
}

pub fn demonstrate_basic_function_with_optional_argument(x: i32, y: Option<i32>) -> i32 {
    x + y.unwrap_or(0)
}

pub fn demonstrate_basic_function_with_variadic_arguments(x: i32, ys: &[i32]) -> i32 {
    x + ys.iter().sum::<i32>()
}

pub fn demonstrate_basic_function_with_closure_argument<F>(x: i32, f: F) -> i32
where
    F: Fn(i32) -> i32,
{
    f(x)
}

pub fn demonstrate_basic_function_with_generic_argument<T>(x: T, y: T) -> (T, T) {
    (x, y)
}

pub fn demonstrate_basic_function_with_generic_return_type<T>(x: T) -> T {
    x
}

pub fn demonstrate_basic_function_with_generic_closure_argument<T, F>(x: T, f: F) -> T
where
    F: Fn(T) -> T,
{
    f(x)
}

pub fn demonstrate_basic_function_with_generic_variadic_arguments<T>(x: T, ys: &[T]) -> (T, Vec<T>)
where
    T: Clone,
{
    let mut result = Vec::with_capacity(ys.len() + 1);
    result.push(x.clone());
    result.extend_from_slice(ys);
    (x, result)
}
