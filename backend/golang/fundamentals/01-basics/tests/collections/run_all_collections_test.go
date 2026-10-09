package collections_test

import "testing"

// TestRunAllCollections runs every group in this package.
func TestRunAllCollections(t *testing.T) {
	t.Run("Arrays", TestRunAllArrays)
	t.Run("Slices", TestRunAllSlices)
}
