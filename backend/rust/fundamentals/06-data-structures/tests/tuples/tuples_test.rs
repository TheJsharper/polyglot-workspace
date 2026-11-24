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

#[test]
fn test_unpack_person() {
    let person = ("Jenny", 45, false);
    let (name, age, active) = unpack_person(person);
    assert_eq!(name, "Jenny");
    assert_eq!(age, 45);
    assert!(!active);
}

#[test]
fn test_unpack_point() {
    let point = (10, 20);
    let (x, y) = unpack_point(point);
    assert_eq!(x, 10);
    assert_eq!(y, 20);
}

#[test]
fn test_unpack_rgb() {
    let color = (255, 128, 64);
    let (r, g, b) = unpack_rgb(color);
    assert_eq!(r, 255);
    assert_eq!(g, 128);
    assert_eq!(b, 64);
}

#[test]
fn test_get_user() {
    let user = get_user();
    assert_eq!(user.0, "Liam");
    assert_eq!(user.1, 25);
    // Unpack the returned tuple
    let (name, age) = user;
    assert_eq!(name, "Liam");
    assert_eq!(age, 25);
}

#[test]
fn test_calculate_stats() {
    let numbers = [10, 20, 30, 40, 50];
    let (min, max, avg) = calculate_stats(&numbers);
    assert_eq!(min, 10);
    assert_eq!(max, 50);
    assert!((avg - 30.0).abs() < 0.001);
}

#[test]
fn test_calculate_stats_empty() {
    let numbers: [i32; 0] = [];
    let (min, max, avg) = calculate_stats(&numbers);
    assert_eq!(min, 0);
    assert_eq!(max, 0);
    assert!(avg.is_nan());
}
