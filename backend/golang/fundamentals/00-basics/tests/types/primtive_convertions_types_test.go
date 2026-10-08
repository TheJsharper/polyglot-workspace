package types_test

import (
	"reflect"
	"testing"

	convertions "example.com/fundamentals/00-basics/src/types"
)

func TestConvertionsPackage(t *testing.T) {
	t.Run("TestConvertionResult_1", TestConvertionResult_1)
	t.Run("TestCOnvertionsResult", TestCOnvertionsResult)
	t.Run("TestFahrenheitValue", TestFahrenheitValue)
	t.Run("TestCelsiusValue", TestCelsiusValue)
	t.Run("TestKelvinValue", TestKelvinValue)
	t.Run("TestValue_2", TestValue_2)
	t.Run("TestValue", TestValue)
}

func TestConvertionResult_1(t *testing.T) {

	if got, want := convertions.Result, float32(3.0); got != want {
		t.Errorf("Result = %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(convertions.Result), reflect.TypeOf(float32(3.0)); got != want {
		t.Errorf("TypeOf(Result) = %v, want %v", got, want)
	}
	if got, want := reflect.ValueOf(convertions.Result).Float(), float64(3.0); got != want {
		t.Errorf("ValueOf(Result).Float() = %v, want %v", got, want)
	}

}
func TestCOnvertionsResult(t *testing.T) {
	if got, want := convertions.Result, float32(3.0); got != want {
		t.Errorf("Result = %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(convertions.Result), reflect.TypeOf(float32(3.0)); got != want {
		t.Errorf("TypeOf(Result) = %v, want %v", got, want)
	}
	if got, want := reflect.ValueOf(convertions.Result).Float(), float64(3.0); got != want {
		t.Errorf("ValueOf(Result).Float() = %v, want %v", got, want)
	}

}

func TestFahrenheitValue(t *testing.T) {
	if got, want := convertions.FahrenheitValue, float32(77); got != want {
		t.Errorf("FahrenheitValue = %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(convertions.FahrenheitValue), reflect.TypeOf(float32(77)); got != want {
		t.Errorf("TypeOf(FahrenheitValue) = %v, want %v", got, want)
	}
	if got, want := reflect.ValueOf(convertions.FahrenheitValue).Float(), float64(77); got != want {
		t.Errorf("ValueOf(FahrenheitValue).Float() = %v, want %v", got, want)
	}
}

func TestCelsiusValue(t *testing.T) {
	if got, want := convertions.CelsiusValue, 25; got != want {
		t.Errorf("CelsiusValue = %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(convertions.CelsiusValue), reflect.TypeOf(25); got != want {
		t.Errorf("TypeOf(CelsiusValue) = %v, want %v", got, want)
	}
	if got, want := reflect.ValueOf(convertions.CelsiusValue).Int(), int64(25); got != want {
		t.Errorf("ValueOf(CelsiusValue).Int() = %v, want %v", got, want)
	}
}

func TestKelvinValue(t *testing.T) {
	if got, want := convertions.KelvinValue, float32(298.15); got != want {
		t.Errorf("KelvinValue = %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(convertions.KelvinValue), reflect.TypeOf(float32(298.15)); got != want {
		t.Errorf("TypeOf(KelvinValue) = %v, want %v", got, want)
	}
	if got, want := reflect.ValueOf(convertions.KelvinValue).Float(), float64(float32(298.15)); got != want {
		t.Errorf("ValueOf(KelvinValue).Float() = %v, want %v", got, want)
	}
}
func TestValue_2(t *testing.T) {
	if got, want := convertions.Value_2, 3.141592653589793; got != want {
		t.Errorf("Value_2 = %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(convertions.Value_2), reflect.TypeOf(3.141592653589793); got != want {
		t.Errorf("TypeOf(Value_2) = %v, want %v", got, want)
	}
	if got, want := reflect.ValueOf(convertions.Value_2).Float(), 3.141592653589793; got != want {
		t.Errorf("ValueOf(Value_2).Float() = %v, want %v", got, want)
	}
}

func TestValue(t *testing.T) {
	if got, want := convertions.Value, float32(3.14); got != want {
		t.Errorf("Value = %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(convertions.Value), reflect.TypeOf(float32(3.14)); got != want {
		t.Errorf("TypeOf(Value) = %v, want %v", got, want)
	}
	if got, want := reflect.ValueOf(convertions.Value).Float(), float64(float32(3.14)); got != want {
		t.Errorf("ValueOf(Value).Float() = %v, want %v", got, want)
	}
}
