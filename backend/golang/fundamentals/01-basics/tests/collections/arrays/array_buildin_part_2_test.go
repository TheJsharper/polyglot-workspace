package arrays_test

import (
	"testing"

	"github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/arrays"
)

func TestArrayBuildinPart2(t *testing.T) {
	t.Run("MyMadeArray", TestMyMadeArray)
	t.Run("MyMadeArrayLen", TestMyMadeArrayLen)
	t.Run("MyMadeArrayCap", TestMyMadeArrayCap)
	t.Run("IsMyMadeArrayCapEqualLen", TestIsMyMadeArrayCapEqualLen)
	t.Run("MyMadeArrayAppend", TestMyMadeArrayAppend)
	t.Run("MyMadeArrayAppendCap", TestMyMadeArrayAppendCap)
	t.Run("IsMyMadeArrayAppendCapEqualLen", TestIsMyMadeArrayAppendCapEqualLen)
	t.Run("MyMadeArrayFull", TestMyMadeArrayFull)
	t.Run("MyMadeArrayFullCap", TestMyMadeArrayFullCap)
	t.Run("IsMyMadeArrayFullCapEqualLen", TestIsMyMadeArrayFullCapEqualLen)
	t.Run("MyMadeArrayFulledCleared", TestMyMadeArrayFulledCleared)
	t.Run("MyMadeArrayFulledClearedCap", TestMyMadeArrayFulledClearedCap)
	t.Run("IsMyMadeArrayFulledClearedCapEqualLen", TestIsMyMadeArrayFulledClearedCapEqualLen)
}
func TestMyMadeArray(t *testing.T) {
	if got, want := arrays.MyMadeArrayLen, 5; got != want {
		t.Errorf("MyMadeArrayLen = %d; want %d", got, want)
	}

}
func TestMyMadeArrayLen(t *testing.T) {
	if got, want := arrays.MyMadeArrayLen, 5; got != want {
		t.Errorf("MyMadeArrayLen = %d; want %d", got, want)
	}
}
func TestMyMadeArrayCap(t *testing.T) {
	if got, want := arrays.MyMadeArrayCap, 5; got != want {
		t.Errorf("MyMadeArrayCap = %d; want %d", got, want)
	}
}
func TestIsMyMadeArrayCapEqualLen(t *testing.T) {
	if got, want := arrays.IsMyMadeArrayCapEqualLen, true; got != want {
		t.Errorf("IsMyMadeArrayCapEqualLen = %v; want %v", got, want)
	}
}
func TestMyMadeArrayAppend(t *testing.T) {
	if got, want := arrays.MyMadeArrayAppendLen, 6; got != want {
		t.Errorf("MyMadeArrayAppendLen = %d; want %d", got, want)
	}
}
func TestMyMadeArrayAppendCap(t *testing.T) {
	if got, want := arrays.MyMadeArrayAppendCap, 10; got != want {
		t.Errorf("MyMadeArrayAppendCap = %d; want %d", got, want)
	}
}
func TestIsMyMadeArrayAppendCapEqualLen(t *testing.T) {
	if got, want := arrays.IsMyMadeArrayAppendCapEqualLen, false; got != want {
		t.Errorf("IsMyMadeArrayAppendCapEqualLen = %v; want %v", got, want)
	}
}
func TestMyMadeArrayFull(t *testing.T) {
	if got, want := arrays.MyMadeArrayFullLen, 10; got != want {
		t.Errorf("MyMadeArrayFullLen = %d; want %d", got, want)
	}
}
func TestMyMadeArrayFullCap(t *testing.T) {
	if got, want := arrays.MyMadeArrayFullCap, 10; got != want {
		t.Errorf("MyMadeArrayFullCap = %d; want %d", got, want)
	}
}
func TestIsMyMadeArrayFullCapEqualLen(t *testing.T) {
	if got, want := arrays.IsMyMadeArrayFullCapEqualLen, true; got != want {
		t.Errorf("IsMyMadeArrayFullCapEqualLen = %v; want %v", got, want)
	}
}
func TestMyMadeArrayFulledCleared(t *testing.T) {
	if got, want := arrays.MyMadeArrayFulledClearedLen, 10; got != want {
		t.Errorf("MyMadeArrayFulledClearedLen = %d; want %d", got, want)
	}
}
func TestMyMadeArrayFulledClearedCap(t *testing.T) {
	if got, want := arrays.MyMadeArrayFulledClearedCap, 10; got != want {
		t.Errorf("MyMadeArrayFulledClearedCap = %d; want %d", got, want)
	}
}
func TestIsMyMadeArrayFulledClearedCapEqualLen(t *testing.T) {
	if got, want := arrays.IsMyMadeArrayFulledClearedCapEqualLen, true; got != want {
		t.Errorf("IsMyMadeArrayFulledClearedCapEqualLen = %v; want %v", got, want)
	}
}
