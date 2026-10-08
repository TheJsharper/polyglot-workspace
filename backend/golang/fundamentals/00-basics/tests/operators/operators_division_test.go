package operators_test

import (
	"reflect"
	"testing"

	operators "example.com/fundamentals/00-basics/src/operators"
)

func TestRunAllOperatorsDivision(t *testing.T) {
	t.Run("TestOperatorsDivision", TestOperatorsDivision)
	t.Run("TestOperatorsDivisionInt8", TestOperatorsDivisionInt8)
	t.Run("TestOperatorsDivisionInt16", TestOperatorsDivisionInt16)
	t.Run("TestOperatorsDivisionInt32", TestOperatorsDivisionInt32)
	t.Run("TestOperatorsDivisionInt64", TestOperatorsDivisionInt64)
	t.Run("TestOperatorsDivisionFloat", TestOperatorsDivisionFloat)
	t.Run("TestOperatorsDivisionFloat32", TestOperatorsDivisionFloat32)
	t.Run("TestOperatorsDivisionFloat64", TestOperatorsDivisionFloat64)
	t.Run("TestOperatorsDivisionFloat64Alt", TestOperatorsDivisionFloat64Alt)
	t.Run("TestOperatorsDivisionFloat32Alt", TestOperatorsDivisionFloat32Alt)
}

func TestOperatorsDivision(t *testing.T) {

	if got, want := operators.ResultDivision, 20; !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultDivision, int(20)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}
func TestOperatorsDivisionInt8(t *testing.T) {

	if got, want := operators.ResultDivisionInt8, int8(20); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultDivisionInt8, int8(20)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsDivisionInt16(t *testing.T) {

	if got, want := operators.ResultDivisionInt16, int16(20); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultDivisionInt16, int16(20)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsDivisionInt32(t *testing.T) {

	if got, want := operators.ResultDivisionInt32, int32(20); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultDivisionInt32, int32(20)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsDivisionInt64(t *testing.T) {

	if got, want := operators.ResultDivisionInt64, int64(20); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultDivisionInt64, int64(20)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsDivisionFloat(t *testing.T) {

	if got, want := operators.ResultDivisionFloat, float64(20); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultDivisionFloat, float64(20)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsDivisionFloat32(t *testing.T) {

	if got, want := operators.ResultDivisionFloat32, float32(20); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultDivisionFloat32, float32(20)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsDivisionFloat64(t *testing.T) {

	if got, want := operators.ResultDivisionFloat64, float64(20); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultDivisionFloat64, float64(20)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsDivisionFloat64Alt(t *testing.T) {

	if got, want := operators.ResultDivisionFloat64Alt, float64(20); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultDivisionFloat64Alt, float64(20)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsDivisionFloat32Alt(t *testing.T) {

	if got, want := operators.ResultDivisionFloat32Alt, float32(20); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultDivisionFloat32Alt, float32(20)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}
