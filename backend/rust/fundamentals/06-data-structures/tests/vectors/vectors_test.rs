use data_structures::*;

#[test]
fn test_vec_create_and_push() {
    let mut cars = create_cars();
    assert_eq!(cars, ["Volvo", "BMW", "Ford"]);
    add_car(&mut cars, "Mazda");
    assert_eq!(cars, ["Volvo", "BMW", "Ford", "Mazda"]);
}

#[test]
fn test_vec_pop() {
    let mut cars = create_cars();
    let removed = remove_last_car(&mut cars);
    assert_eq!(removed, Some("Ford"));
    assert_eq!(cars, ["Volvo", "BMW"]);
}

#[test]
fn test_vec_get_car() {
    let cars = create_cars();
    assert_eq!(get_car(&cars, 0), Ok("Volvo"));
    assert_eq!(get_car(&cars, 2), Ok("Ford"));
    assert!(get_car(&cars, 3).is_err());
}

#[test]
fn test_vec_update_car() {
    let mut cars = create_cars();
    assert_eq!(update_car(&mut cars, 1, "Tesla"), Ok(()));
    assert_eq!(cars, ["Volvo", "Tesla", "Ford"]);
    assert!(update_car(&mut cars, 3, "Mazda").is_err());
}

#[test]
fn test_vec_insert() {
    let mut fruits = vec!["banana", "orange"];
    assert_eq!(insert_fruit(&mut fruits, 0, "apple"), Ok(()));
    assert_eq!(fruits, ["apple", "banana", "orange"]);
    assert_eq!(insert_fruit(&mut fruits, 2, "cherry"), Ok(()));
    assert_eq!(fruits, ["apple", "banana", "cherry", "orange"]);
    assert!(insert_fruit(&mut fruits, 5, "grape").is_err());
}

#[test]
fn test_vec_remove() {
    let mut fruits = vec!["apple", "banana", "cherry", "orange"];
    let removed = remove_fruit(&mut fruits, 1);
    assert_eq!(removed, Ok("banana"));
    assert_eq!(fruits, ["apple", "cherry", "orange"]);
    assert_eq!(remove_fruit(&mut fruits, 0), Ok("apple"));
    assert_eq!(fruits, ["cherry", "orange"]);
    assert!(remove_fruit(&mut fruits, 5).is_err());
}

#[test]
fn test_find_by_lambda() {
    let fruits = vec!["apple", "banana", "cherry", "apricot"];
    assert_eq!(find_by(&fruits, |&f| f.starts_with('a')), Some(0));
    assert_eq!(find_by(&fruits, |&f| f.len() > 6), Some(3));
    assert_eq!(find_by(&fruits, |&f| f == "grape"), None);
}

#[test]
fn test_find_all_by_lambda() {
    let fruits = vec!["apple", "banana", "cherry", "apricot", "avocado"];
    let indices = find_all_by(&fruits, |&f| f.starts_with('a'));
    assert_eq!(indices, vec![0, 3, 4]);
    let indices = find_all_by(&fruits, |&f| f.len() == 6);
    assert_eq!(indices, vec![1, 2]);
}

#[test]
fn test_contains_fruit() {
    let fruits = vec!["apple", "banana", "cherry"];
    assert!(contains_fruit(&fruits, "banana"));
    assert!(!contains_fruit(&fruits, "grape"));
}
