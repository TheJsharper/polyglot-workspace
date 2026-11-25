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
fn test_write_file() {
    let file_path = get_temp_path("write");

    cleanup(&[&file_path]);

    write_file(&file_path, "Hello, file!").unwrap();

    let content = fs::read_to_string(&file_path).unwrap();

    assert_eq!(content, "Hello, file!");

    cleanup(&[&file_path]);
}

#[test]
fn test_write_file_lines() {
    let file_path = get_temp_path("lines");

    cleanup(&[&file_path]);

    let lines = vec![
        "Line 1".to_string(),
        "Line 2".to_string(),
        "Line 3".to_string(),
    ];

    write_file_lines(&file_path, &lines).unwrap();

    let content = fs::read_to_string(&file_path).unwrap();

    assert_eq!(content, "Line 1\nLine 2\nLine 3\n");

    cleanup(&[&file_path]);
}

#[test]
fn test_append_to_file() {
    let file_path = get_temp_path("append");

    cleanup(&[&file_path]);

    write_file(&file_path, "First line").unwrap();

    append_to_file(&file_path, "Second line").unwrap();

    append_to_file(&file_path, "Third line").unwrap();

    let content = fs::read_to_string(&file_path).unwrap();

    assert!(content.contains("First line"));

    assert!(content.contains("Second line"));

    assert!(content.contains("Third line"));

    cleanup(&[&file_path]);
}

#[test]
fn test_copy_file() {
    let source_path = get_temp_path("copy_src");

    let destination_path = get_temp_path("copy_dest");

    cleanup(&[&source_path, &destination_path]);

    write_file(&source_path, "Copy this content").unwrap();

    copy_file(&source_path, &destination_path).unwrap();

    let original_content = fs::read_to_string(&source_path).unwrap();

    let copied_content = fs::read_to_string(&destination_path).unwrap();

    assert_eq!(original_content, copied_content);

    cleanup(&[&source_path, &destination_path]);
}

#[test]
fn test_delete_file() {
    let file_path = get_temp_path("delete");

    cleanup(&[&file_path]);

    write_file(&file_path, "Delete me").unwrap();

    assert!(file_exists(&file_path));

    delete_file(&file_path).unwrap();

    assert!(!file_exists(&file_path));

    cleanup(&[&file_path]);
}

#[test]
fn test_file_exists() {
    let file_path = get_temp_path("exists");

    cleanup(&[&file_path]);

    assert!(!file_exists(&file_path));

    write_file(&file_path, "Exists!").unwrap();

    assert!(file_exists(&file_path));

    cleanup(&[&file_path]);
}
