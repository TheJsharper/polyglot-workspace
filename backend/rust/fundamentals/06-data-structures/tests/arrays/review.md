# Arrays Test Cases

## What are Arrays?

Arrays in Rust are **fixed-size** collections of elements of the same type, stored in contiguous memory.

```rust
let numbers = [1, 2, 3, 4, 5];     // Immutable array
let mut numbers = [1, 2, 3, 4, 5]; // Mutable array
```

## Test Cases Explained

### `test_find_array`
Tests reading elements by index with bounds checking.
- ✅ Valid index returns `Ok(value)`
- ❌ Invalid index returns `Err("Index out of bounds")`

### `test_find_array_mut`
Tests mutable access to array elements (String type).
- ✅ Returns mutable reference for valid index
- ❌ Returns error for out-of-bounds index

### `test_mutable_array`
Tests direct element modification with `mut` keyword.
- Shows that arrays need `mut` to change elements
- Demonstrates index assignment: `numbers[0] = 10`

### `test_update_element`
Tests safe element update with bounds checking.
- ✅ Updates valid index successfully
- ❌ Rejects invalid index with error message
- Array remains unchanged on error

### `test_swap_elements`
Tests swapping two positions in an array.
- Uses built-in `.swap()` method
- Validates bounds for both indices

### `test_double_all`
Tests iterating and modifying all elements.
- Multiplies each element by 2
- Shows in-place mutation pattern

### `test_array_out_of_bounds`
Tests error handling for fixed-size arrays.
- Demonstrates why you can't add elements to arrays
- Shows proper error message format

### `test_vec_push_instead`
Shows the alternative when you need dynamic size.
- Arrays can't grow — use `Vec` instead
- Demonstrates `.push()` on a vector

## Arrays vs Vectors — Key Differences

| Feature | Array `[T; N]` | Vector `Vec<T>` |
|---------|---------------|-----------------|
| Size | Fixed at compile time | Dynamic at runtime |
| Memory | Stack (usually) | Heap |
| Growth | ❌ Cannot add/remove | ✅ `.push()`, `.pop()` |
| Performance | Faster (no allocation) | Slightly slower |
| Type includes size | `[i32; 3]` | `Vec<i32>` |

## When to Use Arrays

- You know the exact number of elements at compile time
- Performance is critical (no heap allocation)
- Working with small, fixed collections (e.g., RGB colors, coordinates)
- Passing data to C APIs that expect fixed buffers

## When to Use Vectors

- The number of elements changes at runtime
- You need to add/remove elements dynamically
- Working with collections of unknown size (e.g., user input, file data)
