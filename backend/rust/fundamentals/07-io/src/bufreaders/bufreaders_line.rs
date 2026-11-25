use std::fs::File;

use std::io::{BufRead, BufReader};

use std::path::Path;


pub fn read_lines_from_bufreader(file_path: &str) -> std::io::Result<Vec<String>> {

    if !Path::new(file_path).exists() {

        return Err(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("File not found: {}", file_path),
        ));
    }

    let file = File::open(file_path)?;

    let reader = BufReader::new(file);

    let mut lines = Vec::new();

    for current_line in reader.lines() {

        lines.push(current_line?);
    }

    Ok(lines)
}


pub fn read_lines_with_custom_capacity(file_path: &str, capacity: usize) -> std::io::Result<Vec<String>> {

    if !Path::new(file_path).exists() {

        return Err(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("File not found: {}", file_path),
        ));
    }

    let file = File::open(file_path)?;

    let reader = BufReader::with_capacity(capacity, file);

    let mut lines = Vec::new();

    for current_line in reader.lines() {

        lines.push(current_line?);
    }

    Ok(lines)
}
