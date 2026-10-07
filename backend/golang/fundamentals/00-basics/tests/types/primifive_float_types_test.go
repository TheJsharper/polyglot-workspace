package types_test

import (
	"reflect"
	"testing"

	float "example.com/fundamentals/00-basics/src/types"
)

func TestRunFloatTests(t *testing.T) {
	t.Run("TestFloat32Value", TestFloat32Value)
	t.Run("TestFloat64Value", TestFloat64Value)
	t.Run("TestFloat32MaxValue", TestFloat32MaxValue)
	t.Run("TestFloat32MinValue", TestFloat32MinValue)
	t.Run("TestFloat64MaxValue", TestFloat64MaxValue)
	t.Run("TestFloat64MinValue", TestFloat64MinValue)
}

func TestFloat32Value(t *testing.T) {
	if float.Float32Value != 3.14 {
		t.Errorf("Float32Value = %f, want 3.14", float.Float32Value)
	}

	if got := reflect.TypeOf(float.Float32Value).Kind(); got != reflect.Float32 {
		t.Errorf("Float32Value type = %s, want float32", got)
	}
	if got, want := float.Float32Value, float32(3.14); got != want {
		t.Errorf("Float32Value = %f, want %f", got, want)
	}
}

func TestFloat64Value(t *testing.T) {
	if float.Float64Value != 3.141592653589793 {
		t.Errorf("Float64Value = %f, want 3.141592653589793", float.Float64Value)
	}
	if got := reflect.TypeOf(float.Float64Value).Kind(); got != reflect.Float64 {
		t.Errorf("Float64Value type = %s, want float64", got)
	}
	if got, want := float.Float64Value, float64(3.141592653589793); got != want {
		t.Errorf("Float64Value = %f, want %f", got, want)
	}
}

func TestFloat32MaxValue(t *testing.T) {
	if float.Float32MaxValue != 3.4028235e+38 {
		t.Errorf("Float32MaxValue = %e, want 3.4028235e+38", float.Float32MaxValue)
	}
	if got := reflect.TypeOf(float.Float32MaxValue).Kind(); got != reflect.Float32 {
		t.Errorf("Float32MaxValue type = %s, want float32", got)
	}
	if got, want := float.Float32MaxValue, float32(3.4028235e+38); got != want {
		t.Errorf("Float32MaxValue = %e, want %e", got, want)
	}
}

func TestFloat32MinValue(t *testing.T) {
	if float.Float32MinValue != 1.401298e-45 {
		t.Errorf("Float32MinValue = %e, want 1.401298e-45", float.Float32MinValue)
	}
	if got := reflect.TypeOf(float.Float32MinValue).Kind(); got != reflect.Float32 {
		t.Errorf("Float32MinValue type = %s, want float32", got)
	}
	if got, want := float.Float32MinValue, float32(1.401298e-45); got != want {
		t.Errorf("Float32MinValue = %e, want %e", got, want)
	}
}

func TestFloat64MaxValue(t *testing.T) {
	if float.Float64MaxValue != 1.7976931348623157e+308 {
		t.Errorf("Float64MaxValue = %e, want 1.7976931348623157e+308", float.Float64MaxValue)
	}
	if got := reflect.TypeOf(float.Float64MaxValue).Kind(); got != reflect.Float64 {
		t.Errorf("Float64MaxValue type = %s, want float64", got)
	}
	if got, want := float.Float64MaxValue, float64(1.7976931348623157e+308); got != want {
		t.Errorf("Float64MaxValue = %e, want %e", got, want)
	}
}

func TestFloat64MinValue(t *testing.T) {
	if float.Float64MinValue != 5e-324 {
		t.Errorf("Float64MinValue = %e, want 5e-324", float.Float64MinValue)
	}
	if got := reflect.TypeOf(float.Float64MinValue).Kind(); got != reflect.Float64 {
		t.Errorf("Float64MinValue type = %s, want float64", got)
	}
	if got, want := float.Float64MinValue, float64(5e-324); got != want {
		t.Errorf("Float64MinValue = %e, want %e", got, want)
	}
}
