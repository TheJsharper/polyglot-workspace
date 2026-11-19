pub fn test() -> &'static str {
    "Hello from test!!"
}

pub fn get_day_static(day: u32) -> &'static str {
    match day {
        1 => "Monday",
        2 => "Tuesday",
        3 => "Wednesday",
        4 => "Thursday",
        5 => "Friday",
        6 => "Saturday",
        7 => "Sunday",
        _ => "Invalid day",
    }
}

pub fn get_day_dynamic(day: u32) -> String {
    match day {
        1 => "Monday".to_string(),
        2 => "Tuesday".to_string(),
        3 => "Wednesday".to_string(),
        4 => "Thursday".to_string(),
        5 => "Friday".to_string(),
        6 => "Saturday".to_string(),
        7 => "Sunday".to_string(),
        _ => "Invalid day".to_string(),
    }
}

pub fn get_day(day: u32) -> String {
    if day > 7 || day == 0 {
        return "Invalid day".to_string();
    }
    match day {
        1 => "Monday".to_string(),
        2 => "Tuesday".to_string(),
        3 => "Wednesday".to_string(),
        4 => "Thursday".to_string(),
        5 => "Friday".to_string(),
        6 => "Saturday".to_string(),
        7 => "Sunday".to_string(),
        _ => "Invalid day".to_string(),
    }
}

pub fn get_weekday(day: u32) -> &'static str {
    if day == 0 || day > 7 {
        return "Invalid day";
    }
    match day {
        1 | 2 | 3 | 4 | 5 => "Weekday",
        6 | 7 => "Weekend",
        _ => "Invalid day",
    }
}
