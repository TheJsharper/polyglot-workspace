use data_structures::*;

#[test]
fn test_create_hashmap() {
    let map = create_hashmap();
    assert_eq!(map.get("Alice"), Some(&30));
    assert_eq!(map.get("Bob"), Some(&25));
}

#[test]
fn test_create_capital_cities() {
    let cities = create_capital_cities();
    assert_eq!(cities.get("England"), Some(&"London"));
    assert_eq!(cities.get("Germany"), Some(&"Berlin"));
    assert_eq!(cities.get("Norway"), Some(&"Oslo"));
}

#[test]
fn test_duplicate_key_overwrites() {
    let cities = insert_duplicate_key();
    assert_eq!(cities.get("England"), Some(&"Berlin"));
    assert_eq!(cities.len(), 1);
}

#[test]
fn test_remove_country() {
    let cities = remove_country();
    assert_eq!(cities.get("England"), None);
    assert_eq!(cities.get("Germany"), Some(&"Berlin"));
    assert_eq!(cities.get("Norway"), Some(&"Oslo"));
    assert_eq!(cities.len(), 2);
}

#[test]
fn test_iterate_capitals() {
    let cities = iterate_capitals();
    let mut count = 0;
    for (country, city) in &cities {
        assert!(!country.is_empty());
        assert!(!city.is_empty());
        count += 1;
    }
    assert_eq!(count, 3);
}

#[test]
fn test_get_city() {
    let cities = create_capital_cities();
    assert_eq!(get_city(&cities, "England"), Some(&"London"));
    assert_eq!(get_city(&cities, "France"), None);
}

#[test]
fn test_add_capital() {
    let mut cities = create_capital_cities();
    add_capital(&mut cities, "France", "Paris");
    assert_eq!(cities.get("France"), Some(&"Paris"));
}

#[test]
fn test_remove_capital() {
    let mut cities = create_capital_cities();
    let removed = remove_capital(&mut cities, "Norway");
    assert_eq!(removed, Some("Oslo"));
    assert_eq!(cities.get("Norway"), None);
}