package slices_basics_test

import "testing"

// TestRunAll groups the package's top-level tests as subtests.
func TestRunAll(t *testing.T) {
	t.Run("TestNilSlice", TestNilSlice)
	t.Run("TestLiteralSlice", TestLiteralSlice)
	t.Run("TestMadeSlice", TestMadeSlice)
	t.Run("TestSliceOfArray", TestSliceOfArray)
	t.Run("TestAppend", TestAppend)
}
