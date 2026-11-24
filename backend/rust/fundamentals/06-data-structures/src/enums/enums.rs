#[derive(Debug, PartialEq)]
pub enum Color {
    Red,
    Green,
    Blue,
}

#[derive(Debug, PartialEq)]
pub enum Direction {
    Up,
    Down,
    Left,
    Right,
}

pub fn create_color_enum(color: &str) -> Color {
    match color {
        "Red" => Color::Red,
        "Green" => Color::Green,
        "Blue" => Color::Blue,
        _ => Color::Red,
    }
}

pub fn move_direction(direction: Direction) -> &'static str {
    match direction {
        Direction::Up => "Moving up",
        Direction::Down => "Moving down",
        Direction::Left => "Moving left",
        Direction::Right => "Moving right",
    }
}

pub fn reverse_direction(direction: Direction) -> Direction {
    match direction {
        Direction::Up => Direction::Down,
        Direction::Down => Direction::Up,
        Direction::Left => Direction::Right,
        Direction::Right => Direction::Left,
    }
}