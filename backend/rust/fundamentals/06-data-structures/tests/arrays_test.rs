use data_structures::*;

#[test]
fn test_find_array() {
    let arr = [1, 2, 3, 4, 5];
    assert_eq!(find_array(&arr, 0), Ok(1));
    assert_eq!(find_array(&arr, 4), Ok(5));
    assert_eq!(find_array(&arr, 5), Err("Index out of bounds".to_string()));
}

#[test]
fn test_find_array_mut() {
    let mut arr = vec!["a".to_string(), "b".to_string(), "c".to_string()];
    assert_eq!(find_array_mut(&mut arr, 0), Ok(&mut "a".to_string()));
    assert_eq!(find_array_mut(&mut arr, 2), Ok(&mut "c".to_string()));
    assert_eq!(find_array_mut(&mut arr, 3), Err("Index out of bounds".to_string()));
}

#[test]
fn test_tuple_person() {
    let person = create_person("John", 30, true);
    assert_eq!(get_person_name(&person), "John");
    assert_eq!(get_person_age(&person), 30);
    assert!(is_person_active(&person));
}

#[test]
fn test_tuple_direct_access() {
    let person = ("John", 30, true);
    assert_eq!(person.0, "John");
    assert_eq!(person.1, 30);
    assert_eq!(person.2, true);
}

#[test]
fn test_mutable_array() {
    let mut numbers = [1, 2, 3, 4, 5];
    assert_eq!(numbers[0], 1);
    numbers[0] = 10;
    assert_eq!(numbers[0], 10);
    assert_eq!(numbers, [10, 2, 3, 4, 5]);
}

#[test]
fn test_update_element() {
    let mut arr = [1, 2, 3, 4, 5];
    assert_eq!(update_element(&mut arr, 0, 10), Ok(()));
    assert_eq!(arr, [10, 2, 3, 4, 5]);
    assert_eq!(update_element(&mut arr, 4, 50), Ok(()));
    assert_eq!(arr, [10, 2, 3, 4, 50]);
    assert_eq!(update_element(&mut arr, 5, 99), Err("Index out of bounds".to_string()));
}

#[test]
fn test_swap_elements() {
    let mut arr = [1, 2, 3, 4, 5];
    assert_eq!(swap_elements(&mut arr, 0, 4), Ok(()));
    assert_eq!(arr, [5, 2, 3, 4, 1]);
    assert_eq!(swap_elements(&mut arr, 1, 3), Ok(()));
    assert_eq!(arr, [5, 4, 3, 2, 1]);
    assert_eq!(swap_elements(&mut arr, 0, 5), Err("Index out of bounds".to_string()));
}

#[test]
fn test_double_all() {
    let mut arr = [1, 2, 3, 4, 5];
    double_all(&mut arr);
    assert_eq!(arr, [2, 4, 6, 8, 10]);
}

#[test]
fn test_array_out_of_bounds() {
    let mut cars = ["Volvo", "BMW", "Ford"];
    assert_eq!(cars.len(), 3);
    let result = try_set_element(&mut cars, 3, "Mazda");
    assert_eq!(
        result,
        Err("Index out of bounds: the len is 3 but the index is 3".to_string())
    );
    assert_eq!(cars, ["Volvo", "BMW", "Ford"]);
}

#[test]
fn test_vec_push_instead() {
    let mut cars = vec!["Volvo", "BMW", "Ford"];
    try_set_vec(&mut cars, "Mazda");
    assert_eq!(cars, ["Volvo", "BMW", "Ford", "Mazda"]);
}
