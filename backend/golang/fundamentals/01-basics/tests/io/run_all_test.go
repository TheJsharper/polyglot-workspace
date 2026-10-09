package io_test

import "testing"

// TestRunAll groups the package's top-level tests as subtests.
func TestRunAll(t *testing.T) {
	t.Run("TestMocksRestored", TestMocksRestored)
	t.Run("TestPrintScan", TestPrintScan)
	t.Run("TestPrintScanTaxes", TestPrintScanTaxes)
}
