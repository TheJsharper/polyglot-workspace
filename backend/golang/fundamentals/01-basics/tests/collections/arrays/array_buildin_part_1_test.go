package arrays_test

import (
	"testing"

	arrays "github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/arrays"
)

func TestArrayBuiltin(t *testing.T) {
	t.Run("MyArray", TestArrayBuiltinMyArray)
	t.Run("EmptyArrayBuiltin", TestArrayBuiltinEmptyArrayBuiltin)
	t.Run("IsNilArray", TestArrayBuiltinIsNilarray)
	t.Run("IsEmptyArrayExplicit", TestArrayBuiltinIsEmptyArrayExplicit)
	t.Run("EmptyArrayBuiltinLen", TestEmptyArrayBuiltinLen)
	t.Run("NilArrayLen", TestNilArrayLen)
	t.Run("ArrayAppend", TestArrayAppend)
	t.Run("EmptyArrayBuiltinAppend", TestEmptyArrayBuiltinAppend)
	t.Run("NilArrayAppend", TestNilArrayAppend)
	t.Run("MyArrayCap", TestMyArrayCap)
	t.Run("EmptyArrayBuiltinCap", TestEmptyArrayBuiltinCap)
	t.Run("NilArrayCap", TestNilArrayCap)
	t.Run("IsCapEqualLen", TestIsCapEqualLen)
	t.Run("IsEmptyArrayBuiltinCapEqualLen", TestIsEmptyArrayBuiltinCapEqualLen)
	t.Run("IsNilArrayCapEqualLen", TestIsNilArrayCapEqualLen)

}

func TestArrayBuiltinMyArray(t *testing.T) {
	if got, want := arrays.MyArray, []int{1, 2, 3, 4, 5}; !equal(got, want) {
		t.Errorf("MyArray = %v, want %v", got, want)
	}

}

func TestArrayBuiltinEmptyArrayBuiltin(t *testing.T) {
	if got, want := arrays.EmptyArrayBuiltin, []int{}; !equal(got, want) {
		t.Errorf("EmptyArrayBuiltin = %v, want %v", got, want)
	}
}

func TestArrayBuiltinIsNilarray(t *testing.T) {
	if got, want := arrays.IsNilArray, true; got != want {
		t.Errorf("IsNilArray = %v, want %v", got, want)
	}
}

func TestArrayBuiltinIsEmptyArrayExplicit(t *testing.T) {
	if got, want := arrays.IsNilArrayExplicit, true; got != want {
		t.Errorf("IsNilArrayExplicit = %v, want %v", got, want)
	}
}

func TestEmptyArrayBuiltinLen(t *testing.T) {
	if got, want := arrays.MyArrayLen, 5; got != want {
		t.Errorf("len(MyArray) = %v, want %v", got, want)
	}
}

func TestNilArrayLen(t *testing.T) {
	if got, want := arrays.NilArrayLen, 0; got != want {
		t.Errorf("len(NilArray) = %v, want %v", got, want)
	}
}

func TestArrayAppend(t *testing.T) {
	if got, want := arrays.MyArrayAppend, []int{1, 2, 3, 4, 5, 6}; !equal(got, want) {
		t.Errorf("ArrayAppend = %v, want %v", got, want)
	}
}

func TestEmptyArrayBuiltinAppend(t *testing.T) {
	if got, want := arrays.EmptyArrayBuiltinAppend, []int{6}; !equal(got, want) {
		t.Errorf("EmptyArrayBuiltinAppend = %v, want %v", got, want)
	}
}
func TestNilArrayAppend(t *testing.T) {
	if got, want := arrays.NilArrayAppend, []int{6}; !equal(got, want) {
		t.Errorf("NilArrayAppend = %v, want %v", got, want)
	}
}

func TestMyArrayCap(t *testing.T) {
	if got, want := arrays.MyArrayCap, 5; got != want {
		t.Errorf("cap(MyArray) = %v, want %v", got, want)
	}
}

func TestEmptyArrayBuiltinCap(t *testing.T) {
	if got, want := arrays.EmptyArrayBuiltinCap, 0; got != want {
		t.Errorf("cap(EmptyArrayBuiltin) = %v, want %v", got, want)
	}
}

func TestNilArrayCap(t *testing.T) {
	if got, want := arrays.NilArrayCap, 0; got != want {
		t.Errorf("cap(NilArray) = %v, want %v", got, want)
	}
}

func TestIsCapEqualLen(t *testing.T) {
	if got, want := arrays.IsCapEqualLen, true; got != want {
		t.Errorf("IsCapEqualLen = %v, want %v", got, want)
	}
}

func TestIsEmptyArrayBuiltinCapEqualLen(t *testing.T) {
	if got, want := arrays.IsEmptyArrayBuiltinCapEqualLen, true; got != want {
		t.Errorf("IsEmptyArrayBuiltinCapEqualLen = %v, want %v", got, want)
	}
}

func TestIsNilArrayCapEqualLen(t *testing.T) {
	if got, want := arrays.IsNilArrayCapEqualLen, true; got != want {
		t.Errorf("IsNilArrayCapEqualLen = %v, want %v", got, want)
	}
}

func equal(a, b []int) bool {
	if len(a) != len(b) {
		return false
	}
	for i := range a {
		if a[i] != b[i] {
			return false
		}
	}
	return true
}
