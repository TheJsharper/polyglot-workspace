package tests

import (
	"reflect"
	"testing"

	variables "example.com/fundamentals/00-basics/src/variable"
)

func TestTextValueAndType(t *testing.T) {
	if got, want := variables.Text, "John Doe"; got != want {
		t.Errorf("variables.Text = %q, want %q", got, want)
	}

	if got, want := reflect.TypeOf(variables.Text).Kind(), reflect.String; got != want {
		t.Errorf("type of variables.Text = %v, want %v", got, want)
	}
}
func TestNumberValueAndType(t *testing.T) {
	if got, want := variables.Number, 42; got != want {
		t.Errorf("variables.Number = %d, want %d", got, want)
	}

	if got, want := reflect.TypeOf(variables.Number).Kind(), reflect.Int; got != want {
		t.Errorf("type of variables.Number = %v, want %v", got, want)
	}
}

func TestFloatValueAndType(t *testing.T) {
	if got, want := variables.Float, 3.14; got != want {
		t.Errorf("variables.Float = %f, want %f", got, want)
	}

	if got, want := reflect.TypeOf(variables.Float).Kind(), reflect.Float64; got != want {
		t.Errorf("type of variables.Float = %v, want %v", got, want)
	}
}

func TestBooleanValueAndType(t *testing.T) {
	if got, want := variables.Boolean, true; got != want {
		t.Errorf("variables.Boolean = %v, want %v", got, want)
	}

	if got, want := reflect.TypeOf(variables.Boolean).Kind(), reflect.Bool; got != want {
		t.Errorf("type of variables.Boolean = %v, want %v", got, want)
	}
}

func TestComplexValueAndType(t *testing.T) {
	if got, want := variables.Complex, complex(1, 2); got != want {
		t.Errorf("variables.Complex = %v, want %v", got, want)
	}

	if got, want := reflect.TypeOf(variables.Complex).Kind(), reflect.Complex128; got != want {
		t.Errorf("type of variables.Complex = %v, want %v", got, want)
	}
}
func TestUninitializedVariables(t *testing.T) {
	if got, want := variables.UninitializedString, ""; got != want {
		t.Errorf("variables.UninitializedString = %q, want %q", got, want)
	}

	if got, want := variables.UninitializedInt, 0; got != want {
		t.Errorf("variables.UninitializedInt = %d, want %d", got, want)
	}

	if got, want := variables.UninitializedFloat, 0.0; got != want {
		t.Errorf("variables.UninitializedFloat = %f, want %f", got, want)
	}

	if got, want := variables.UninitializedBool, false; got != want {
		t.Errorf("variables.UninitializedBool = %v, want %v", got, want)
	}

	if got, want := variables.UninitializedComplex, complex(0, 0); got != want {
		t.Errorf("variables.UninitializedComplex = %v, want %v", got, want)
	}
}
func TestInitializedVariables(t *testing.T) {
	if got, want := variables.InitializedString, "Hello, World!"; got != want {
		t.Errorf("variables.InitializedString = %q, want %q", got, want)
	}

	if got, want := variables.InitializedInt, 100; got != want {
		t.Errorf("variables.InitializedInt = %d, want %d", got, want)
	}

	if got, want := variables.InitializedFloat, 2.718; got != want {
		t.Errorf("variables.InitializedFloat = %f, want %f", got, want)
	}

	if got, want := variables.InitializedBool, false; got != want {
		t.Errorf("variables.InitializedBool = %v, want %v", got, want)
	}

	if got, want := variables.InitializedComplex, complex(3, 4); got != want {
		t.Errorf("variables.InitializedComplex = %v, want %v", got, want)
	}
}
