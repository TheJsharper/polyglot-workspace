package operators

import (
	"reflect"
	"testing"

	operators "example.com/fundamentals/00-basics/src/operators"
)

func TestOperators(t *testing.T) {
	t.Run("TestResult", TestResult)
	t.Run("TestResultInt64", TestResultInt64)
	t.Run("TestResultInt64Alt", TestResultInt64Alt)
	t.Run("TestResultFloat", TestResultFloat)
	t.Run("TestResultInt8", TestResultInt8)
	t.Run("TestResultUint8", TestResultUint8)
	t.Run("TestResultUint16", TestResultUint16)
	t.Run("TestResultUint32", TestResultUint32)
	t.Run("TestResultUint64", TestResultUint64)
	t.Run("TestResultInt16", TestResultInt16)
	t.Run("TestResultInt32", TestResultInt32)
	t.Run("TestResultUintptr", TestResultUintptr)
}

func TestResult(t *testing.T) {
	if got, want := operators.Result, operators.ValueA+operators.ValueB; !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := operators.ResultFloat, float64(operators.ValueA)+float64(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := operators.ResultInt64, int64(operators.ValueA)+int64(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultInt64(t *testing.T) {
	if got, want := operators.ResultInt64, int64(operators.ValueA)+int64(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := operators.ResultInt64, int64(operators.ValueA)+int64(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operators.ResultInt64), reflect.TypeOf(int64(operators.ValueA)+int64(operators.ValueB)); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultInt64Alt(t *testing.T) {
	if got, want := operators.ResultInt64Alt, int64(operators.ValueA)+int64(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operators.ResultInt64Alt), reflect.TypeOf(int64(operators.ValueA)+int64(operators.ValueB)); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultFloat(t *testing.T) {
	if got, want := operators.ResultFloat, float64(operators.ValueA)+float64(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operators.ResultFloat), reflect.TypeOf(float64(operators.ValueA)+float64(operators.ValueB)); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultInt8(t *testing.T) {
	if got, want := operators.ResultInt8, int8(operators.ValueA)+int8(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operators.ResultInt8), reflect.TypeOf(int8(operators.ValueA)+int8(operators.ValueB)); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultUint8(t *testing.T) {
	if got, want := operators.ResultUint8, uint8(operators.ValueA)+uint8(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operators.ResultUint8), reflect.TypeOf(uint8(operators.ValueA)+uint8(operators.ValueB)); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultUint16(t *testing.T) {
	if got, want := operators.ResultUint16, uint16(operators.ValueA)+uint16(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operators.ResultUint16), reflect.TypeOf(uint16(operators.ValueA)+uint16(operators.ValueB)); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultUint32(t *testing.T) {
	if got, want := operators.ResultUint32, uint32(operators.ValueA)+uint32(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operators.ResultUint32), reflect.TypeOf(uint32(operators.ValueA)+uint32(operators.ValueB)); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultUint64(t *testing.T) {
	if got, want := operators.ResultUint64, uint64(operators.ValueA)+uint64(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operators.ResultUint64), reflect.TypeOf(uint64(operators.ValueA)+uint64(operators.ValueB)); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}
func TestResultInt16(t *testing.T) {
	if got, want := operators.ResultInt16, int16(operators.ValueA)+int16(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operators.ResultInt16), reflect.TypeOf(int16(operators.ValueA)+int16(operators.ValueB)); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultInt32(t *testing.T) {
	if got, want := operators.ResultInt32, int32(operators.ValueA)+int32(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operators.ResultInt32), reflect.TypeOf(int32(operators.ValueA)+int32(operators.ValueB)); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}

func TestResultUintptr(t *testing.T) {
	if got, want := operators.ResultUintptr, uintptr(operators.ValueA)+uintptr(operators.ValueB); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(operators.ResultUintptr), reflect.TypeOf(uintptr(operators.ValueA)+uintptr(operators.ValueB)); !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
}
