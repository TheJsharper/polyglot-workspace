use std::fs::File;
use std::io::{BufRead, BufReader};

pub fn read_first_line(file_path: &str) -> std::io::Result<String> {
    let f = File::open(file_path)?;
    let mut reader = BufReader::new(f);

    let mut line = String::new();
    reader.read_line(&mut line)?;
    Ok(line)
}

pub fn count_lines(file_path: &str) -> std::io::Result<usize> {
    let f = File::open(file_path)?;
    let reader = BufReader::new(f);

    Ok(reader.lines().count())
}

pub fn get_bufreader_capacity(file_path: &str) -> std::io::Result<(usize, usize)> {
    let f = File::open(file_path)?;
    let mut reader = BufReader::new(f);

    let capacity = reader.capacity();
    let buffer = reader.fill_buf()?;
    let buffer_len = buffer.len();

    Ok((capacity, buffer_len))
}

pub fn get_underlying_file(file_path: &str) -> std::io::Result<()> {
    let f1 = File::open(file_path)?;
    let mut reader = BufReader::new(f1);

    let _f2 = reader.get_mut();
    Ok(())
}

pub fn get_immutable_ref(file_path: &str) -> std::io::Result<()> {
    let f1 = File::open(file_path)?;
    let reader = BufReader::new(f1);

    let _f2 = reader.get_ref();
    Ok(())
}

pub fn into_inner_demo(file_path: &str) -> std::io::Result<File> {
    let f1 = File::open(file_path)?;
    let reader = BufReader::new(f1);

    let f2 = reader.into_inner();
    Ok(f2)
}

pub fn peek_demo() -> std::io::Result<String> {
    use std::io::Read;

    let mut bytes = &b"oh, hello there"[..];
    let mut rdr = BufReader::with_capacity(6, &mut bytes);

    // fill_buf() peeks at the buffer without consuming
    let buffer = rdr.fill_buf().unwrap();
    assert_eq!(&buffer[..2], b"oh");

    // Read consumes 4 bytes
    let mut buf = [0; 4];
    rdr.read(&mut buf[..]).unwrap();
    assert_eq!(&buf, b"oh, ");

    // Read remaining
    let mut s = String::new();
    rdr.read_to_string(&mut s).unwrap();
    assert_eq!(&s, "hello there");

    Ok(s)
}
