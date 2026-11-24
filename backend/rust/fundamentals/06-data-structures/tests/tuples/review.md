# Tuples Test Cases

## What are Tuples?

Tuples are **fixed-size** collections that can hold elements of **different types**. They're useful for grouping related values without creating a named struct.

```rust
let person = ("John", 30, true);  // (&str, i32, bool)
let point = (3, 4);               // (i32, i32)
let color = (255, 128, 0);        // (u8, u8, u8)
```

## Test Cases Explained

### `test_create_person`
Tests creating a tuple with mixed types.
- Contains `&str`, `i32`, and `bool`
- Accesses elements by index: `.0`, `.1`, `.2`
- Shows heterogeneous type support

### `test_tuple_accessors`
Tests helper functions for tuple access.
- `get_name()` extracts the name field
- `get_age()` extracts the age field
- `is_active()` extracts the boolean field
- Demonstrates why helper functions improve readability

### `test_point_operations`
Tests tuple manipulation with coordinates.
- `create_point()` creates a 2D point
- `point_sum()` calculates x + y
- `swap_point()` returns a new tuple with swapped values
- Shows tuples as lightweight data structures

### `test_rgb_colors`
Tests tuples for color representation.
- RGB values as `(u8, u8, u8)`
- `is_grayscale()` checks if all channels are equal
- Demonstrates domain modeling with tuples

### `test_tuple_destructuring`
Tests pattern matching to extract tuple values.
```rust
let (name, age, active) = person;
```
- Unpacks all values at once
- More readable than `.0`, `.1`, `.2`
- Idiomatic Rust pattern

## Tuple Access Methods

| Method | Example | Pros | Cons |
|--------|---------|------|------|
| Index | `person.0` | Quick, no function needed | Unclear meaning |
| Destructuring | `let (name, _) = person` | Clean, readable | Need all variables |
| Helper function | `get_name(&person)` | Self-documenting | More code to write |

## Tuples vs Arrays vs Vectors

| Feature | Tuple `(A, B)` | Array `[T; N]` | Vector `Vec<T>` |
|---------|---------------|----------------|-----------------|
| Types | **Different** | Same | Same |
| Size | Fixed | Fixed | Dynamic |
| Access | `.0`, `.1` | `[index]` | `[index]` |
| Destructuring | ✅ Yes | ❌ No | ❌ No |
| Use case | Group related values | Fixed homogeneous data | Dynamic homogeneous data |

## When to Use Tuples

- Returning multiple values from a function
- Grouping related but different types
- Quick data grouping without defining a struct
- Pattern matching and destructuring

## When to Use Structs Instead

Consider a struct when:
- The tuple has more than 2-3 elements
- You need to add methods
- Field names would improve clarity
- You'll pass it around frequently

```rust
// Tuple (ok for simple cases)
let person = ("John", 30, true);

// Struct (better for complex cases)
struct Person {
    name: String,
    age: u32,
    active: bool,
}
```

## Lifetime Annotations in Tuples

When tuples contain references, you need lifetime annotations:

```rust
// Without lifetimes - won't compile
fn get_name(person: &(&str, i32, bool)) -> &str { ... }

// With lifetimes - correct
fn get_name<'a>(person: &'a (&'a str, i32, bool)) -> &'a str {
    person.0
}
```

The lifetime `'a` tells the compiler that the returned `&str` lives as long as the tuple reference.
