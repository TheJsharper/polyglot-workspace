pub fn demonstrate_for_loops(inclusive: i32) -> Result<i32, String> {
    if inclusive < 0 {
        return Err("Inclusive value must be non-negative".to_string());
    }
    let mut sum = 0;
    for i in 0..=inclusive {
        sum += i;
    }
    Ok(sum)
}

pub fn demonstrate_for_loops_with_step(inclusive: i32) ->Result<i32, String> {
    if inclusive < 0 {
        return Err("Inclusive value must be non-negative".to_string());
    }
    let mut sum = 0;
    for i in (0..=inclusive).step_by(2) {
        sum += i;
    }
    Ok(sum)
}

pub fn demonstrate_for_loops_reverse(inclusive: i32) -> Result<i32, String> {
    if inclusive < 0 {
        return Err("Inclusive value must be non-negative".to_string());
    }
    let mut sum = 0;
    for i in (0..=inclusive).rev() {
        sum += i;
    }
    Ok(sum)
}

pub fn demonstrate_for_loops_reverse_with_step(inclusive: i32) -> Result<i32, String> {
    if inclusive < 0 {
        return Err("Inclusive value must be non-negative".to_string());
    }
    let mut sum = 0;
    for i in (0..=inclusive).rev().step_by(2) {
        sum += i;
    }
    Ok(sum)
}
pub fn demonstrate_for_loops_with_step_reverse(inclusive: i32) -> Result<i32, String> {
    if inclusive < 0 {
        return Err("Inclusive value must be non-negative".to_string());
    }
    let mut sum = 0;
    for i in (0..=inclusive).rev().step_by(2) {
        sum += i;
    }
    Ok(sum)
}

pub fn demonstrate_for_loops_with_step_reverse_with_from_inclusive( from: i32, inclusive: i32) -> Result<i32, String> {
    if inclusive < 0  || from < 0  || from > inclusive {
        return Err("Inclusive and from values must be non-negative and from must be less than or equal to inclusive".to_string());
    }
    let mut sum = 0;
    for i in (from..=inclusive).rev().step_by(2) {
        sum += i;
    }
    Ok(sum)
}

pub fn demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step(from: i32, inclusive: i32, step: usize) -> Result<i32, String> {
    if inclusive < 0  || from < 0  || from > inclusive {
        return Err("Inclusive and from values must be non-negative and from must be less than or equal to inclusive".to_string());
    }
    let mut sum = 0;
    for i in (from..=inclusive).rev().step_by(step) {
        sum += i;
    }
    Ok(sum)
}

pub fn demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse(from: i32, inclusive: i32, step: usize) -> Result<i32, String> {
    if inclusive < 0  || from < 0  || from > inclusive {
        return Err("Inclusive and from values must be non-negative and from must be less than or equal to inclusive".to_string());
    }
    let mut sum = 0;
    for i in (from..=inclusive).rev().step_by(step) {
        sum += i;
    }
    Ok(sum)
}
pub fn demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse_and_step(from: i32, inclusive: i32, step: usize) -> Result<i32, String> {
    if inclusive < 0  || from < 0  || from > inclusive {
        return Err("Inclusive and from values must be non-negative and from must be less than or equal to inclusive".to_string());
    }
    let mut sum = 0;
    for i in (from..=inclusive).rev().step_by(step) {
        sum += i;
    }
    Ok(sum)
}
pub fn demonstrate_for_loops_with_step_reverse_with_from_inclusive_and_step_and_reverse_and_step_and_reverse(from: i32, inclusive: i32, step: usize) -> Result<i32, String> {
    if inclusive < 0  || from < 0  || from > inclusive {
        return Err("Inclusive and from values must be non-negative and from must be less than or equal to inclusive".to_string());
    }
    let mut sum = 0;
    for i in (from..=inclusive).rev().step_by(step) {
        sum += i;
    }
    Ok(sum)
}   

