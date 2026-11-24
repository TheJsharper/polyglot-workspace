use std::fs;
use std::io::Write;
use std::path::Path;

pub fn write_file(file_path: &str, content: &str) -> std::io::Result<()> {
    fs::write(file_path, content)
}

pub fn write_file_lines(file_path: &str, lines: &[String]) -> std::io::Result<()> {
    let content = lines.join("\n");
    fs::write(file_path, content + "\n")
}

pub fn append_to_file(file_path: &str, content: &str) -> std::io::Result<()> {
    let mut file = fs::OpenOptions::new()
        .create(true)
        .append(true)
        .open(file_path)?;
    writeln!(file, "{}", content)?;
    Ok(())
}

pub fn copy_file(source: &str, destination: &str) -> std::io::Result<()> {
    fs::copy(source, destination)?;
    Ok(())
}

pub fn delete_file(file_path: &str) -> std::io::Result<()> {
    fs::remove_file(file_path)
}

pub fn file_exists(file_path: &str) -> bool {
    Path::new(file_path).exists()
}
