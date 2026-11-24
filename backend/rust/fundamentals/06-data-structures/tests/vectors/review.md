# Vectors Test Cases

## What are Vectors?

Vectors (`Vec<T>`) are **dynamic-size** collections that can grow and shrink at runtime. They store elements contiguously on the heap.

```rust
let mut fruits = vec!["apple", "banana"];
fruits.push("cherry");  // Grows dynamically
```

## Test Cases Explained

### `test_vec_create_and_push`
Tests vector creation and adding elements.
- Creates initial vector with 3 elements
- Uses `.push()` to add a 4th element
- Verifies the vector grew correctly

### `test_vec_pop`
Tests removing elements from the end.
- Uses `.pop()` which returns `Option<T>`
- Returns the removed element wrapped in `Some`
- Returns `None` if vector is empty

### `test_vec_get_car`
Tests safe element access with bounds checking.
- Returns `Ok(value)` for valid index
- Returns `Err` for out-of-bounds index
- Shows custom error message with index details

### `test_vec_update_car`
Tests modifying existing elements.
- Updates element at specific index
- Validates bounds before update
- Returns `Result<(), String>`

### `test_vec_insert`
Tests inserting elements at any position.
- Inserts at beginning (index 0)
- Inserts at middle (index 2)
- Shifts existing elements right
- Validates bounds (can insert at `len`, not beyond)

### `test_vec_remove`
Tests removing elements at any position.
- Removes element and returns it
- Shifts remaining elements left
- Validates bounds before removal

### `test_find_by_lambda`
Tests flexible search with closures.
- Finds first element matching a condition
- Uses `|&f| f.starts_with('a')` predicate
- Returns `Option<usize>` (index or None)

### `test_find_all_by_lambda`
Tests finding all matching elements.
- Returns all indices matching the condition
- Uses `.enumerate()` and `.filter()`
- Returns `Vec<usize>` of matching indices

### `test_contains_fruit`
Tests membership checking.
- Uses built-in `.contains()` method
- Returns `bool` (true/false)
- Simple equality check

## Vector Operations Summary

| Operation | Method | Complexity | Description |
|-----------|--------|------------|-------------|
| Add to end | `.push()` | O(1)* | Fast amortized |
| Remove from end | `.pop()` | O(1) | Returns `Option<T>` |
| Insert at index | `.insert()` | O(n) | Shifts elements right |
| Remove at index | `.remove()` | O(n) | Shifts elements left |
| Access by index | `[index]` | O(1) | Panics if out of bounds |
| Safe access | `.get()` | O(1) | Returns `Option<&T>` |
| Search | `.iter().position()` | O(n) | Finds first match |
| Contains | `.contains()` | O(n) | Checks membership |

*Amortized: occasionally O(n) when reallocating, but average is O(1)

## Closures (Lambdas) in Rust

Closures are anonymous functions you can pass around:

```rust
// Simple closure
|&fruit| fruit.starts_with('a')

// With multiple parameters
|index, fruit| index > 0 && fruit.len() > 5

// Capturing variables from scope
let prefix = "ap";
fruits.iter().filter(|&f| f.starts_with(prefix))
```

### Why Use Closures?

- **Flexible**: Any condition, not just equality
- **Reusable**: Same `find_by` function works with any predicate
- **Expressive**: Code reads like English

## When to Use Vectors

- Collection size changes at runtime
- You need to add/remove elements frequently
- Reading data from files, APIs, or user input
- Building collections incrementally

## Memory Layout

```
Vector: [ptr][len][cap]
         └──-> [A][B][C][ ][ ][ ]  <- Heap allocation
```

- `ptr`: Pointer to heap memory
- `len`: Number of elements
- `cap`: Allocated capacity (may be larger than len)

When `len == cap`, vector reallocates with double capacity.
