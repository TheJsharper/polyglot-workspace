package types_test

import "testing"

// TestRunAll groups the package's top-level tests as subtests.
func TestRunAll(t *testing.T) {
	t.Run("TestCompareStringValue", TestCompareStringValue)
	t.Run("TestConvertionsPackage", TestConvertionsPackage)
	t.Run("TestEqualFoldStringValue", TestEqualFoldStringValue)
	t.Run("TestIndexByteStringValue", TestIndexByteStringValue)
	t.Run("TestIndexRuneStringValue", TestIndexRuneStringValue)
	t.Run("TestIntegerMaxValues", TestIntegerMaxValues)
	t.Run("TestIntegerMinValues", TestIntegerMinValues)
	t.Run("TestLastIndexByteStringValue", TestLastIndexByteStringValue)
	t.Run("TestLastIndexRuneStringValue", TestLastIndexRuneStringValue)
	t.Run("TestMapStringToLowerValue", TestMapStringToLowerValue)
	t.Run("TestMapStringToUpperValue", TestMapStringToUpperValue)
	t.Run("TestRepeatedStringValue", TestRepeatedStringValue)
	t.Run("TestReplaceAllStringValue", TestReplaceAllStringValue)
	t.Run("TestReplacedStringValue", TestReplacedStringValue)
	t.Run("TestRunComplexTests", TestRunComplexTests)
	t.Run("TestRunFloatTests", TestRunFloatTests)
	t.Run("TestRunStringTests", TestRunStringTests)
	t.Run("TestSplitAfterNStringValue", TestSplitAfterNStringValue)
	t.Run("TestSplitAfterStringValue", TestSplitAfterStringValue)
	t.Run("TestSplitNStringValue", TestSplitNStringValue)
	t.Run("TestTrimFuncStringValue", TestTrimFuncStringValue)
	t.Run("TestTrimLeftFuncStringValue", TestTrimLeftFuncStringValue)
	t.Run("TestTrimLeftStringValue", TestTrimLeftStringValue)
	t.Run("TestTrimPrefixStringValue", TestTrimPrefixStringValue)
	t.Run("TestTrimRightFuncStringValue", TestTrimRightFuncStringValue)
	t.Run("TestTrimRightStringValue", TestTrimRightStringValue)
	t.Run("TestTrimSuffixStringValue", TestTrimSuffixStringValue)
}
