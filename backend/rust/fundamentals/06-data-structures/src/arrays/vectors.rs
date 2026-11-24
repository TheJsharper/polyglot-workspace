pub fn create_cars() -> Vec<&'static str> {
    vec!["Volvo", "BMW", "Ford"]
}

pub fn add_car(cars: &mut Vec<&'static str>, car: &'static str) {
    cars.push(car);
}

pub fn remove_last_car(cars: &mut Vec<&'static str>) -> Option<&'static str> {
    cars.pop()
}

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

pub fn print_all_fruits(fruits: &[&'static str]) {
    for fruit in fruits {
        println!("I like {}.", fruit);
    }
}

pub fn find_fruit(fruits: &[&'static str], target: &str) -> Option<usize> {
    fruits.iter().position(|&fruit| fruit == target)
}

pub fn find_by<F>(fruits: &[&'static str], predicate: F) -> Option<usize>
where
    F: Fn(&&str) -> bool,
{
    fruits.iter().position(predicate)
}

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

pub fn contains_fruit(fruits: &[&'static str], target: &str) -> bool {
    fruits.contains(&target)
}

pub fn filter_fruits<'a>(fruits: &'a [&'static str], starts_with: &str) -> Vec<&'static str> {
    fruits
        .iter()
        .filter(|&&fruit| fruit.starts_with(starts_with))
        .copied()
        .collect()
}
