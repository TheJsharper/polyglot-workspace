#[path = "../../src/if-controls/if-controls.rs"]
mod if_controls;

use if_controls::test;

#[test]
fn test_smoke() {
    assert_eq!(test(), "Hello from test!!");
}
