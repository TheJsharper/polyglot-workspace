# Enum Test Cases

## What is an Enum?

An enum (enumeration) is a type that can be **one of several named variants**. Think of it like a dropdown menu — you can only pick one option.

```rust
enum Color {
    Red,
    Green,
    Blue,
}

enum Direction {
    Up,
    Down,
    Left,
    Right,
}
```

## Test Cases Explained

### `test_create_color_enum`
Tests creating a Color enum from a string using `match`.
- `"Red"` → `Color::Red`
- `"Green"` → `Color::Green`
- `"Blue"` → `Color::Blue`
- `"Unknown"` → defaults to `Color::Red` (catch-all `_` pattern)

### `test_move_direction`
Tests returning different messages based on enum variant.
- Each direction returns a unique string
- Shows how `match` exhaustively handles all variants

### `test_reverse_direction`
Tests transforming one enum variant into another.
- `Up` ↔ `Down`
- `Left` ↔ `Right`
- Returns the same enum type, just a different variant

## Why Derive `Debug` and `PartialEq`?

```rust
#[derive(Debug, PartialEq)]
pub enum Color { Red, Green, Blue }
```

- **`Debug`** — required for `assert_eq!` error messages (prints the value)
- **`PartialEq`** — required for `==` comparison in `assert_eq!`

Without these, tests won't compile!

## Enum vs Struct vs Tuple

| Feature | Enum | Struct | Tuple |
|---------|------|--------|-------|
| Variants | **One of many** | All fields | All values |
| Best for | Choices/options | Grouped data | Quick grouping |
| Example | Color, Direction | Person, Point | (x, y) |
| Memory | Size of largest variant | Sum of all fields | Sum of all values |

## When to Use Enums

- A value can only be **one of several options** (e.g., traffic light: Red/Yellow/Green)
- Representing **states** (e.g., Loading, Success, Error)
- Replacing magic strings/numbers with named constants
- Working with `match` for exhaustive handling

## When Not to Use Enums

- You need to store **multiple values at once** → use a struct
- The options change at runtime → use a HashMap or Vec
- Only 2 options → consider `bool`

## The `match` Expression

Rust's `match` is like a supercharged `switch`:

```rust
fn move_direction(direction: Direction) -> &'static str {
    match direction {
        Direction::Up => "Moving up",
        Direction::Down => "Moving down",
        Direction::Left => "Moving left",
        Direction::Right => "Moving right",
    }
}
```

**Key rules:**
- Must handle **all variants** (exhaustive)
- Use `_` as catch-all for "everything else"
- No `break` needed — each arm is automatic

## Enum with Data (Advanced)

Enums can hold data too:

```rust
enum Message {
    Quit,
    Move { x: i32, y: i32 },
    Write(String),
    ChangeColor(i32, i32, i32),
}
```

This is how Rust's `Option<T>` and `Result<T, E>` work under the hood!
