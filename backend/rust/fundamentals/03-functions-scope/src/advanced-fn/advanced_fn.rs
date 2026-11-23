/// Advanced function examples: closures, higher-order functions, and lambdas.

/// Identity closure — takes a value and a closure, applies it.
pub fn apply_closure<T, F>(value: T, f: F) -> T
where
    F: FnOnce(T) -> T,
{
    f(value)
}

/// Map over a slice with a closure, returning a new Vec.
pub fn map_slice<F, T, U>(slice: &[T], f: F) -> Vec<U>
where
    F: Fn(&T) -> U,
{
    slice.iter().map(f).collect()
}

/// Filter a slice, keeping only elements where the predicate returns true.
pub fn filter_slice<F, T>(slice: &[T], predicate: F) -> Vec<&T>
where
    F: Fn(&T) -> bool,
{
    slice.iter().filter(|item| predicate(*item)).collect()
}

/// Fold (reduce) a slice into a single value using an accumulator and a closure.
pub fn fold_slice<F, T, B>(slice: &[T], init: B, f: F) -> B
where
    F: Fn(B, &T) -> B,
{
    slice.iter().fold(init, f)
}

/// Returns a closure that captures `offset` and adds it to any input.
/// Demonstrates a closure that moves captured data (FnOnce).
pub fn make_offset_adder(offset: i32) -> impl Fn(i32) -> i32 {
    move |x| x + offset
}

/// Returns a closure that counts how many times it has been called.
/// Demonstrates a mutable closure (FnMut).
pub fn make_counter() -> impl FnMut() -> u32 {
    let mut count = 0;
    move || {
        count += 1;
        count
    }
}

/// Composes two functions: f(g(x)).
pub fn compose<F, G, T>(f: F, g: G) -> impl Fn(T) -> T
where
    F: Fn(T) -> T,
    G: Fn(T) -> T,
    T: Clone,
{
    move |x| f(g(x.clone()))
}

/// Chains a slice of functions, applying each in order.
pub fn chain_apply<T, F>(value: T, functions: &[F]) -> T
where
    F: Fn(T) -> T,
    T: Clone,
{
    functions.iter().fold(value, |acc, f| f(acc))
}

/// Curried addition: takes one argument, returns a function that takes the second.
pub fn curried_add(x: i32) -> impl Fn(i32) -> i32 {
    move |y| x + y
}

/// Partial application: pre-fills the first argument of a two-arg function.
pub fn partial_apply<F, T, U, R>(f: F, first: T) -> impl Fn(U) -> R
where
    F: Fn(T, U) -> R,
    T: Clone,
{
    move |second| f(first.clone(), second)
}

/// Returns true if all elements in the slice satisfy the predicate.
pub fn all_match<T, F>(slice: &[T], predicate: F) -> bool
where
    F: Fn(&T) -> bool,
{
    slice.iter().all(predicate)
}

/// Returns true if any element in the slice satisfies the predicate.
pub fn any_match<T, F>(slice: &[T], predicate: F) -> bool
where
    F: Fn(&T) -> bool,
{
    slice.iter().any(predicate)
}

/// Find the first element matching the predicate.
pub fn find_first<T, F>(slice: &[T], predicate: F) -> Option<&T>
where
    F: Fn(&T) -> bool,
{
    slice.iter().find(|item| predicate(*item))
}
