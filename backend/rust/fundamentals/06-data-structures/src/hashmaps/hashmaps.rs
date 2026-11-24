use std::collections::HashMap;

pub fn create_hashmap() -> HashMap<String, i32> {
    let mut map = HashMap::new();
    map.insert(String::from("Alice"), 30);
    map.insert(String::from("Bob"), 25);
    map
}

pub fn create_capital_cities() -> HashMap<&'static str, &'static str> {
    let mut capital_cities = HashMap::new();
    capital_cities.insert("England", "London");
    capital_cities.insert("Germany", "Berlin");
    capital_cities.insert("Norway", "Oslo");
    capital_cities
}

pub fn insert_duplicate_key() -> HashMap<&'static str, &'static str> {
    let mut capital_cities = HashMap::new();
    capital_cities.insert("England", "London");
    capital_cities.insert("England", "Berlin");
    capital_cities
}

pub fn remove_country() -> HashMap<&'static str, &'static str> {
    let mut capital_cities = HashMap::new();
    capital_cities.insert("England", "London");
    capital_cities.insert("Germany", "Berlin");
    capital_cities.insert("Norway", "Oslo");
    capital_cities.remove("England");
    capital_cities
}

pub fn iterate_capitals() -> HashMap<&'static str, &'static str> {
    let mut capital_cities = HashMap::new();
    capital_cities.insert("England", "London");
    capital_cities.insert("Germany", "Berlin");
    capital_cities.insert("Norway", "Oslo");
    capital_cities
}

pub fn get_city<'a>(capital_cities: &'a HashMap<&'a str, &'a str>, country: &'a str) -> Option<&'a &'a str> {
    capital_cities.get(country)
}

pub fn add_capital(capital_cities: &mut HashMap<&str, &str>, country: &'static str, city: &'static str) {
    capital_cities.insert(country, city);
}

pub fn remove_capital(capital_cities: &mut HashMap<&'static str, &'static str>, country: &str) -> Option<&'static str> {
    capital_cities.remove(country)
}