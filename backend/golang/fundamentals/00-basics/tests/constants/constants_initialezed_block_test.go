package constants_test

import (
	"reflect"
	"testing"

	constants "example.com/fundamentals/00-basics/src/constants"
)

func TestConstants_initialized_block(t *testing.T) {
	t.Run("InitializedString", TestInitializedConstantsString)
	t.Run("InitializedNumber", TestInitializedConstantsNumber)
	t.Run("InitializedFloat", TestInitializedConstantsFloat)
	t.Run("InitializedBoolean", TestInitializedConstantsBoolean)
	t.Run("InitializedComplex", TestInitializedConstantsComplex)
}

func TestInitializedConstantsString(t *testing.T) {
	if got, want := constants.InitializedString, "John Doe"; got != want {
		t.Errorf("Expected InitializedString to be 'John Doe', but got '%s'", constants.InitializedString)
	}
	if got, want := reflect.TypeOf(constants.InitializedString), reflect.TypeOf("John Doe"); got != want {
		t.Errorf("constants.InitializedString = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(constants.InitializedString).Kind(), reflect.String; got != want {
		t.Errorf("constants.InitializedString has kind %v, want %v", got, want)
	}
	if got := reflect.TypeOf(constants.InitializedString).AssignableTo(reflect.TypeOf("")); !got {
		t.Errorf("constants.InitializedString is not assignable to string")
	}

}

func TestInitializedConstantsNumber(t *testing.T) {
	if got, want := constants.InitializedNumber, 42; got != want {
		t.Errorf("Expected InitializedNumber to be 42, but got %d", constants.InitializedNumber)
	}
	if got, want := reflect.TypeOf(constants.InitializedNumber), reflect.TypeOf(42); got != want {
		t.Errorf("constants.InitializedNumber = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(constants.InitializedNumber).Kind(), reflect.Int; got != want {
		t.Errorf("constants.InitializedNumber has kind %v, want %v", got, want)
	}
	if got := reflect.TypeOf(constants.InitializedNumber).AssignableTo(reflect.TypeOf(0)); !got {
		t.Errorf("constants.InitializedNumber is not assignable to int")
	}
}

func TestInitializedConstantsFloat(t *testing.T) {
	if got, want := constants.InitializedFloat, 3.14; got != want {
		t.Errorf("Expected InitializedFloat to be 3.14, but got %f", constants.InitializedFloat)
	}
	if got, want := reflect.TypeOf(constants.InitializedFloat), reflect.TypeOf(3.14); got != want {
		t.Errorf("constants.InitializedFloat = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(constants.InitializedFloat).Kind(), reflect.Float64; got != want {
		t.Errorf("constants.InitializedFloat has kind %v, want %v", got, want)
	}
	if got := reflect.TypeOf(constants.InitializedFloat).AssignableTo(reflect.TypeOf(0.0)); !got {
		t.Errorf("constants.InitializedFloat is not assignable to float64")
	}
}

func TestInitializedConstantsBoolean(t *testing.T) {
	if got, want := constants.InitializedBoolean, true; got != want {
		t.Errorf("Expected InitializedBoolean to be true, but got %v", constants.InitializedBoolean)
	}
	if got, want := reflect.TypeOf(constants.InitializedBoolean), reflect.TypeOf(true); got != want {
		t.Errorf("constants.InitializedBoolean = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(constants.InitializedBoolean).Kind(), reflect.Bool; got != want {
		t.Errorf("constants.InitializedBoolean has kind %v, want %v", got, want)
	}
	if got := reflect.TypeOf(constants.InitializedBoolean).AssignableTo(reflect.TypeOf(false)); !got {
		t.Errorf("constants.InitializedBoolean is not assignable to bool")
	}
}
func TestInitializedConstantsComplex(t *testing.T) {
	if got, want := constants.InitializedComplex, complex(1, 2); got != want {
		t.Errorf("Expected InitializedComplex to be (1+2i), but got %v", constants.InitializedComplex)
	}
	if got, want := reflect.TypeOf(constants.InitializedComplex), reflect.TypeOf(complex(1, 2)); got != want {
		t.Errorf("constants.InitializedComplex = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(constants.InitializedComplex).Kind(), reflect.Complex128; got != want {
		t.Errorf("constants.InitializedComplex has kind %v, want %v", got, want)
	}
	if got := reflect.TypeOf(constants.InitializedComplex).AssignableTo(reflect.TypeOf(complex(0, 0))); !got {
		t.Errorf("constants.InitializedComplex is not assignable to complex128")
	}
}
