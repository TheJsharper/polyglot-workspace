#[path = "../../src/assignments/assignments.rs"]
mod assignments;
use assignments::*;

#[test]
fn test_assignments() {
    assert_eq!(MY_ASSIGNMENT_NUMBER, 5);
    assert_eq!(MY_ASSIGNMENT_STRING, "Hello");
    assert!(MY_ASSIGNMENT_BOOLEAN);
    assert!((MY_ASSIGNMENT_FLOAT - 3.14).abs() < f32::EPSILON);
    assert_eq!(MY_ASSIGNMENT_CHAR, 'A');
    assert_eq!(MY_ASSIGNMENT_UNSIGNED, 10);
    assert_eq!(MY_ASSIGNMENT_RESULT, Ok(42));
    assert_eq!(MY_ASSIGNMENT_OPTION, Some(10));
    assert_eq!(MY_ASSIGNMENT_INCREMENT, MY_ASSIGNMENT_NUMBER + 3);
    assert_eq!(MY_ASSIGNMENT_DECREMENT, MY_ASSIGNMENT_NUMBER - 2);
    assert_eq!(MY_ASSIGNMENT_MULTIPLY, MY_ASSIGNMENT_NUMBER * 2);
    assert_eq!(MY_ASSIGNMENT_DIVIDE, MY_ASSIGNMENT_NUMBER / 2);
    assert_eq!(MY_ASSIGNMENT_REMAINDER, MY_ASSIGNMENT_NUMBER % 2);
}

#[test]
fn test_assignments_values() {
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_NUMBER)
    );
    assert_eq!(
        std::any::type_name::<&str>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_STRING)
    );
    assert_eq!(
        std::any::type_name::<bool>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_BOOLEAN)
    );
    assert_eq!(
        std::any::type_name::<f32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_FLOAT)
    );
    assert_eq!(
        std::any::type_name::<char>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_CHAR)
    );
    assert_eq!(
        std::any::type_name::<u32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_UNSIGNED)
    );
    assert_eq!(
        std::any::type_name::<Result<i32, &str>>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_RESULT)
    );
    assert_eq!(
        std::any::type_name::<Option<i32>>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_OPTION)
    );
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_INCREMENT)
    );
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_DECREMENT)
    );
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_MULTIPLY)
    );
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_DIVIDE)
    );
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_REMAINDER)
    );
}
# [test]
fn test_assignments_values_additional() {
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_NUMBER)
    );
    assert_eq!(
        std::any::type_name::<&str>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_STRING)
    );
    assert_eq!(
        std::any::type_name::<bool>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_BOOLEAN)
    );
    assert_eq!(
        std::any::type_name::<f32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_FLOAT)
    );
    assert_eq!(
        std::any::type_name::<char>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_CHAR)
    );
    assert_eq!(
        std::any::type_name::<u32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_UNSIGNED)
    );
    assert_eq!(
        std::any::type_name::<Result<i32, &str>>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_RESULT)
    );
    assert_eq!(
        std::any::type_name::<Option<i32>>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_OPTION)
    );
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_INCREMENT)
    );
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_DECREMENT)
    );
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_MULTIPLY)
    );
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_DIVIDE)
    );
    assert_eq!(
        std::any::type_name::<i32>(),
        std::any::type_name_of_val(&MY_ASSIGNMENT_REMAINDER)
    );  
}