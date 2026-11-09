#[test]
fn test_hello_output() {
    let greeting = "Hello, world!";
    assert_eq!(greeting, "Hello, world!");
}

#[test]
fn test_variables() {
    let name = "Rust";
    let version = 1;
    assert!(version > 0);
    assert_eq!(name, "Rust");
}

#[test]
fn test_data_types() {
    let integer: i32 = 42;
    let decimal: f64 = 3.14;
    let boolean: bool = true;

    assert_eq!(integer, 42);
    assert!((decimal - 3.14).abs() < f64::EPSILON);
    assert!(boolean);
}

#[test]
fn test_operators() {
    let sum = 5 + 3;
    let product = 4 * 7;
    let remainder = 10 % 3;

    assert_eq!(sum, 8);
    assert_eq!(product, 28);
    assert_eq!(remainder, 1);
}
