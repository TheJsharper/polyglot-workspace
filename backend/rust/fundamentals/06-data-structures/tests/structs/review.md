# Struct Test Cases

## What is a Struct?

A struct is a **custom data type** that groups related values with named fields. Unlike tuples, each field has a **name** — making code clearer.

```rust
struct Person {
    name: String,
    age: u32,
    can_vote: bool,
}
```

## Test Cases Explained

### `test_create_person`
Tests creating a Person struct with the `create_person_struct()` function.
- Creates a person with name "Alice" and age 20
- Verifies `can_vote` is `true` (age >= 18)
- Shows struct field access with `.name`, `.age`, `.can_vote`

### `test_create_person_underage`
Tests the voting logic for minors.
- Creates a person with age 16
- Verifies `can_vote` is `false`
- Shows automatic logic inside the constructor

### `test_mutate_person`
Tests modifying struct fields after creation.
- Creates a person, then changes `name` and `age`
- Requires `mut` keyword on the variable
- Shows that structs are mutable by default when declared with `mut`

## Struct vs Tuple

| Feature | Struct | Tuple |
|---------|--------|-------|
| Field names | ✅ Named (`person.name`) | ❌ Index only (`person.0`) |
| Readability | High — self-documenting | Low — need to remember order |
| Reorder fields | ✅ Safe | ❌ Breaks code |
| Add fields | ✅ Easy | ❌ Changes all access |
| Best for | Complex data with many fields | Quick grouping, function returns |

```rust
// Tuple — unclear what each value means
let person = ("Alice", 20, true);

// Struct — clear and self-documenting
let person = Person {
    name: String::from("Alice"),
    age: 20,
    can_vote: true,
};
```

## Mutable Structs

Structs are **immutable by default**. To change fields, use `mut`:

```rust
// Immutable — cannot change fields
let person = Person { name: "John".to_string(), age: 35, can_vote: true };
person.name = "Jane".to_string();  // ❌ Error

// Mutable — can change fields
let mut person = Person { name: "John".to_string(), age: 35, can_vote: true };
person.name = "Jane".to_string();  // ✅ Works
```

## Struct Field Update Syntax

Use `..` to copy remaining fields from another instance:

```rust
let person1 = Person {
    name: String::from("Alice"),
    age: 20,
    can_vote: true,
};

let person2 = Person {
    name: String::from("Bob"),
    ..person1  // Copies age and can_vote from person1
};
```

## When to Use Structs

- Data has **3+ fields** or needs clarity
- Fields benefit from **names** (not just positions)
- You'll pass the data around frequently
- You might add **methods** later with `impl`

## When to Use Tuples Instead

- Returning **2-3 values** from a function
- Quick, temporary grouping
- Field order is obvious from context
- You don't want to define a new type

## When to Use HashMap Instead

- **Unknown number** of key-value pairs
- Looking up values by **dynamic keys**
- Data grows/shrink at runtime
- Keys aren't known at compile time
