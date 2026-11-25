use std::fs::File;

use std::io::{BufRead, BufReader, BufWriter};

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

pub fn seek_in_file(file_path: &str, position: u64) -> std::io::Result<()> {

    if !Path::new(file_path).exists() {

        return Err(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("File not found: {}", file_path),
        ));
    }

    let mut file = File::open(file_path)?;

    use std::io::Seek;
    use std::io::SeekFrom;

    file.seek(SeekFrom::Start(position))?;

    Ok(())
}

pub fn read_lines_from_bufreader_to_string_buf(file_path: &str) -> std::io::Result<String> {

    if !Path::new(file_path).exists() {

        return Err(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("File not found: {}", file_path),
        ));
    }

    let file = File::open(file_path)?;

    let mut reader = BufReader::new(file);

    let mut content = String::new();

    use std::io::Read;
    reader.read_to_string(&mut content)?;

    Ok(content)
}

pub fn read_lines_from_std_in_to_string_buf() -> std::io::Result<String> {

    let stdin = std::io::stdin();
    let mut reader = BufReader::new(stdin);

    let mut content = String::new();

    use std::io::Read;
    reader.read_to_string(&mut content)?;

    Ok(content)
}

pub fn read_lines_from_std_in_to_string_buf_with_iterator() -> std::io::Result<String> {

    let stdin = std::io::stdin();
    let reader = BufReader::new(stdin);

    let mut content = String::new();

    for line in reader.lines() {
        content.push_str(&line?);
        content.push('\n');
    }

    Ok(content)
}
pub fn read_write_with_pipe(input_file: &str, output_file: &str) -> std::io::Result<()> {

    if !Path::new(input_file).exists() {

        return Err(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("File not found: {}", input_file),
        ));
    }

    let input = File::open(input_file)?;
    let mut reader = BufReader::new(input);

    let output = File::create(output_file)?;
    let mut writer = BufWriter::new(output);

    use std::io::Write;
    let mut buffer = String::new();
    while reader.read_line(&mut buffer)? > 0 {
        writer.write_all(buffer.as_bytes())?;
        buffer.clear();
    }

    Ok(())
}