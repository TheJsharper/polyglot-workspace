pub fn print_sum_to_end(n: i32) -> Result<i32, String> {
    if n < 1 {
        return Err("Input must be greater than or equal to 1".to_string());
    }
    let mut sum = 0;
    let mut i = 1;
    while i <= n {
        sum += i;
        i += 1;
    }
    Ok(sum)
}

pub fn count_to_n(n: i32) -> Result<Vec<i32>, String> {
    if n < 1 {
        return Err("Input must be greater than or equal to 1".to_string());
    }
    let mut result = Vec::new();
    let mut i = 1;
    while i <= n {
        result.push(i);
        i += 1;
    }
    Ok(result)
}

pub fn sum_to_n(n: i32) -> Result<i32, String> {
    if n < 1 {
        return Err("Input must be greater than or equal to 1".to_string());
    }
    let mut sum = 0;
    let mut i = 1;
    while i <= n {
        sum += i;
        i += 1;
    }
    Ok(sum)
}

pub fn factorial(n: i32) -> Result<i32, String> {
    if n < 1 {
        return Err("Input must be greater than or equal to 1".to_string());
    }
    let mut result = 1;
    let mut i = 1;
    while i <= n {
        result *= i;
        i += 1;
    }
    Ok(result)
}

pub fn fibonacci(n: i32) -> Result<i32, String> {
    if n < 1 {
        return Err("Input must be greater than or equal to 1".to_string());
    }
    let mut a = 0;
    let mut b = 1;
    let mut i = 1;
    while i < n {
        let temp = b;
        b = a + b;
        a = temp;
        i += 1;
    }
    Ok(b)
}

pub fn is_prime(n: i32) -> Result<bool, String> {
    if n < 2 {
        return Err("Input must be greater than or equal to 2".to_string());
    }
    let mut i = 2;
    while i * i <= n {
        if n % i == 0 {
            return Ok(false);
        }
        i += 1;
    }
    Ok(true)
}

pub fn gcd(mut a: i32, mut b: i32) -> Result<i32, String> {
    if a < 1 || b < 1 {
        return Err("Input must be greater than or equal to 1".to_string());
    }
    while b != 0 {
        let temp = b;
        b = a % b;
        a = temp;
    }
    Ok(a)
}

pub fn lcm(a: i32, b: i32) -> Result<i32, String> {
    if a < 1 || b < 1 {
        return Err("Input must be greater than or equal to 1".to_string());
    }
    let gcd_value = gcd(a, b)?;
    Ok(a * b / gcd_value)
}

