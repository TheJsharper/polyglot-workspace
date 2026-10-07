package types_test

import (
	"reflect"
	"testing"

	complex "example.com/fundamentals/00-basics/src/types"
)

func TestRunComplexTests(t *testing.T) {
	t.Run("TestComplex64Value", TestComplex64Value)
	t.Run("TestComplex128Value", TestComplex128Value)
	t.Run("TestComplex64MaxValue", TestComplex64MaxValue)
	t.Run("TestComplex64MinValue", TestComplex64MinValue)
	t.Run("TestComplex128MaxValue", TestComplex128MaxValue)
}

func TestComplex64Value(t *testing.T) {
	if complex.Complex64Value != 1+2i {
		t.Errorf("Complex64Value = %v, want 1+2i", complex.Complex64Value)
	}
	if got := reflect.TypeOf(complex.Complex64Value).Kind(); got != reflect.Complex64 {
		t.Errorf("Complex64Value type = %s, want complex64", got)
	}
	if got, want := complex.Complex64Value, complex64(1+2i); got != want {
		t.Errorf("Complex64Value = %v, want %v", got, want)
	}
}

func TestComplex128Value(t *testing.T) {
	if complex.Complex128Value != 1+2i {
		t.Errorf("Complex128Value = %v, want 1+2i", complex.Complex128Value)
	}
	if got := reflect.TypeOf(complex.Complex128Value).Kind(); got != reflect.Complex128 {
		t.Errorf("Complex128Value type = %s, want complex128", got)
	}
	if got, want := complex.Complex128Value, complex128(1+2i); got != want {
		t.Errorf("Complex128Value = %v, want %v", got, want)
	}
}

func TestComplex64MaxValue(t *testing.T) {
	if complex.Complex64MaxValue != 3.4028235e+38+3.4028235e+38i {
		t.Errorf("Complex64MaxValue = %v, want 3.4028235e+38+3.4028235e+38i", complex.Complex64MaxValue)
	}
	if got := reflect.TypeOf(complex.Complex64MaxValue).Kind(); got != reflect.Complex64 {
		t.Errorf("Complex64MaxValue type = %s, want complex64", got)
	}
	if got, want := complex.Complex64MaxValue, complex64(3.4028235e+38+3.4028235e+38i); got != want {
		t.Errorf("Complex64MaxValue = %v, want %v", got, want)
	}
}

func TestComplex64MinValue(t *testing.T) {
	if complex.Complex64MinValue != 1.401298e-45+1.401298e-45i {
		t.Errorf("Complex64MinValue = %v, want 1.401298e-45+1.401298e-45i", complex.Complex64MinValue)
	}
	if got := reflect.TypeOf(complex.Complex64MinValue).Kind(); got != reflect.Complex64 {
		t.Errorf("Complex64MinValue type = %s, want complex64", got)
	}
	if got, want := complex.Complex64MinValue, complex64(1.401298e-45+1.401298e-45i); got != want {
		t.Errorf("Complex64MinValue = %v, want %v", got, want)
	}
}

func TestComplex128MaxValue(t *testing.T) {
	if complex.Complex128MaxValue != 1.7976931348623157e+308+1.7976931348623157e+308i {
		t.Errorf("Complex128MaxValue = %v, want 1.7976931348623157e+308+1.7976931348623157e+308i", complex.Complex128MaxValue)
	}
	if got := reflect.TypeOf(complex.Complex128MaxValue).Kind(); got != reflect.Complex128 {
		t.Errorf("Complex128MaxValue type = %s, want complex128", got)
	}
	if got, want := complex.Complex128MaxValue, complex128(1.7976931348623157e+308+1.7976931348623157e+308i); got != want {
		t.Errorf("Complex128MaxValue = %v, want %v", got, want)
	}
}
