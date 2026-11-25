use std::fs::File;

use std::io::{BufWriter, Write};

pub fn write_with_bufwriter(file_path: &str, content: &str) -> std::io::Result<()> {

    let file = File::create(file_path)?;

    let mut buf_writer = BufWriter::new(file);

    write!(buf_writer, "{}", content)?;

    buf_writer.flush()?;

    Ok(())
}

pub fn write_lines_with_bufwriter(file_path: &str, lines: &[&str]) -> std::io::Result<()> {

    let file = File::create(file_path)?;

    let mut buf_writer = BufWriter::new(file);

    for current_line in lines {

        writeln!(buf_writer, "{}", current_line)?;

    }

    buf_writer.flush()?;

    Ok(())
}
