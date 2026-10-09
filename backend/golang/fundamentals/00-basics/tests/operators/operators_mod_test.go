package operators_test

import (
	"testing"

	operator "example.com/fundamentals/00-basics/src/operators"
)

func TestOperatorsMod(t *testing.T) {

	if got, want := operator.ResultMod, 1; got != want {
		t.Errorf("Expected %v, got %v", want, got)
	}
	if got, want := operator.ResultModInt8, int8(1); got != want {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := operator.ResultModInt16, int16(1); got != want {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := operator.ResultModInt32, int32(1); got != want {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := operator.ResultModInt64, int64(1); got != want {
		t.Errorf("Expected %v, got %v", want, got)
	}

}
