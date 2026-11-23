pub fn demonstrate_basic_string() -> &'static str {
    "Hello, world!"
}

pub fn demonstrate_basic_from_str() -> String {
    "Hello, world!".to_string()
}

pub fn demostrate_basic_from_method() -> String {
    String::from("Hello, world!")
}

pub fn demonstrate_basic_string_with_format() -> String {
    format!("Hello, world!")
}

pub fn demonstrate_basic_string_with_argument_and_format(name: &str) -> String {
    format!("Hello, {}!", name)
}

pub fn demonstrate_basic_string_with_multiple_arguments_and_format(name: &str, age: u32) -> String {
    format!("Hello, {}! You are {} years old.", name, age)
}

pub fn demonstrate_basic_string_with_named_arguments_and_format(name: &str, age: u32) -> String {
    format!(
        "Hello, {name}! You are {age} years old.",
        name = name,
        age = age
    )
}

pub fn demostrate_basic_mutate_string() -> String {
    let mut s = String::from("Hello");
    s.push_str(", world!");
    s
}
pub fn demonstrate_basic_concatenate_strings() -> String {
    let s1 = String::from("Hello");
    let s2 = String::from(", world!");
    let s3 = s1 + &s2;
    s3
}
pub fn demonstrate_basic_concatenate_strings_with_format() -> String {
    let s1 = String::from("Hello");
    let s2 = String::from(", world!");
    format!("{}{}", s1, s2)
}
