// Run all tests by including nested test modules
// This file aggregates all test suites without duplication

#[path = "arrays/arrays_test.rs"]
mod arrays_test;

#[path = "vectors/vectors_test.rs"]
mod vectors_test;

#[path = "tuples/tuples_test.rs"]
mod tuples_test;

#[path = "hashmaps/hashmaps_test.rs"]
mod hashmaps_test;