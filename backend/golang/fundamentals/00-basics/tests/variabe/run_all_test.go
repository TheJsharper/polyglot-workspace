package variable_test

import "testing"

// TestRunAll groups the package's top-level tests as subtests.
func TestRunAll(t *testing.T) {
	t.Run("TestInitializedVariables", TestInitializedVariables)
	t.Run("TestShortHandVariableInitializedExportedVariables", TestShortHandVariableInitializedExportedVariables)
	t.Run("TestUninitializedVariables", TestUninitializedVariables)
	t.Run("TestVariableInitializedExportedVariables", TestVariableInitializedExportedVariables)
}
