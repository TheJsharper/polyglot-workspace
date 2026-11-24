# Arrays vs Vectors in Rust

## Key Differences

| Feature | Array `[T; N]` | Vector `Vec<T>` |
|---------|---------------|-----------------|
| **Size** | Fixed at compile time | Dynamic at runtime |
| **Memory** | Stack (usually) | Heap |
| **Grow/Shrink** | ❌ Cannot change size | ✅ `.push()`, `.pop()`, `.insert()`, `.remove()` |
| **Performance** | Faster (no allocation) | Slightly slower (heap allocation) |
| **Syntax** | `[1, 2, 3]` | `vec![1, 2, 3]` |
| **Type** | `[i32; 3]` (size is part of type) | `Vec<i32>` |

## When to Use Which

- **Array**: When you know the exact number of elements at compile time
- **Vector**: When the number of elements can change at runtime

## Common Operations

### Iteration
```rust
let fruits = vec!["apple", "banana", "orange"];
for fruit in &fruits {
    println!("I like {}.", fruit);
}
```

### Find by Lambda
```rust
let fruits = vec!["apple", "banana", "cherry", "apricot"];
// Find first fruit starting with 'a'
let index = find_by(&fruits, |&f| f.starts_with('a')); // Some(0)
// Find all fruits starting with 'a'
let indices = find_all_by(&fruits, |&f| f.starts_with('a')); // [0, 3]
```

### Push (Add to end)
```rust
let mut fruits = vec!["apple", "banana"];
fruits.push("cherry");
println!("{:?}", fruits); // ["apple", "banana", "cherry"]
```

### Pop (Remove from end)
```rust
let mut fruits = vec!["apple", "banana", "cherry"];
fruits.pop();
println!("{:?}", fruits); // ["apple", "banana"]
```

### Insert (Add at index)
```rust
let mut fruits = vec!["banana", "orange"];
fruits.insert(0, "apple");
println!("{:?}", fruits); // ["apple", "banana", "orange"]
```

### Remove (Remove at index)
```rust
let mut fruits = vec!["apple", "banana", "cherry"];
let removed = fruits.remove(1);
println!("{:?}", fruits); // ["apple", "cherry"]
println!("{}", removed);  // "banana"
```

### Access by Index
```rust
let fruits = vec!["apple", "banana", "cherry"];
println!("{}", fruits[0]); // "apple"
```

### Get with Bounds Check
```rust
let fruits = vec!["apple", "banana"];
match fruits.get(2) {
    Some(fruit) => println!("{}", fruit),
    None => println!("Index out of bounds"),
}
```

## Memory Layout

```
Array: [A][B][C]           <- Contiguous stack memory, fixed size
Vector: [ptr][len][cap]    <- Pointer to heap, dynamic size
         └──-> [A][B][C][ ][ ]  <- Heap allocation with capacity
```

## Performance Trade-offs

| Operation | Array | Vector |
|-----------|-------|--------|
| Index access | O(1) | O(1) |
| Push (end) | N/A | O(1) amortized |
| Pop (end) | N/A | O(1) |
| Insert at index | N/A | O(n) - shifts elements |
| Remove at index | N/A | O(n) - shifts elements |
| Memory allocation | None | Heap allocation |

## Summary

- **Arrays** are for fixed-size collections known at compile time
- **Vectors** are for dynamic collections that grow/shrink at runtime
- Both provide O(1) index access
- Vectors add heap allocation overhead but offer flexibility
- Use `.push()` to add and `.pop()` to remove from vectors
