// Run all tests across 02-control-flow modules
// Usage: cargo test --test run_all

#[path = "matches_test.rs"]
mod matches_test;

#[path = "if_controls_test.rs"]
mod if_controls_test;

#[path = "loops_test.rs"]
mod loops_test;

#[path = "while_loops_test.rs"]
mod while_loops_test;

#[path = "matches/matches_test.rs"]
mod matches_matches_test;

#[path = "if-controls/if_controls_test.rs"]
mod if_controls_if_controls_test;

#[path = "loops/loops_test.rs"]
mod loops_loops_test;

#[path = "loops/while_loops_test.rs"]
mod loops_while_loops_test;

#[path = "loops/for_loops_test.rs"]
mod loops_for_loops_test;
