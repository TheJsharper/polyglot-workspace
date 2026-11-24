pub fn create_person(name: &str, age: i32, is_active: bool) -> (&str, i32, bool) {
    (name, age, is_active)
}

pub fn create_point(x: i32, y: i32) -> (i32, i32) {
    (x, y)
}

pub fn get_name<'a>(person: &'a (&'a str, i32, bool)) -> &'a str {
    person.0
}

pub fn get_age(person: &(&str, i32, bool)) -> i32 {
    person.1
}

pub fn is_active(person: &(&str, i32, bool)) -> bool {
    person.2
}

pub fn swap_point(point: (i32, i32)) -> (i32, i32) {
    (point.1, point.0)
}

pub fn point_sum(point: (i32, i32)) -> i32 {
    point.0 + point.1
}

pub fn create_rgb(r: u8, g: u8, b: u8) -> (u8, u8, u8) {
    (r, g, b)
}

pub fn is_grayscale(color: (u8, u8, u8)) -> bool {
    color.0 == color.1 && color.1 == color.2
}
