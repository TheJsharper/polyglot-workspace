use data_structures::*;

#[test]
fn test_create_person() {
    let person = create_person("John", 30, true);
    assert_eq!(person.0, "John");
    assert_eq!(person.1, 30);
    assert_eq!(person.2, true);
}

#[test]
fn test_tuple_accessors() {
    let person = create_person("Alice", 25, false);
    assert_eq!(get_name(&person), "Alice");
    assert_eq!(get_age(&person), 25);
    assert!(!is_active(&person));
}

#[test]
fn test_point_operations() {
    let point = create_point(3, 4);
    assert_eq!(point, (3, 4));
    assert_eq!(point_sum(point), 7);
    let swapped = swap_point(point);
    assert_eq!(swapped, (4, 3));
}

#[test]
fn test_rgb_colors() {
    let white = create_rgb(255, 255, 255);
    assert!(is_grayscale(white));
    let gray = create_rgb(128, 128, 128);
    assert!(is_grayscale(gray));
    let red = create_rgb(255, 0, 0);
    assert!(!is_grayscale(red));
}

#[test]
fn test_tuple_destructuring() {
    let person = create_person("Bob", 40, true);
    let (name, age, active) = person;
    assert_eq!(name, "Bob");
    assert_eq!(age, 40);
    assert!(active);
}
