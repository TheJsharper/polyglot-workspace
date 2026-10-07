package variable_test

import (
	"reflect"
	"testing"

	variables "example.com/fundamentals/00-basics/src/variable"
)

func TestInitializedVariables(t *testing.T) {
	t.Run("String", testInitializedString)
	t.Run("Int", testInitializedInt)
	t.Run("Float", testInitializedFloat)
	t.Run("Bool", testInitializedBool)
}

func testInitializedString(t *testing.T) {
	if got, want := variables.InitializedString, "Hello, World!"; got != want {
		t.Errorf("variables.InitializedString = %q, want %q", got, want)
	}

	if got, want := reflect.TypeOf(variables.InitializedString), reflect.TypeOf(""); got != want {
		t.Errorf("variables.InitializedString has type %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(variables.InitializedString).Kind(), reflect.String; got != want {
		t.Errorf("variables.InitializedString has kind %v, want %v", got, want)
	}

}
func testInitializedInt(t *testing.T) {
	if got, want := variables.InitializedInt, 100; got != want {
		t.Errorf("variables.InitializedInt = %d, want %d", got, want)
	}
	if got, want := reflect.TypeOf(variables.InitializedInt), reflect.TypeOf(0); got != want {
		t.Errorf("variables.InitializedInt has type %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(variables.InitializedInt).Kind(), reflect.Int; got != want {
		t.Errorf("variables.InitializedInt has kind %v, want %v", got, want)
	}
}

func testInitializedFloat(t *testing.T) {
	if got, want := variables.InitializedFloat, 2.718; got != want {
		t.Errorf("variables.InitializedFloat = %f, want %f", got, want)
	}
	if got, want := reflect.TypeOf(variables.InitializedFloat), reflect.TypeOf(0.0); got != want {
		t.Errorf("variables.InitializedFloat has type %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(variables.InitializedFloat).Kind(), reflect.Float64; got != want {
		t.Errorf("variables.InitializedFloat has kind %v, want %v", got, want)
	}
}

func testInitializedBool(t *testing.T) {
	if got, want := variables.InitializedBool, false; got != want {
		t.Errorf("variables.InitializedBool = %v, want %v", got, want)
	}

	if got, want := reflect.TypeOf(variables.InitializedBool), reflect.TypeOf(false); got != want {
		t.Errorf("variables.InitializedBool has type %v, want %v", got, want)
	}
	if got, want := reflect.TypeOf(variables.InitializedBool).Kind(), reflect.Bool; got != want {
		t.Errorf("variables.InitializedBool has kind %v, want %v", got, want)
	}
}
