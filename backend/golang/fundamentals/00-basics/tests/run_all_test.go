package tests

import "testing"

// TestRunAll groups the package's top-level tests as subtests.
func TestRunAll(t *testing.T) {
	t.Run("TestBooleanValueAndType", TestBooleanValueAndType)
	t.Run("TestComplexValueAndType", TestComplexValueAndType)
	t.Run("TestFloatValueAndType", TestFloatValueAndType)
	t.Run("TestInitializedVariables", TestInitializedVariables)
	t.Run("TestNumberValueAndType", TestNumberValueAndType)
	t.Run("TestTextValueAndType", TestTextValueAndType)
	t.Run("TestUninitializedVariables", TestUninitializedVariables)
}
