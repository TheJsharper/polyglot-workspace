use data_structures::*;

#[test]
fn test_create_color_enum() {
    assert_eq!(create_color_enum("Red"), Color::Red);
    assert_eq!(create_color_enum("Green"), Color::Green);
    assert_eq!(create_color_enum("Blue"), Color::Blue);
    assert_eq!(create_color_enum("Unknown"), Color::Red);
}

#[test]
fn test_move_direction() {
    assert_eq!(move_direction(Direction::Up), "Moving up");
    assert_eq!(move_direction(Direction::Down), "Moving down");
    assert_eq!(move_direction(Direction::Left), "Moving left");
    assert_eq!(move_direction(Direction::Right), "Moving right");
}

#[test]
fn test_reverse_direction() {
    assert_eq!(reverse_direction(Direction::Up), Direction::Down);
    assert_eq!(reverse_direction(Direction::Down), Direction::Up);
    assert_eq!(reverse_direction(Direction::Left), Direction::Right);
    assert_eq!(reverse_direction(Direction::Right), Direction::Left);
}