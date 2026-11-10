#[path = "../../src/constants/constants.rs"]
mod constants;
use constants::{
    MY_BOOL, MY_BOOLEAN, MY_CHAR, MY_DOUBLE, MY_FLOAT, MY_LETTER, MY_NONE, MY_NUMBER, MY_OPTION,
    MY_RESULT, MY_RESULT_ERR, MY_STRING, MY_STRING_EMPTY, MY_UNSIGNED,
};

#[test]
fn test_constants() {
    assert_eq!(MY_NUMBER, 5);

    assert_eq!(MY_UNSIGNED, 10);

    assert!((MY_DOUBLE - 5.99).abs() < f64::EPSILON);

    assert_eq!(MY_LETTER, 'A');

    assert!(MY_BOOLEAN);

    assert_eq!(MY_STRING, "Hello");

    assert!((MY_FLOAT - 3.14).abs() < f32::EPSILON);

    assert!(!MY_BOOL);

    assert_eq!(MY_STRING_EMPTY, "");

    assert_eq!(MY_CHAR, 'Z');

    assert_eq!(MY_OPTION, Some(10));

    assert_eq!(MY_NONE, None);

    assert_eq!(MY_RESULT, Ok(42));

    assert_eq!(MY_RESULT_ERR, Err("Error"));
}

#[test]
fn test_constants_values() {
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_NUMBER)
    );

    assert_eq!(
        std::any::type_name::<u32>(),
        std::any::type_name_of_val(&MY_UNSIGNED)
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

    assert_eq!(
        std::any::type_name::<f32>(),
        std::any::type_name_of_val(&MY_FLOAT)
    );

    assert_eq!(
        std::any::type_name::<bool>(),
        std::any::type_name_of_val(&MY_BOOL)
    );

    assert_eq!(
        std::any::type_name::<&str>(),
        std::any::type_name_of_val(&MY_STRING_EMPTY)
    );

    assert_eq!(
        std::any::type_name::<char>(),
        std::any::type_name_of_val(&MY_CHAR)
    );

    assert_eq!(
        std::any::type_name::<Option<i32>>(),
        std::any::type_name_of_val(&MY_OPTION)
    );

    assert_eq!(
        std::any::type_name::<Option<i32>>(),
        std::any::type_name_of_val(&MY_NONE)
    );

    assert_eq!(
        std::any::type_name::<Result<i32, &str>>(),
        std::any::type_name_of_val(&MY_RESULT)
    );

    assert_eq!(
        std::any::type_name::<Result<i32, &str>>(),
        std::any::type_name_of_val(&MY_RESULT_ERR)
    );
}
