package variable_test

import (
	"reflect"
	"testing"

	variables "example.com/fundamentals/00-basics/src/variable"
)

func TestUninitializedVariables(t *testing.T) {
	t.Run("String", TestUninitializedString)
	t.Run("Int", TestUninitializedInt)
	t.Run("Float", TestUninitializedFloat)
	t.Run("Bool", TestUninitializedBool)
	t.Run("Complex", TestUninitializedComplex)
}

func TestUninitializedString(t *testing.T) {
	if got, want := variables.UninitializedString, ""; got != want {
		t.Errorf("variables.UninitializedString = %q, want %q", got, want)
	}
	if got, want := reflect.TypeOf(variables.UninitializedString), reflect.TypeOf(""); got != want {
		t.Errorf("variables.UninitializedString has type %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(variables.UninitializedString).Kind(), reflect.String; got != want {
		t.Errorf("variables.UninitializedString has kind %v, want %v", got, want)
	}

}

func TestUninitializedInt(t *testing.T) {
	if got, want := variables.UninitializedInt, 0; got != want {
		t.Errorf("variables.UninitializedInt = %d, want %d", got, want)
	}
	if got, want := reflect.TypeOf(variables.UninitializedInt), reflect.TypeOf(0); got != want {
		t.Errorf("variables.UninitializedInt has type %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(variables.UninitializedInt).Kind(), reflect.Int; got != want {
		t.Errorf("variables.UninitializedInt has kind %v, want %v", got, want)
	}
}

func TestUninitializedFloat(t *testing.T) {
	if got, want := variables.UninitializedFloat, 0.0; got != want {
		t.Errorf("variables.UninitializedFloat = %f, want %f", got, want)
	}
	if got, want := reflect.TypeOf(variables.UninitializedFloat), reflect.TypeOf(0.0); got != want {
		t.Errorf("variables.UninitializedFloat has type %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(variables.UninitializedFloat).Kind(), reflect.Float64; got != want {
		t.Errorf("variables.UninitializedFloat has kind %v, want %v", got, want)
	}
}

func TestUninitializedBool(t *testing.T) {
	if got, want := variables.UninitializedBool, false; got != want {
		t.Errorf("variables.UninitializedBool = %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(variables.UninitializedBool), reflect.TypeOf(false); got != want {
		t.Errorf("variables.UninitializedBool has type %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(variables.UninitializedBool).Kind(), reflect.Bool; got != want {
		t.Errorf("variables.UninitializedBool has kind %v, want %v", got, want)
	}
}

func TestUninitializedComplex(t *testing.T) {
	if got, want := variables.UninitializedComplex, complex(0, 0); got != want {
		t.Errorf("variables.UninitializedComplex = %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(variables.UninitializedComplex), reflect.TypeOf(complex(0, 0)); got != want {
		t.Errorf("variables.UninitializedComplex has type %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(variables.UninitializedComplex).Kind(), reflect.Complex128; got != want {
		t.Errorf("variables.UninitializedComplex has kind %v, want %v", got, want)
	}
}
