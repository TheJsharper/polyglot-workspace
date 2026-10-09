package slice_sort_test

import "testing"

// TestRunAll groups the package's top-level tests as subtests.
func TestRunAll(t *testing.T) {
	t.Run("TestSliceSortMinMax", TestSliceSortMinMax)
}
