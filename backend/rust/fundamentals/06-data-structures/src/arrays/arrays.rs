pub fn find_array(arr: &[i32], index: usize) -> Result<i32, String> {
    if index >= arr.len() {
        return Err("Index out of bounds".to_string());
    }
    let found: i32 = arr[index];
    Ok(found)
}

pub fn find_array_mut(arr: &mut [String], index: usize) -> Result<&mut String, String> {
    if index >= arr.len() {
        return Err("Index out of bounds".to_string());
    }
    Ok(&mut arr[index])
}

pub fn update_element(arr: &mut [i32], index: usize, value: i32) -> Result<(), String> {
    if index >= arr.len() {
        return Err("Index out of bounds".to_string());
    }
    arr[index] = value;
    Ok(())
}

pub fn swap_elements(arr: &mut [i32], index_a: usize, index_b: usize) -> Result<(), String> {
    if index_a >= arr.len() || index_b >= arr.len() {
        return Err("Index out of bounds".to_string());
    }
    arr.swap(index_a, index_b);
    Ok(())
}

pub fn double_all(arr: &mut [i32]) {
    for i in 0..arr.len() {
        arr[i] *= 2;
    }
}

pub fn try_set_element<'a>(arr: &mut [&'a str; 3], index: usize, value: &'a str) -> Result<(), String> {
    if index >= arr.len() {
        return Err(format!(
            "Index out of bounds: the len is {} but the index is {}",
            arr.len(),
            index
        ));
    }
    arr[index] = value;
    Ok(())
}

pub fn try_set_vec<'a>(cars: &mut Vec<&'a str>, value: &'a str) {
    cars.push(value);
}