pub fn print_to_end(end: i32) -> Result<String, String> {
    if end < 1 {
        return Err("End must be greater than or equal to 1".to_string());
    }
    let mut result = String::new();
    let mut current = 1;
    loop {
        if end == current {
            result.push_str(&format!("{}\n", current));
            break;
        }
        result.push_str(&format!("{}\n", current));
        current += 1;
    }
    Ok(result)
}
