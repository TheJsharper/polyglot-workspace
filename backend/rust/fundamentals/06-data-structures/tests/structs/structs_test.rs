use data_structures::*;

#[test]
fn test_create_person() {
    let person = create_person_struct("Alice".to_string(), 20);
    assert_eq!(person.name, "Alice");
    assert_eq!(person.age, 20);
    assert!(person.can_vote);
}

#[test]
fn test_create_person_underage() {
    let person = create_person_struct("Bob".to_string(), 16);
    assert_eq!(person.name, "Bob");
    assert_eq!(person.age, 16);
    assert!(!person.can_vote);
}

#[test]
fn test_mutate_person() {
    let person = mutate_person();
    assert_eq!(person.name, "Jane");
    assert_eq!(person.age, 36);
    assert!(person.can_vote);
}

#[test]
fn test_struct_update_syntax() {
    let (person1, person2) = struct_update_syntax();
    assert_eq!(person1.name, "Alice");
    assert_eq!(person2.name, "Bob");
    assert_eq!(person2.age, person1.age);
    assert_eq!(person2.can_vote, person1.can_vote);
}

#[test]
fn test_struct_with_method() {
    let person = struct_with_method();
    assert_eq!(person.name, "Charlie");
    assert_eq!(person.age, 26);
    assert!(person.can_vote);
}