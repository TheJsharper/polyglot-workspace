use std::fs::File;

use std::io::{BufRead, BufReader};

pub fn read_first_line(file_path: &str) -> std::io::Result<String> {

    let file = File::open(file_path)?;

    let mut reader = BufReader::new(file);

    let mut line = String::new();

    reader.read_line(&mut line)?;

    Ok(line)
}

pub fn count_lines(file_path: &str) -> std::io::Result<usize> {

    let file = File::open(file_path)?;

    let reader = BufReader::new(file);

    Ok(reader.lines().count())
}

pub fn get_bufreader_capacity(file_path: &str) -> std::io::Result<(usize, usize)> {

    let file = File::open(file_path)?;

    let mut reader = BufReader::new(file);

    let capacity = reader.capacity();

    let buffer = reader.fill_buf()?;

    let buffer_len = buffer.len();

    Ok((capacity, buffer_len))
}

pub fn get_underlying_file(file_path: &str) -> std::io::Result<()> {

    let file = File::open(file_path)?;

    let mut reader = BufReader::new(file);

    let _mutable_ref = reader.get_mut();

    Ok(())
}

pub fn get_immutable_ref(file_path: &str) -> std::io::Result<()> {

    let file = File::open(file_path)?;

    let reader = BufReader::new(file);

    let _immutable_ref = reader.get_ref();

    Ok(())
}

pub fn into_inner_demo(file_path: &str) -> std::io::Result<File> {

    let file = File::open(file_path)?;

    let reader = BufReader::new(file);

    let extracted_file = reader.into_inner();

    Ok(extracted_file)
}

pub fn peek_demo() -> std::io::Result<String> {

    use std::io::Read;

    let mut byte_slice = &b"oh, hello there"[..];

    let mut reader = BufReader::with_capacity(6, &mut byte_slice);

    let buffer = reader.fill_buf().unwrap();

    assert_eq!(&buffer[..2], b"oh");

    let mut read_buffer = [0; 4];

    reader.read(&mut read_buffer[..]).unwrap();

    assert_eq!(&read_buffer, b"oh, ");

    let mut remaining_content = String::new();

    reader.read_to_string(&mut remaining_content).unwrap();

    assert_eq!(&remaining_content, "hello there");

    Ok(remaining_content)
}
