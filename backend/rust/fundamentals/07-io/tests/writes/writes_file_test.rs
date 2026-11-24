use ios::*;
use std::fs;

fn get_temp_path(name: &str) -> String {
    format!("tests/resources/temp_{}_{}.txt", name, std::process::id())
}

fn cleanup(paths: &[&str]) {
    for path in paths {
        let _ = fs::remove_file(path);
    }
}

#[test]
fn test_write_file() {
    let path = get_temp_path("write");
    cleanup(&[&path]);
    write_file(&path, "Hello, file!").unwrap();
    let content = fs::read_to_string(&path).unwrap();
    assert_eq!(content, "Hello, file!");
    cleanup(&[&path]);
}

#[test]
fn test_write_file_lines() {
    let path = get_temp_path("lines");
    cleanup(&[&path]);
    let lines = vec![
        "Line 1".to_string(),
        "Line 2".to_string(),
        "Line 3".to_string(),
    ];
    write_file_lines(&path, &lines).unwrap();
    let content = fs::read_to_string(&path).unwrap();
    assert_eq!(content, "Line 1\nLine 2\nLine 3\n");
    cleanup(&[&path]);
}

#[test]
fn test_append_to_file() {
    let path = get_temp_path("append");
    cleanup(&[&path]);
    write_file(&path, "First line").unwrap();
    append_to_file(&path, "Second line").unwrap();
    append_to_file(&path, "Third line").unwrap();
    let content = fs::read_to_string(&path).unwrap();
    assert!(content.contains("First line"));
    assert!(content.contains("Second line"));
    assert!(content.contains("Third line"));
    cleanup(&[&path]);
}

#[test]
fn test_copy_file() {
    let src = get_temp_path("copy_src");
    let dest = get_temp_path("copy_dest");
    cleanup(&[&src, &dest]);
    write_file(&src, "Copy this content").unwrap();
    copy_file(&src, &dest).unwrap();
    let original = fs::read_to_string(&src).unwrap();
    let copied = fs::read_to_string(&dest).unwrap();
    assert_eq!(original, copied);
    cleanup(&[&src, &dest]);
}

#[test]
fn test_delete_file() {
    let path = get_temp_path("delete");
    cleanup(&[&path]);
    write_file(&path, "Delete me").unwrap();
    assert!(file_exists(&path));
    delete_file(&path).unwrap();
    assert!(!file_exists(&path));
    cleanup(&[&path]);
}

#[test]
fn test_file_exists() {
    let path = get_temp_path("exists");
    cleanup(&[&path]);
    assert!(!file_exists(&path));
    write_file(&path, "Exists!").unwrap();
    assert!(file_exists(&path));
    cleanup(&[&path]);
}
