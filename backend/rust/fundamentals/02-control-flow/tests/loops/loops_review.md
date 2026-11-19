# Loops Module — Result vs Panic Review

## What happened

We went through three approaches before landing on the right one:

| Attempt               | Problem                                  | Output                          |
| --------------------- | ---------------------------------------- | ------------------------------- |
| `error!()` macro      | `core::error` is a module, not a macro   | compilation error               |
| `panic!()` + `catch_unwind` | panic caught but backtrace still prints | noisy, confusing               |
| `panic!()` + `#[should_panic]` | always prints panic to stdout         | noisy even on pass              |
| `Result<T, E>`        | no panic, no noise, clean assertions     | **clean — this is the answer**  |

---

## Result is like a Promise

### JavaScript Promise

```js
// Async function returns a Promise
async function fetchData(url) {
  if (!url) {
    throw new Error("URL is required");
  }
  const response = await fetch(url);
  return response.json();
}

// Caller handles both outcomes
fetchData("api/data")
  .then(data => console.log(data))    // success path
  .catch(err => console.error(err));  // error path
```

### Rust Result

```rust
// Function returns a Result
fn print_to_end(end: i32) -> Result<String, String> {
    if end < 1 {
        return Err("End must be greater than or equal to 1".to_string());
    }
    // ... build string ...
    Ok(result)
}

// Caller handles both outcomes
let result = print_to_end(3);
match result {
    Ok(output) => println!("{}", output),   // success path
    Err(msg) => eprintln!("{}", msg),       // error path
}
```

### The mapping

| JavaScript         | Rust                  | Meaning                        |
| ------------------ | --------------------- | ------------------------------ |
| `Promise<T>`       | `Result<T, E>`        | "This might succeed or fail"   |
| `.then(data => ...)`  | `Ok(value)` / `unwrap()` | success case              |
| `.catch(err => ...)`  | `Err(msg)` / `unwrap_err()` | error case            |
| `throw new Error()`   | `return Err(...)`     | signal failure                 |
| `await promise`       | `result?`             | propagate error upward         |
| `try/catch`           | `match result { ... }`| handle both branches           |

---

## Key difference: sync vs async

Promise is **async** — it represents work that happens later.
Result is **sync** — the value is already here, you just need to check it.

For async in Rust, you use `Future` (the actual Promise equivalent), but it still returns a `Result` when it resolves. So the pattern composes:

```rust
// Rust async (closest to JS async/await)
async fn fetch_data(url: &str) -> Result<Data, Error> {
    if url.is_empty() {
        return Err("URL required".into());
    }
    // ... async work ...
    Ok(data)
}
```

---

## Panic is NOT an error

A panic in Rust is like an uncaught exception that crashes the whole process. It is not for "the user gave bad input". It is for "something impossible happened and the program cannot continue".

```rust
// WRONG: panic for bad user input
fn divide(a: i32, b: i32) -> i32 {
    if b == 0 {
        panic!("Cannot divide by zero");  // noisy, crashes
    }
    a / b
}

// RIGHT: Result for bad input
fn divide(a: i32, b: i32) -> Result<i32, String> {
    if b == 0 {
        return Err("Cannot divide by zero".to_string());
    }
    Ok(a / b)
}
```

---

## Why catch_unwind printed the backtrace

`catch_unwind` catches the panic so the test passes, but Rust's panic hook runs **before** the catch. The hook prints the message and backtrace to stderr. There is no way to silence it without custom panic hooks.

This is why `catch_unwind` exists: to test that a library panics when given bad data, or to prevent a plugin from crashing the host. It is **not** a general error-handling mechanism.

---

## Rule of thumb

| Situation                      | Use       |
| ------------------------------ | --------- |
| Expected failure, caller should handle | `Result<T, E>` |
| Programming error, never recoverable     | `panic!()`     |
| Test that a function panics              | `#[should_panic]` |
| Catch a panic silently                   | **cannot be done** |

If you want it to behave like a Promise's `.catch()`, use `Result`. Always.
