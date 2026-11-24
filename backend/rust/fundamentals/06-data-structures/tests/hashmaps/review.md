# HashMap Test Cases

## What is a HashMap?

A HashMap stores **key-value pairs**. Think of it like a dictionary — you look up a word (key) to find its meaning (value).

```rust
use std::collections::HashMap;

let mut capital_cities = HashMap::new();
capital_cities.insert("England", "London");
capital_cities.insert("Germany", "Berlin");
capital_cities.insert("Norway", "Oslo");
```

## Test Cases Explained

### `test_create_hashmap`
Creates a simple HashMap with names and ages.
- Tests basic insertion and lookup with `.get()`
- `.get()` returns `Option<&V>` — `Some(value)` or `None`

### `test_create_capital_cities`
Creates a country → city mapping.
- Tests multiple inserts
- Verifies each key returns the correct city

### `test_get_city`
Tests safe lookup with the `get_city()` helper.
- Returns `Some(&city)` if found
- Returns `None` if key doesn't exist
- No panic on missing keys!

### `test_add_capital`
Tests adding a new entry after creation.
- `.insert()` adds or **overwrites** if key exists
- HashMap grows dynamically

### `test_remove_capital`
Tests removing an entry by key.
- `.remove()` returns `Some(value)` if key existed
- Returns `None` if key wasn't there
- Entry is gone after removal

### `test_duplicate_key_overwrites`
Shows what happens when you insert the same key twice.
```rust
capital_cities.insert("England", "London");
capital_cities.insert("England", "Berlin");  // Overwrites!
```
- **Second insert wins** — "London" is replaced by "Berlin"
- Map still has 1 entry, not 2
- No error or warning!

### `test_remove_country`
Tests the exact example from the tutorial.
- Creates 3 entries
- Removes "England"
- Verifies only Germany and Norway remain

### `test_iterate_capitals`
Tests looping through all key-value pairs.
```rust
for (country, city) in &capital_cities {
    println!("The capital of {} is {}.", country, city);
}
```
- Uses `&` to borrow (not take ownership)
- Destructures each entry into `(key, value)`

## Key Operations

| Operation | Method | Returns | Notes |
|-----------|--------|---------|-------|
| Insert | `.insert(key, value)` | `Option<old_value>` | Overwrites if key exists |
| Get | `.get(&key)` | `Option<&value>` | Never panics |
| Remove | `.remove(&key)` | `Option<value>` | Deletes the entry |
| Contains | `.contains_key(&key)` | `bool` | Quick existence check |
| Length | `.len()` | `usize` | Number of entries |
| Empty | `.is_empty()` | `bool` | True if no entries |

## HashMap vs Other Collections

| Feature | HashMap | Vector | Array |
|---------|---------|--------|-------|
| Access by | **Key** | Index | Index |
| Speed | O(1) average | O(1) | O(1) |
| Order | ❌ Not guaranteed | ✅ Insertion order | ✅ Fixed order |
| Keys must be | Unique | N/A | N/A |
| Best for | Lookups by name | Ordered lists | Fixed-size data |

## Important Rules

1. **Keys must be unique** — second insert overwrites the first
2. **Order is not guaranteed** — iteration may differ from insertion order
3. **`.get()` never panics** — returns `Option` instead
4. **Keys must implement `Eq` and `Hash`** — strings, numbers work fine

## When to Use HashMap

- Looking up values by name/ID instead of index
- Counting occurrences (e.g., word frequency)
- Caching computed results
- Grouping data by a key

## When Not to Use HashMap

- You need items in a specific order → use `Vec` + sort
- Small fixed set of keys → use a struct or enum
- You only have values, no keys → use `Vec`
