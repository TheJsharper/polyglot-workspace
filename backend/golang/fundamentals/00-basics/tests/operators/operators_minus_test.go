package operators

import (
	"reflect"
	"testing"

	operator "example.com/fundamentals/00-basics/src/operators"
)

func TestRunAllOperatorMinus(t *testing.T) {
	t.Run("TestOperatorsMinus", TestOperatorsMinus)
	t.Run("TestOperatorsMinusInt64", TestOperatorsMinusInt64)
	t.Run("TestOperatorsMinusInt64Alt", TestOperatorsMinusInt64Alt)
	t.Run("TestOperatorsMinusFloat64", TestOperatorsMinusFloat64)
	t.Run("TestResultFloatMinus", TestResultFloatMinus)
	t.Run("TestResultInt8Minus", TestResultInt8Minus)
	t.Run("TestResultInt16Minus", TestResultInt16Minus)
	t.Run("TestResultInt32Minus", TestResultInt32Minus)
	t.Run("TestResultInt64Minus", TestResultInt64Minus)
	t.Run("TestResultUint8Minus", TestResultUint8Minus)
	t.Run("TestResultUint16Minus", TestResultUint16Minus)
	t.Run("TestResultUintptrMinus", TestResultUintptrMinus)
}

func TestOperatorsMinus(t *testing.T) {
	if got, want := operator.ResultMinus, operator.ValueAMinus-operator.ValueBMinus; got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultMinus), reflect.TypeOf(operator.ValueAMinus-operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}
func TestOperatorsMinusInt64(t *testing.T) {
	if got, want := operator.ResultInt64Minus, int64(operator.ValueAMinus)-int64(operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultInt64Minus), reflect.TypeOf(int64(operator.ValueAMinus)-int64(operator.ValueBMinus)); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestOperatorsMinusInt64Alt(t *testing.T) {
	if got, want := operator.ResultInt64AltMinus, int64(operator.ValueAMinus)-int64(operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultInt64AltMinus), reflect.TypeOf(int64(operator.ValueAMinus)-int64(operator.ValueBMinus)); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}
func TestOperatorsMinusFloat64(t *testing.T) {
	if got, want := operator.ResultFloatMinus, float64(operator.ValueAMinus)-float64(operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultFloatMinus), reflect.TypeOf(float64(operator.ValueAMinus)-float64(operator.ValueBMinus)); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultFloatMinus(t *testing.T) {
	if got, want := operator.ResultFloatMinus, float64(operator.ValueAMinus)-float64(operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultFloatMinus), reflect.TypeOf(float64(operator.ValueAMinus)-float64(operator.ValueBMinus)); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultInt8Minus(t *testing.T) {
	if got, want := operator.ResultInt8Minus, int8(operator.ValueAMinus)-int8(operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultInt8Minus), reflect.TypeOf(int8(operator.ValueAMinus)-int8(operator.ValueBMinus)); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultInt16Minus(t *testing.T) {
	if got, want := operator.ResultInt16Minus, int16(operator.ValueAMinus)-int16(operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultInt16Minus), reflect.TypeOf(int16(operator.ValueAMinus)-int16(operator.ValueBMinus)); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultInt32Minus(t *testing.T) {
	if got, want := operator.ResultInt32Minus, int32(operator.ValueAMinus)-int32(operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultInt32Minus), reflect.TypeOf(int32(operator.ValueAMinus)-int32(operator.ValueBMinus)); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultInt64Minus(t *testing.T) {
	if got, want := operator.ResultInt64Minus, int64(operator.ValueAMinus)-int64(operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultInt64Minus), reflect.TypeOf(int64(operator.ValueAMinus)-int64(operator.ValueBMinus)); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultUint8Minus(t *testing.T) {
	if got, want := operator.ResultUint8Minus, uint8(operator.ValueAMinus)-uint8(operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultUint8Minus), reflect.TypeOf(uint8(operator.ValueAMinus)-uint8(operator.ValueBMinus)); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultUint16Minus(t *testing.T) {
	if got, want := operator.ResultUint16Minus, uint16(operator.ValueAMinus)-uint16(operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultUint16Minus), reflect.TypeOf(uint16(operator.ValueAMinus)-uint16(operator.ValueBMinus)); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultUintptrMinus(t *testing.T) {
	if got, want := operator.ResultUintptrMinus, uintptr(operator.ValueAMinus)-uintptr(operator.ValueBMinus); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operator.ResultUintptrMinus), reflect.TypeOf(uintptr(operator.ValueAMinus)-uintptr(operator.ValueBMinus)); got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}
