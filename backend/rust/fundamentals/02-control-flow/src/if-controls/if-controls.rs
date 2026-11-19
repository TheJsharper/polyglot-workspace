pub fn test() -> &'static str {
    "Hello from test!!"
}

pub fn greater(a: i32, b: i32) -> i32 {
    if a > b {
        a
    } else {
        b
    }
}

pub fn is_even(n: i32) -> bool {
    if n % 2 == 0 {
        true
    } else {
        false
    }
}

pub fn is_odd(n: i32) -> bool {
    if n % 2 != 0 {
        true
    } else {
        false
    }
}

pub fn is_positive(n: i32) -> bool {
    if n > 0 {
        true
    } else {
        false
    }
}

pub fn is_negative(n: i32) -> bool {
    if n < 0 {
        true
    } else {
        false
    }
}
pub fn is_zero(n: i32) -> bool {
    if n == 0 {
        true
    } else {
        false
    }
}

pub fn greater_than(a: i32, b: i32) -> bool {
    if a > b {
        true
    } else {
        false
    }
}

pub fn less_than(a: i32, b: i32) -> bool {
    if a < b {
        true
    } else {
        false
    }
}
pub fn greater_than_or_equal(a: i32, b: i32) -> bool {
    if a >= b {
        true
    } else {
        false
    }
}
pub fn less_than_or_equal(a: i32, b: i32) -> bool {
    if a <= b {
        true
    } else {
        false
    }
}

pub fn equal(a: i32, b: i32) -> bool {
    if a == b {
        true
    } else {
        false
    }
}