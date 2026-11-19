#[path = "../src/matches/matches.rs"]
mod matches;

use matches::{test, get_day, get_day_static, get_day_dynamic, get_weekday};

// --- smoke test ---

#[test]
fn test_smoke() {
    assert_eq!(test(), "Hello from test!!");
}

// --- happy path ---

#[test]
fn test_all_valid_days() {
    let expected = [
        (1, "Monday"),
        (2, "Tuesday"),
        (3, "Wednesday"),
        (4, "Thursday"),
        (5, "Friday"),
        (6, "Saturday"),
        (7, "Sunday"),
    ];

    for (day, name) in expected {
        assert_eq!(get_day(day), name, "day {} should be {}", day, name);
    }
}

#[test]
fn test_get_day_static_all_valid() {
    let expected = [
        (1, "Monday"),
        (2, "Tuesday"),
        (3, "Wednesday"),
        (4, "Thursday"),
        (5, "Friday"),
        (6, "Saturday"),
        (7, "Sunday"),
    ];

    for (day, name) in expected {
        assert_eq!(get_day_static(day), name);
    }
}

#[test]
fn test_get_day_dynamic_all_valid() {
    let expected = [
        (1, "Monday"),
        (2, "Tuesday"),
        (3, "Wednesday"),
        (4, "Thursday"),
        (5, "Friday"),
        (6, "Saturday"),
        (7, "Sunday"),
    ];

    for (day, name) in expected {
        assert_eq!(get_day_dynamic(day), name);
    }
}

#[test]
fn test_get_weekday() {
    let expected = [
        (1, "Weekday"),
        (2, "Weekday"),
        (3, "Weekday"),
        (4, "Weekday"),
        (5, "Weekday"),
        (6, "Weekend"),
        (7, "Weekend"),
    ];

    for (day, label) in expected {
        assert_eq!(get_weekday(day), label);
    }
}

// --- exception / invalid input handling ---

#[test]
fn test_day_zero_is_invalid() {
    assert_eq!(get_day(0), "Invalid day");
}

#[test]
fn test_day_eight_is_invalid() {
    assert_eq!(get_day(8), "Invalid day");
}

#[test]
fn test_large_number_is_invalid() {
    assert_eq!(get_day(100), "Invalid day");
}

#[test]
fn test_max_u32_is_invalid() {
    assert_eq!(get_day(u32::MAX), "Invalid day");
}

#[test]
fn test_static_zero_is_invalid() {
    assert_eq!(get_day_static(0), "Invalid day");
}

#[test]
fn test_dynamic_zero_is_invalid() {
    assert_eq!(get_day_dynamic(0), "Invalid day");
}

#[test]
fn test_boundary_day_seven_is_valid() {
    assert_eq!(get_day(7), "Sunday");
}

#[test]
fn test_boundary_day_eight_rejects() {
    assert_eq!(get_day(8), "Invalid day");
}

#[test]
fn test_weekday_invalid_inputs() {
    assert_eq!(get_weekday(0), "Invalid day");
    assert_eq!(get_weekday(8), "Invalid day");
    assert_eq!(get_weekday(u32::MAX), "Invalid day");
}
