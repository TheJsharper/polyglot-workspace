#[path = "../src/constants.rs"]
mod constants;
use constants::{MY_BOOLEAN, MY_DOUBLE, MY_LETTER, MY_NUMBER, MY_STRING};

#[test]
fn test_constants() {
    assert_eq!(MY_NUMBER, 5);
    assert!((MY_DOUBLE - 5.99).abs() < f64::EPSILON);
    assert_eq!(MY_LETTER, 'A');
    assert!(MY_BOOLEAN);
    assert_eq!(MY_STRING, "Hello");
}

#[test]
fn test_constants_values() {
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_NUMBER)
    );
    assert_eq!(
        std::any::type_name::<f64>(),
        std::any::type_name_of_val(&MY_DOUBLE)
    );
    assert_eq!(
        std::any::type_name::<char>(),
        std::any::type_name_of_val(&MY_LETTER)
    );
    assert_eq!(
        std::any::type_name::<bool>(),
        std::any::type_name_of_val(&MY_BOOLEAN)
    );
    assert_eq!(
        std::any::type_name::<&str>(),
        std::any::type_name_of_val(&MY_STRING)
    );
}
