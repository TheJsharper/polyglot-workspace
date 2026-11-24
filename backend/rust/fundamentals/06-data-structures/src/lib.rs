/// Data Structures Module
///
/// This crate provides foundational data structure implementations for learning Rust:
/// - **Arrays**: Fixed-size collections with bounds-checked access and mutation
/// - **Vectors**: Dynamic, growable collections with push/pop/insert/remove operations
/// - **Tuples**: Heterogeneous collections for grouping related values of different types
///
/// Each module includes comprehensive tests and review documentation explaining
/// the use cases, trade-offs, and when to choose one structure over another.

pub mod arrays;
pub mod vectors;
pub mod tuples;

// Re-export all module contents for convenience
// This allows users to import directly from the crate root:
//   use data_structures::*;
// instead of:
//   use data_structures::arrays::arrays::*;
pub use arrays::arrays::*;
pub use vectors::vectors::*;
pub use tuples::tuples::*;