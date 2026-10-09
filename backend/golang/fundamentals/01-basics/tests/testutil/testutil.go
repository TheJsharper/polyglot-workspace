package testutil

import (
	"os"
	"os/exec"
	"path/filepath"
	"reflect"
	"testing"
)

// ExpectEqual fails the test when got and want are not deeply equal.
func ExpectEqual(t *testing.T, got, want any) {
	t.Helper()
	if !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}

// RunChildPackages runs TestRunAll of every sub-package of the current test
// directory, one subtest per folder. Test functions cannot be imported across
// packages, so each child is executed with `go test`.
func RunChildPackages(t *testing.T) {
	t.Helper()
	entries, err := os.ReadDir(".")
	if err != nil {
		t.Fatal(err)
	}
	for _, entry := range entries {
		if !entry.IsDir() || !hasTests(entry.Name()) {
			continue
		}
		folder := entry.Name()
		t.Run(folder, func(t *testing.T) {
			output, err := exec.Command("go", "test", "-count=1", "-run", "^TestRunAll$", "./"+folder).CombinedOutput()
			if err != nil {
				t.Fatalf("%s\n%s", err, output)
			}
		})
	}
}

// hasTests reports whether the folder contains a *_test.go file.
func hasTests(folder string) bool {
	files, _ := filepath.Glob(filepath.Join(folder, "*_test.go"))
	return len(files) > 0
}
