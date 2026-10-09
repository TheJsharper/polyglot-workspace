package io_test

import (
	"fmt"
	"testing"

	pio "github.com/TheJsharper/polyglot-workspace/01-basics/src/io/print_scan_taxes"
)

func TestPrintScanTaxes(t *testing.T) {
	s := setupTaxes(t, "Ana", 50000.0)

	pio.PrintScanTaxes()

	if len(s.prompts) != 1 || s.prompts[0] != "Enter your name and income: " {
		t.Errorf("unexpected prompt: %v", s.prompts)
	}
	if s.scanCalls != 1 {
		t.Errorf("Scan called %d times, want 1", s.scanCalls)
	}
	if want := "Hello, Ana! Your tax is 7500.00.\n"; s.output != want {
		t.Errorf("got %q, want %q", s.output, want)
	}
}

func setupTaxes(t *testing.T, name string, income float64) *taxSpies {
	t.Helper()
	origPrint, origScan, origPrintf := pio.Print, pio.Scan, pio.Printf
	t.Cleanup(func() { pio.Print, pio.Scan, pio.Printf = origPrint, origScan, origPrintf })

	s := &taxSpies{}
	pio.Print = func(a ...any) (int, error) {
		s.prompts = append(s.prompts, fmt.Sprint(a...))
		return 0, nil
	}
	pio.Scan = func(a ...any) (int, error) {
		s.scanCalls++
		*(a[0].(*string)) = name
		*(a[1].(*float64)) = income
		return 2, nil
	}
	pio.Printf = func(format string, a ...any) (int, error) {
		s.output = fmt.Sprintf(format, a...)
		return 0, nil
	}
	return s
}

type taxSpies struct {
	prompts   []string
	scanCalls int
	output    string
}
