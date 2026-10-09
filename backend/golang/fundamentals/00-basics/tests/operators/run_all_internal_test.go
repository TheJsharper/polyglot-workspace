package operators

import "testing"

// TestRunAll groups the package's top-level tests as subtests.
func TestRunAll(t *testing.T) {
	t.Run("TestOperators", TestOperators)
	t.Run("TestRunAllOperatorMinus", TestRunAllOperatorMinus)
}
