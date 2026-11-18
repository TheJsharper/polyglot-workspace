//use std::fmt::format;

// Comparison operators return bool
pub const LESS_THAN: bool = 10 < 11;

pub const GREATER_THAN: bool = 15 > 12;

pub const LESS_THAN_OR_EQUAL: bool = 7 <= 7;

pub const GREATER_THAN_OR_EQUAL: bool = 10 >= 9;

pub const EQUAL_TO: bool = 5 == 5;

pub const NOT_EQUAL_TO: bool = 3 != 4;

pub const fn is_greater(a: i32, b: i32) -> bool {
    a > b
}
pub fn comparison_message(a:i32, b:i32)-> String{
    if a > b {
        format!("{a} is greater than {b}")
    } else{
        format!("{a} is not greater than {b}")
    }
}
pub fn greeting_message(time:i16)-> String{
    if time < 18{
        String::from("Good Day")
    }else {
        String::from("Good evening")
    }
    
}