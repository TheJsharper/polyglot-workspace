package operators_test

import "testing"

// TestRunAll groups the package's top-level tests as subtests.
func TestRunAll(t *testing.T) {
	t.Run("TestOperatorsMod", TestOperatorsMod)
	t.Run("TestRunAllComparisons", TestRunAllComparisons)
	t.Run("TestRunAllOperatorsDivision", TestRunAllOperatorsDivision)
	t.Run("TestRunAllOperatorsMulti", TestRunAllOperatorsMulti)
}
