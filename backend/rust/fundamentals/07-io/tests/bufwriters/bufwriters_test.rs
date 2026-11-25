use ios::*;

use std::fs;

fn get_temp_path(test_name: &str) -> String {
    format!(
        "tests/resources/temp_{}_{}.txt",
        test_name,
        std::process::id()
    )
}

fn cleanup(paths_to_clean: &[&str]) {
    for file_path in paths_to_clean {
        let _ = fs::remove_file(file_path);
    }
}

#[test]
fn test_write_with_bufwriter() {
    let file_path = get_temp_path("bufwriter");

    cleanup(&[&file_path]);

    write_with_bufwriter(&file_path, "Hello, BufWriter!").unwrap();

    let content = fs::read_to_string(&file_path).unwrap();

    assert_eq!(content, "Hello, BufWriter!");

    cleanup(&[&file_path]);
}

#[test]
fn test_write_lines_with_bufwriter() {
    let file_path = get_temp_path("bufwriter_lines");

    cleanup(&[&file_path]);

    let lines = vec!["Line 1", "Line 2", "Line 3"];

    write_lines_with_bufwriter(&file_path, &lines).unwrap();

    let content = fs::read_to_string(&file_path).unwrap();

    assert_eq!(content, "Line 1\nLine 2\nLine 3\n");

    cleanup(&[&file_path]);
}

#[test]
fn test_write_with_bufwriter_overwrites() {
    let file_path = get_temp_path("bufwriter_overwrite");

    cleanup(&[&file_path]);

    write_with_bufwriter(&file_path, "First content").unwrap();

    write_with_bufwriter(&file_path, "Second content").unwrap();

    let content = fs::read_to_string(&file_path).unwrap();

    assert_eq!(content, "Second content");

    cleanup(&[&file_path]);
}

#[test]
fn test_file_exists() {
    let file_path = get_temp_path("bufwriter_exists");

    cleanup(&[&file_path]);

    assert!(!std::path::Path::new(&file_path).exists());

    write_with_bufwriter(&file_path, "Exists!").unwrap();

    assert!(std::path::Path::new(&file_path).exists());

    cleanup(&[&file_path]);
}
