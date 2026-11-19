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

pub fn print_to_start(start: i32) -> Result<String, String> {
    if start < 1 {
        return Err("Start must be greater than or equal to 1".to_string());
    }
    let mut result = String::new();
    let mut current = start;
    loop {
        if current == 1 {
            result.push_str(&format!("{}\n", current));
            break;
        }
        result.push_str(&format!("{}\n", current));
        current -= 1;
    }
    Ok(result)
}

pub fn print_range(start: i32, end: i32) -> Result<i32, String> {
    if start < 1 || end < 1 {
        return Err("Start and end must be greater than or equal to 1".to_string());
    }
    if start > end {
        return Err("Start must be less than or equal to end".to_string());
    }
    let mut sum = 0;
    let mut current = start;
    loop {
        sum += current;
        if current == end {
            break;
        }
        current += 1;
    }
    Ok(sum)
}

pub fn print_sum_to_end(end: i32) -> Result<i32, String> {
    if end < 1 {
        return Err("End must be greater than or equal to 1".to_string());
    }
    let mut sum = 0;
    let mut current = 1;
    loop {
        sum += current;
        if current == end {
            break;
        }
        current += 1;
    }
    Ok(sum)
}

pub fn print_sum_to_start(start: i32) -> Result<i32, String> {
    if start < 1 {
        return Err("Start must be greater than or equal to 1".to_string());
    }
    let mut sum = 0;
    let mut current = start;
    loop {
        sum += current;
        if current == 1 {
            break;
        }
        current -= 1;
    }
    Ok(sum)
}

pub fn print_sum_range(start: i32, end: i32) -> Result<i32, String> {
    if start < 1 || end < 1 {
        return Err("Start and end must be greater than or equal to 1".to_string());
    }
    if start > end {
        return Err("Start must be less than or equal to end".to_string());
    }
    let mut sum = 0;
    let mut current = start;
    loop {
        sum += current;
        if current == end {
            break;
        }
        current += 1;
    }
    Ok(sum)
}
