// Run all tests by including nested test modules
// This file aggregates all test suites without duplication

#[path = "reads/reads_file_test.rs"]
mod reads_file_test;

#[path = "writes/writes_file_test.rs"]
mod writes_file_test;

#[path = "bufreaders/bufreader_test.rs"]
mod bufreader_test;
