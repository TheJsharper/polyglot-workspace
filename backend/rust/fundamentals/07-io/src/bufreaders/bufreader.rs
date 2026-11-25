use std::fs::File;

use std::io::{BufRead, BufReader};

use std::path::Path;


pub fn read_first_line(file_path: &str) -> std::io::Result<String> {

    if !Path::new(file_path).exists() {

        return Err(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("File not found: {}", file_path),
        ));
    }

    let file = File::open(file_path)?;

    let mut reader = BufReader::new(file);

    let mut first_line = String::new();

    reader.read_line(&mut first_line)?;

    Ok(first_line)
}


pub fn count_lines(file_path: &str) -> std::io::Result<usize> {

    if !Path::new(file_path).exists() {

        return Err(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("File not found: {}", file_path),
        ));
    }

    let file = File::open(file_path)?;

    let reader = BufReader::new(file);

    Ok(reader.lines().count())
}


pub fn get_bufreader_capacity(file_path: &str) -> std::io::Result<(usize, usize)> {

    if !Path::new(file_path).exists() {

        return Err(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("File not found: {}", file_path),
        ));
    }

    let file = File::open(file_path)?;

    let mut reader = BufReader::new(file);

    let buffer_capacity = reader.capacity();

    let buffer = reader.fill_buf()?;

    let buffer_len = buffer.len();

    Ok((buffer_capacity, buffer_len))
}


pub fn get_underlying_file(file_path: &str) -> std::io::Result<()> {

    if !Path::new(file_path).exists() {

        return Err(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("File not found: {}", file_path),
        ));
    }

    let file = File::open(file_path)?;

    let mut reader = BufReader::new(file);

    let _mutable_ref = reader.get_mut();

    Ok(())
}


pub fn get_immutable_ref(file_path: &str) -> std::io::Result<()> {

    if !Path::new(file_path).exists() {

        return Err(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("File not found: {}", file_path),
        ));
    }

    let file = File::open(file_path)?;

    let reader = BufReader::new(file);

    let _immutable_ref = reader.get_ref();

    Ok(())
}


pub fn into_inner_demo(file_path: &str) -> std::io::Result<File> {

    if !Path::new(file_path).exists() {

        return Err(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("File not found: {}", file_path),
        ));
    }

    let file = File::open(file_path)?;

    let reader = BufReader::new(file);

    let extracted_file = reader.into_inner();

    Ok(extracted_file)
}


pub fn peek_demo() -> std::io::Result<String> {

    use std::io::Read;

    let mut byte_slice = &b"oh, hello there"[..];

    let mut reader = BufReader::with_capacity(6, &mut byte_slice);

    let buffer = reader.fill_buf()?;

    if buffer.len() < 2 {

        return Err(std::io::Error::new(
            std::io::ErrorKind::UnexpectedEof,
            "Buffer has fewer than 2 bytes",
        ));
    }

    assert_eq!(&buffer[..2], b"oh");

    let mut read_buffer = [0; 4];

    reader.read(&mut read_buffer[..])?;

    assert_eq!(&read_buffer, b"oh, ");

    let mut remaining_content = String::new();

    reader.read_to_string(&mut remaining_content)?;

    assert_eq!(&remaining_content, "hello there");

    Ok(remaining_content)
}
