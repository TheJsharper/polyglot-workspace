package operators_test

import (
	"reflect"
	"testing"

	operators "example.com/fundamentals/00-basics/src/operators"
)

func TestRunAllOperatorsMulti(t *testing.T) {
	t.Run("TestOperatorsMulti", TestOperatorsMulti)
	t.Run("TestOperatorsMultiInt8", TestOperatorsMultiInt8)
	t.Run("TestOperatorsMultiInt16", TestOperatorsMultiInt16)
	t.Run("TestOperatorsMultiInt32", TestOperatorsMultiInt32)
	t.Run("TestOperatorsMultiInt64", TestOperatorsMultiInt64)
	t.Run("TestOperatorsMultiUint", TestOperatorsMultiUint)
	t.Run("TestOperatorsMultiUint8", TestOperatorsMultiUint8)
	t.Run("TestOperatorsMultiUint16", TestOperatorsMultiUint16)
	t.Run("TestOperatorsMultiUint32", TestOperatorsMultiUint32)
	t.Run("TestOperatorsMultiUint64", TestOperatorsMultiUint64)
	t.Run("TestOperatorsMultiFloat", TestOperatorsMultiFloat)
	t.Run("TestOperatorsMultiFloat32", TestOperatorsMultiFloat32)
	t.Run("TestOperatorsMultiFloat64", TestOperatorsMultiFloat64)
	t.Run("TestOperatorsMultiFloat64Alt", TestOperatorsMultiFloat64Alt)
}

func TestOperatorsMulti(t *testing.T) {

	if got, want := operators.ResultMulti, 50; !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMulti, int(50)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiInt8(t *testing.T) {

	if got, want := operators.ResultMultiInt8, int8(50); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiInt8, int8(50)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiInt16(t *testing.T) {

	if got, want := operators.ResultMultiInt16, int16(50); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiInt16, int16(50)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiInt32(t *testing.T) {

	if got, want := operators.ResultMultiInt32, int32(50); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiInt32, int32(50)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiInt64(t *testing.T) {

	if got, want := operators.ResultMultiInt64, int64(50); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiInt64, int64(50)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiUint(t *testing.T) {

	if got, want := operators.ResultMultiUint, uint(50); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiUint, uint(50)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiUint8(t *testing.T) {

	if got, want := operators.ResultMultiUint8, uint8(50); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiUint8, uint8(50)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiUint16(t *testing.T) {

	if got, want := operators.ResultMultiUint16, uint16(50); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiUint16, uint16(50)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiUint32(t *testing.T) {

	if got, want := operators.ResultMultiUint32, uint32(50); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiUint32, uint32(50)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiUint64(t *testing.T) {

	if got, want := operators.ResultMultiUint64, uint64(50); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiUint64, uint64(50)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiFloat(t *testing.T) {

	if got, want := operators.ResultMultiFloat, float64(50.0); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiFloat, float64(50.0)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiFloat32(t *testing.T) {

	if got, want := operators.ResultMultiFloat32, float32(50.0); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiFloat32, float32(50.0)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiFloat64(t *testing.T) {

	if got, want := operators.ResultMultiFloat64, float64(50.0); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiFloat64, float64(50.0)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}

func TestOperatorsMultiFloat64Alt(t *testing.T) {

	if got, want := operators.ResultMultiFloat64Alt, float64(50.0); !reflect.DeepEqual(got, want) {
		t.Errorf("Expected %v, got %v", want, got)
	}

	if got, want := reflect.DeepEqual(operators.ResultMultiFloat64Alt, float64(50.0)), true; !got {
		t.Errorf("Expected %v, got %v", want, got)
	}

}
