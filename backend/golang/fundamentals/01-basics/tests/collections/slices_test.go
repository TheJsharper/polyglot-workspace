package collections_test

import (
	"fmt"
	"reflect"
	"testing"

	c "github.com/TheJsharper/polyglot-workspace/01-basics/src/collections"
)

func TestRunAllSlices(t *testing.T) {
	t.Run("TestNilSlice", TestNilSlice)
	t.Run("TestLiteralSlice", TestLiteralSlice)
	t.Run("TestMadeSlice", TestMadeSlice)
	t.Run("TestSliceOfArray", TestSliceOfArray)
	t.Run("TestAppend", TestAppend)
}

func TestNilSlice(t *testing.T) {
	s := c.NilSlice
	fmt.Println("nil:", s)

	if s != nil || len(s) != 0 || cap(s) != 0 {
		t.Errorf("got %v len=%d cap=%d, want nil 0 0", s, len(s), cap(s))
	}
}

func TestLiteralSlice(t *testing.T) {
	s := c.LiteralSlice
	fmt.Println("lit:", s)

	// Unlike [5]int, the type has no length.
	if got := reflect.TypeOf(s).String(); got != "[]int" {
		t.Errorf("type = %s, want []int", got)
	}
	if len(s) != 5 || cap(s) != 5 {
		t.Errorf("len = %d, cap = %d, want 5 and 5", len(s), cap(s))
	}
}

func TestMadeSlice(t *testing.T) {
	s := c.MadeSlice
	fmt.Println("make:", s)

	if len(s) != 3 || cap(s) != 5 {
		t.Errorf("len = %d, cap = %d, want 3 and 5", len(s), cap(s))
	}
	if !reflect.DeepEqual(s, []int{0, 0, 0}) {
		t.Errorf("s = %v, want [0 0 0]", s)
	}
}

// Slicing an array shares its memory: writes through the slice change the array.
func TestSliceOfArray(t *testing.T) {
	a := [5]int{1, 2, 3, 4, 5}
	s := a[1:4]
	fmt.Println("sub:", s)

	if !reflect.DeepEqual(s, []int{2, 3, 4}) || len(s) != 3 || cap(s) != 4 {
		t.Errorf("s = %v len=%d cap=%d, want [2 3 4] 3 4", s, len(s), cap(s))
	}
	s[0] = 99
	if a[1] != 99 {
		t.Errorf("a[1] = %d, want 99 (shared memory)", a[1])
	}
}

func TestAppend(t *testing.T) {
	var s []int
	s = append(s, 1, 2, 3)
	fmt.Println("app:", s)

	if !reflect.DeepEqual(s, []int{1, 2, 3}) {
		t.Errorf("s = %v, want [1 2 3]", s)
	}
	if c.NilSlice != nil {
		t.Error("append must not touch the nil package slice")
	}
}
