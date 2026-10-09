package io_test

import (
	"fmt"
	"testing"

	pio "github.com/TheJsharper/polyglot-workspace/01-basics/src/io"
)

type spies struct {
	prompts   []string
	scanCalls int
	output    string
}

// setup is the "beforeEach": it installs the mocks and registers the
// "afterEach" restore through t.Cleanup (runs LIFO, even on failure/t.Fatal).
func setup(t *testing.T, name string, age int) *spies {
	t.Helper()
	origPrint, origScan, origPrintf := pio.Print, pio.Scan, pio.Printf
	t.Cleanup(func() { pio.Print, pio.Scan, pio.Printf = origPrint, origScan, origPrintf })

	s := &spies{}
	pio.Print = func(a ...any) (int, error) {
		s.prompts = append(s.prompts, fmt.Sprint(a...))
		return 0, nil
	}
	pio.Scan = func(a ...any) (int, error) {
		s.scanCalls++
		*(a[0].(*string)) = name
		*(a[1].(*int)) = age
		return 2, nil
	}
	pio.Printf = func(format string, a ...any) (int, error) {
		s.output = fmt.Sprintf(format, a...)
		return 0, nil
	}
	return s
}

func TestPrintScan(t *testing.T) {
	s := setup(t, "Ana", 30)

	pio.PrintScan()

	if len(s.prompts) != 1 || s.prompts[0] != "Enter your name and age: " {
		t.Errorf("unexpected prompt: %v", s.prompts)
	}
	if s.scanCalls != 1 {
		t.Errorf("Scan called %d times, want 1", s.scanCalls)
	}
	if want := "Hello, Ana! You are 30 years old.\n"; s.output != want {
		t.Errorf("got %q, want %q", s.output, want)
	}
}

func TestMocksRestored(t *testing.T) {
	t.Run("mocked", func(t *testing.T) { setup(t, "Bob", 1) })
	// After the subtest, Cleanup has run: Scan is the real fmt.Scan again.
	if fmt.Sprintf("%p", pio.Scan) != fmt.Sprintf("%p", fmt.Scan) {
		t.Error("Scan was not restored")
	}
}
