/// Vector Operations Module
///
/// Demonstrates dynamic, growable collections in Rust. Unlike arrays, vectors
/// can change size at runtime by adding or removing elements.
///
/// Key characteristics:
/// - Stored on the heap with contiguous memory
/// - Automatic capacity management (doubles when full)
/// - O(1) amortized push, O(1) pop, O(n) insert/remove at arbitrary index

/// Creates a vector of car brands with initial values.
pub fn create_cars() -> Vec<&'static str> {
    vec!["Volvo", "BMW", "Ford"]
}

/// Adds a car to the end of the vector using `.push()`.
/// This is O(1) amortized — occasionally O(n) when reallocating.
pub fn add_car(cars: &mut Vec<&'static str>, car: &'static str) {
    cars.push(car);
}

/// Removes and returns the last element using `.pop()`.
/// Returns `None` if the vector is empty.
pub fn remove_last_car(cars: &mut Vec<&'static str>) -> Option<&'static str> {
    cars.pop()
}

/// Safely retrieves a car by index with bounds checking.
/// Returns `Err` with a descriptive message if index is out of bounds.
pub fn get_car(cars: &[&'static str], index: usize) -> Result<&'static str, String> {
    if index >= cars.len() {
        return Err(format!(
            "Index out of bounds: the len is {} but the index is {}",
            cars.len(),
            index
        ));
    }
    Ok(cars[index])
}

/// Updates a car at the specified index.
/// Validates bounds before mutation to prevent panics.
pub fn update_car(cars: &mut Vec<&'static str>, index: usize, car: &'static str) -> Result<(), String> {
    if index >= cars.len() {
        return Err(format!(
            "Index out of bounds: the len is {} but the index is {}",
            cars.len(),
            index
        ));
    }
    cars[index] = car;
    Ok(())
}

/// Inserts a car at any position, shifting subsequent elements right.
/// O(n) operation due to element shifting.
/// Allows insertion at `len` (appending), but not beyond.
pub fn insert_fruit(fruits: &mut Vec<&'static str>, index: usize, fruit: &'static str) -> Result<(), String> {
    if index > fruits.len() {
        return Err(format!(
            "Insert index out of bounds: the len is {} but the index is {}",
            fruits.len(),
            index
        ));
    }
    fruits.insert(index, fruit);
    Ok(())
}

/// Removes and returns the car at the specified index.
/// O(n) operation — shifts all subsequent elements left.
pub fn remove_fruit(fruits: &mut Vec<&'static str>, index: usize) -> Result<&'static str, String> {
    if index >= fruits.len() {
        return Err(format!(
            "Remove index out of bounds: the len is {} but the index is {}",
            fruits.len(),
            index
        ));
    }
    Ok(fruits.remove(index))
}

/// Iterates and prints all fruits — demonstrates reference iteration.
pub fn print_all_fruits(fruits: &[&'static str]) {
    for fruit in fruits {
        println!("I like {}.", fruit);
    }
}

/// Finds the first exact match by value.
/// Returns the index or `None` if not found.
pub fn find_fruit(fruits: &[&'static str], target: &str) -> Option<usize> {
    fruits.iter().position(|&fruit| fruit == target)
}

/// Finds the first element matching a custom predicate (closure).
/// Enables flexible, reusable search logic.
///
/// # Example
/// ```
/// let fruits = vec!["apple", "banana", "cherry"];
/// let idx = find_by(&fruits, |&f| f.starts_with('b')); // Some(1)
/// ```
pub fn find_by<F>(fruits: &[&'static str], predicate: F) -> Option<usize>
where
    F: Fn(&&str) -> bool,
{
    fruits.iter().position(predicate)
}

/// Finds all indices matching a predicate.
/// Returns a vector of indices, empty if none match.
pub fn find_all_by<F>(fruits: &[&'static str], predicate: F) -> Vec<usize>
where
    F: Fn(&&str) -> bool,
{
    fruits
        .iter()
        .enumerate()
        .filter(|(_, fruit)| predicate(fruit))
        .map(|(index, _)| index)
        .collect()
}

/// Checks if the vector contains a specific value.
/// O(n) linear scan — use a `HashSet` for O(1) lookups on large collections.
pub fn contains_fruit(fruits: &[&'static str], target: &str) -> bool {
    fruits.contains(&target)
}

/// Filters fruits by a prefix, returning a new vector.
/// Demonstrates iterator combinators: `.filter()`, `.copied()`, `.collect()`.
pub fn filter_fruits<'a>(fruits: &'a [&'static str], starts_with: &str) -> Vec<&'static str> {
    fruits
        .iter()
        .filter(|&&fruit| fruit.starts_with(starts_with))
        .copied()
        .collect()
}
