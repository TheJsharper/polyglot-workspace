#!/bin/bash
# Run all tests for the 06-data-structures project
# Usage: ./scripts/run_tests.sh

echo "========================================="
echo "  Running all data structure tests..."
echo "========================================="
echo ""

cd "$(dirname "$0")/.."

cargo test --test run_all -- --nocapture

echo ""
echo "========================================="
echo "  Test run complete!"
echo "========================================="
