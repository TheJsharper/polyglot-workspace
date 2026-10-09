package collections_test

import (
	"fmt"
	"reflect"
	"testing"

	c "github.com/TheJsharper/polyglot-workspace/01-basics/src/collections"
)

func TestRunAllArrays(t *testing.T) {
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
func TestArrayAccess(t *testing.T) {

	if got := c.AccessByIndex(c.ExampleArray); got != 1 {
		t.Errorf("AccessByIndex = %d, want 1", got)
	}
}

func TestAccessLast(t *testing.T) {
	if got := c.AccessLast(c.ExampleArray); got != 3 {
		t.Errorf("AccessLast = %d, want 3", got)
	}
}

func TestAccessByVariable(t *testing.T) {
	if got := c.AccessByVariable(c.ExampleArray, 1); got != 2 {
		t.Errorf("AccessByVariable = %d, want 2", got)
	}
}

func TestAccessForLoop(t *testing.T) {
	if got := c.AccessForLoop(c.ExampleArray); got != 6 {
		t.Errorf("AccessForLoop = %d, want 6", got)
	}
}

func TestAccessRange(t *testing.T) {
	if got := c.AccessRange(c.ExampleArray); got != 6 {
		t.Errorf("AccessRange = %d, want 6", got)
	}
}

func TestAccessRangeIndexOnly(t *testing.T) {
	if got := c.AccessRangeIndexOnly(c.ExampleArray); got != 3 {
		t.Errorf("AccessRangeIndexOnly = %d, want 3", got)
	}
}

func TestAccessViaPointer(t *testing.T) {
	if got := c.AccessViaPointer(&c.ExampleArray); got != 2 {
		t.Errorf("AccessViaPointer = %d, want 2", got)
	}
}

func TestAccessAndModify(t *testing.T) {
	a := c.ExampleArray
	c.AccessAndModify(&a, 0, 99)
	if a[0] != 99 {
		t.Errorf("AccessAndModify: a[0] = %d, want 99", a[0])
	}
	if c.ExampleArray[0] != 1 {
		t.Error("original array must be unchanged (value semantics)")
	}
}

func TestAccessDestructure(t *testing.T) {
	x, y, z := c.AccessDestructure(c.ExampleArray)
	if x != 1 || y != 2 || z != 3 {
		t.Errorf("AccessDestructure = %d,%d,%d", x, y, z)
	}
}

func TestAccessMatrix(t *testing.T) {
	m := [2][3]int{{1, 2, 3}, {4, 5, 6}}
	if got := c.AccessMatrix(m, 1, 2); got != 6 {
		t.Errorf("AccessMatrix = %d, want 6", got)
	}
}

func TestEmptyArray(t *testing.T) {
	a := c.EmptyArray
	fmt.Println("emp:", a)

	if got := reflect.TypeOf(a).String(); got != "[5]int" {
		t.Errorf("type = %s, want [5]int", got)
	}
	if len(a) != 5 || cap(a) != 5 {
		t.Errorf("len = %d, cap = %d, want 5 and 5", len(a), cap(a))
	}
}
func TestEmptyArrayModification(t *testing.T) {
	a := c.EmptyArray
	a[0] = 99
	if a[0] != 99 {
		t.Errorf("modified EmptyArray copy: a[0] = %d, want 99", a[0])
	}
	if c.EmptyArray[0] != 0 {
		t.Error("original empty array must be unchanged (value semantics)")
	}
}
func TestEmptyArrayAccess(t *testing.T) {
	a := c.EmptyArray
	if a[0] != 0 || a[4] != 0 {
		t.Errorf("EmptyArray access = %d,%d, want 0,0", a[0], a[4])
	}
}

func TestEmptyArrayLength(t *testing.T) {
	a := c.EmptyArray
	if len(a) != 5 {
		t.Errorf("EmptyArray length = %d, want 5", len(a))
	}
}

func TestEmptyArrayCapacity(t *testing.T) {
	a := c.EmptyArray
	if cap(a) != 5 {
		t.Errorf("EmptyArray capacity = %d, want 5", cap(a))
	}
}

func TestDeclaredArray(t *testing.T) {
	b := c.DeclaredArray
	fmt.Println("dcl:", b)

	if got := reflect.TypeOf(b).String(); got != "[5]int" {
		t.Errorf("type = %s, want [5]int", got)
	}
	if len(b) != 5 || cap(b) != 5 {
		t.Errorf("len = %d, cap = %d, want 5 and 5", len(b), cap(b))
	}
	if b != [5]int{1, 2, 3, 4, 5} {
		t.Errorf("b = %v", b)
	}
}

func TestInferredArray(t *testing.T) {
	b := c.InferredArray
	fmt.Println("dcl:", b)

	// [...] is resolved at compile time: the type is still [5]int, not a slice.
	if got := reflect.TypeOf(b).String(); got != "[5]int" {
		t.Errorf("type = %s, want [5]int", got)
	}
	if len(b) != 5 || cap(b) != 5 {
		t.Errorf("len = %d, cap = %d, want 5 and 5", len(b), cap(b))
	}
	if b != c.DeclaredArray {
		t.Errorf("b = %v, want %v", b, c.DeclaredArray)
	}
}

func TestTwoDArray(t *testing.T) {
	d := c.TwoD
	fmt.Println("2d:", d)

	if got := reflect.TypeOf(d).String(); got != "[2][3]int" {
		t.Errorf("type = %s, want [2][3]int", got)
	}
	if len(d) != 2 || len(d[0]) != 3 {
		t.Errorf("len = %d x %d, want 2 x 3", len(d), len(d[0]))
	}
	if d != [2][3]int{{1, 2, 3}, {1, 2, 3}} {
		t.Errorf("d = %v", d)
	}
}

func TestTwoDInferredArray(t *testing.T) {
	d := c.TwoDInferred
	fmt.Println("2d:", d)

	if got := reflect.TypeOf(d).String(); got != "[2][3]int" {
		t.Errorf("type = %s, want [2][3]int", got)
	}
	if d != c.TwoD {
		t.Errorf("d = %v, want %v", d, c.TwoD)
	}
}
