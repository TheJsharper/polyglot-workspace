pub fn read_file(file_path: &str) -> std::io::Result<String> {

    std::fs::read_to_string(file_path)
}

pub fn read_file_lines(file_path: &str) -> std::io::Result<Vec<String>> {

    let content = std::fs::read_to_string(file_path)?;

    Ok(content.lines().map(|single_line| single_line.to_string()).collect())
}

pub fn count_words_in_file(file_path: &str) -> std::io::Result<usize> {

    let content = std::fs::read_to_string(file_path)?;

    Ok(content.split_whitespace().count())
}
