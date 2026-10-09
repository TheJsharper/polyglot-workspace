package collections_test

import (
	"testing"

	"github.com/TheJsharper/polyglot-workspace/01-basics/tests/testutil"
)

// TestRunAll runs TestRunAll of every sub-package.
func TestRunAll(t *testing.T) {
	testutil.RunChildPackages(t)
}
