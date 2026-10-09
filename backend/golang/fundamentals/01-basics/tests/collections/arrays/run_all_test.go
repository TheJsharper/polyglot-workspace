package arrays_test

import "testing"

// TestRunAll groups the package's top-level tests as subtests.
func TestRunAll(t *testing.T) {
	t.Run("TestArrayAccess", TestArrayAccess)
	t.Run("TestAccessLast", TestAccessLast)
	t.Run("TestAccessByVariable", TestAccessByVariable)
	t.Run("TestAccessForLoop", TestAccessForLoop)
	t.Run("TestAccessRange", TestAccessRange)
	t.Run("TestAccessRangeIndexOnly", TestAccessRangeIndexOnly)
	t.Run("TestAccessViaPointer", TestAccessViaPointer)
	t.Run("TestAccessAndModify", TestAccessAndModify)
	t.Run("TestAccessDestructure", TestAccessDestructure)
	t.Run("TestAccessMatrix", TestAccessMatrix)
	t.Run("TestEmptyArray", TestEmptyArray)
	t.Run("TestDeclaredArray", TestDeclaredArray)
	t.Run("TestInferredArray", TestInferredArray)
	t.Run("TestTwoDArray", TestTwoDArray)
	t.Run("TestTwoDInferredArray", TestTwoDInferredArray)
	t.Run("TestEmptyArrayModification", TestEmptyArrayModification)
	t.Run("TestEmptyArrayAccess", TestEmptyArrayAccess)
	t.Run("TestEmptyArrayLength", TestEmptyArrayLength)
	t.Run("TestEmptyArrayCapacity", TestEmptyArrayCapacity)
}
