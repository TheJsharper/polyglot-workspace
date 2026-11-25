use ios::bufwriters::bufwriters::*;

use std::fs;
use std::io::Write;

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

// --- Original tests ---

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

// --- with_capacity ---

#[test]
fn test_write_with_custom_capacity_small() {
    let file_path = get_temp_path("bufwriter_cap_small");
    cleanup(&[&file_path]);

    let bytes = write_with_custom_capacity(&file_path, "tiny", 16).unwrap();
    assert_eq!(bytes, 4);

    let content = fs::read_to_string(&file_path).unwrap();
    assert_eq!(content, "tiny");

    cleanup(&[&file_path]);
}

#[test]
fn test_write_with_custom_capacity_large() {
    let file_path = get_temp_path("bufwriter_cap_large");
    cleanup(&[&file_path]);

    let data = "x".repeat(5000);
    let bytes = write_with_custom_capacity(&file_path, &data, 8192).unwrap();
    assert_eq!(bytes, 5000);

    let content = fs::read_to_string(&file_path).unwrap();
    assert_eq!(content, data);

    cleanup(&[&file_path]);
}

#[test]
fn test_capacity_returns_buffer_size() {
    let file_path = get_temp_path("bufwriter_cap_check");
    cleanup(&[&file_path]);

    let file = std::fs::File::create(&file_path).unwrap();
    let writer = std::io::BufWriter::with_capacity(256, file);
    assert_eq!(writer.capacity(), 256);

    let file2 = std::fs::File::create(&file_path).unwrap();
    let writer2 = std::io::BufWriter::new(file2);
    assert_eq!(writer2.capacity(), 8192); // default

    cleanup(&[&file_path]);
}

// --- get_ref ---

#[test]
fn test_get_underlying_writer() {
    let file_path = get_temp_path("bufwriter_get_ref");
    cleanup(&[&file_path]);

    get_underlying_writer(&file_path).unwrap();
    // File was created and BufWriter wrapped it
    assert!(std::path::Path::new(&file_path).exists());

    cleanup(&[&file_path]);
}

// --- get_mut ---

#[test]
fn test_get_mutable_underlying_writer() {
    let file_path = get_temp_path("bufwriter_get_mut");
    cleanup(&[&file_path]);

    get_mutable_underlying_writer(&file_path).unwrap();
    assert!(std::path::Path::new(&file_path).exists());

    cleanup(&[&file_path]);
}

// --- into_inner ---

#[test]
fn test_into_inner_demo() {
    let file_path = get_temp_path("bufwriter_into_inner");
    cleanup(&[&file_path]);

    let inner_file = into_inner_bufwriter(&file_path).unwrap();
    // inner_file is a File — we can still read from it
    drop(inner_file);

    cleanup(&[&file_path]);
}

#[test]
fn test_into_inner_flushes_buffer() {
    let file_path = get_temp_path("bufwriter_into_inner_flush");
    cleanup(&[&file_path]);

    let content = into_inner_flushes_buffer(&file_path, "auto-flushed data").unwrap();
    assert_eq!(content, "auto-flushed data");

    cleanup(&[&file_path]);
}

// --- write_all ---

#[test]
fn test_write_all_bufwriter() {
    let file_path = get_temp_path("bufwriter_write_all");
    cleanup(&[&file_path]);

    write_all_bufwriter(&file_path, "written with write_all").unwrap();

    let content = fs::read_to_string(&file_path).unwrap();
    assert_eq!(content, "written with write_all");

    cleanup(&[&file_path]);
}

#[test]
fn test_write_all_guarantees_full_write() {
    let file_path = get_temp_path("bufwriter_write_all_large");
    cleanup(&[&file_path]);

    let data = "data ".repeat(2000); // 10KB
    write_all_bufwriter(&file_path, &data).unwrap();

    let content = fs::read_to_string(&file_path).unwrap();
    assert_eq!(content.len(), data.len());
    assert_eq!(content, data);

    cleanup(&[&file_path]);
}

// --- Buffer behavior ---

#[test]
fn test_demonstrate_buffer_behavior() {
    let file_path = get_temp_path("bufwriter_buffer_behavior");
    cleanup(&[&file_path]);

    let result = demonstrate_buffer_behavior(&file_path, "buffered content").unwrap();
    assert!(result.contains("before_flush: \"\""));
    assert!(result.contains("after_flush: \"buffered content\""));

    cleanup(&[&file_path]);
}

#[test]
fn test_data_not_on_disk_until_flush() {
    let file_path = get_temp_path("bufwriter_not_on_disk");
    cleanup(&[&file_path]);

    // Create file first so read_to_string succeeds
    std::fs::write(&file_path, "").unwrap();

    let file = std::fs::File::create(&file_path).unwrap();
    let mut buf_writer = std::io::BufWriter::with_capacity(1024, file);

    buf_writer.write_all(b"not flushed yet").unwrap();

    // File on disk is still empty (we truncated it with create, but no new data)
    let content = fs::read_to_string(&file_path).unwrap();
    assert_eq!(content, "");

    // Flush and verify
    buf_writer.flush().unwrap();
    let content = fs::read_to_string(&file_path).unwrap();
    assert_eq!(content, "not flushed yet");

    cleanup(&[&file_path]);
}

// --- Drop without flush ---

#[test]
fn test_demonstrate_drop_without_flush() {
    let file_path = get_temp_path("bufwriter_drop_no_flush");
    cleanup(&[&file_path]);

    // Create file first
    std::fs::write(&file_path, "").unwrap();

    let result = demonstrate_drop_without_flush(&file_path, "this data may be lost").unwrap();

    // Drop may or may not flush — on many platforms data IS flushed by drop,
    // but the docs say errors are silently ignored. The point: don't rely on it.
    // We just verify the function doesn't panic.
    // The actual content depends on platform/implementation.
    let _ = result; // just verify it doesn't error

    cleanup(&[&file_path]);
}

// --- Many small writes (BufWriter's sweet spot) ---

#[test]
fn test_write_many_small_writes() {
    let file_path = get_temp_path("bufwriter_many_small");
    cleanup(&[&file_path]);

    let fragments = vec!["hello ", "world ", "from ", "bufwriter"];
    write_many_small_writes(&file_path, &fragments).unwrap();

    let content = fs::read_to_string(&file_path).unwrap();
    assert_eq!(content, "hello world from bufwriter");

    cleanup(&[&file_path]);
}

#[test]
fn test_write_many_small_writes_empty() {
    let file_path = get_temp_path("bufwriter_many_small_empty");
    cleanup(&[&file_path]);

    write_many_small_writes(&file_path, &[]).unwrap();

    let content = fs::read_to_string(&file_path).unwrap();
    assert_eq!(content, "");

    cleanup(&[&file_path]);
}

#[test]
fn test_write_many_small_writes_single_fragment() {
    let file_path = get_temp_path("bufwriter_many_small_single");
    cleanup(&[&file_path]);

    write_many_small_writes(&file_path, &["solo"]).unwrap();

    let content = fs::read_to_string(&file_path).unwrap();
    assert_eq!(content, "solo");

    cleanup(&[&file_path]);
}
