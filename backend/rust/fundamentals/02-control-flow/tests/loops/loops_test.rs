#[path = "../../src/loops/loops.rs"]
mod loops;
use loops::print_to_end;    

#[test]
fn test_print_to_end() {
    let result = print_to_end(3);
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), "1\n2\n3\n");
}

#[test]
fn test_print_to_end_invalid() {
    let result = print_to_end(0);
    assert!(result.is_err());
    assert_eq!(result.unwrap_err(), "End must be greater than or equal to 1");
}
