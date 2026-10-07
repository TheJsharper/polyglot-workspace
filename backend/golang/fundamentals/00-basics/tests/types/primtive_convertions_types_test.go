package types_test

import (
	"testing"

	convertions "example.com/fundamentals/00-basics/src/types"
)

func TestConvertionResult_1(t *testing.T) {

	if got, want := convertions.Result, float32(3.0); got != want {
		t.Errorf("Result = %v, want %v", got, want)
	}

}
