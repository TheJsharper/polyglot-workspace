package slice_compare_test

import "testing"

// TestRunAll groups the package's top-level tests as subtests.
func TestRunAll(t *testing.T) {
	t.Run("TestSliceCompare", TestSliceCompare)
}
