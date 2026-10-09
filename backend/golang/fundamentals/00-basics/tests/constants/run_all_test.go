package constants_test

import "testing"

// TestRunAll groups the package's top-level tests as subtests.
func TestRunAll(t *testing.T) {
	t.Run("TestConstants", TestConstants)
	t.Run("TestConstants_initialized_block", TestConstants_initialized_block)
}
