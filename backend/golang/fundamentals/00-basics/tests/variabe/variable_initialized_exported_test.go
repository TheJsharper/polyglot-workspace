package variable_test

import (
	"reflect"
	"testing"

	variables "example.com/fundamentals/00-basics/src/variable"
)

func TestVariableInitializedExportedVariables(t *testing.T) {
	t.Run("Text", TestVariableInitializedExportedText)
	t.Run("Number", TestVariableInitializedExportedNumber)
	t.Run("Float", TestVariableInitializedExportedFloat)
	t.Run("Boolean", TestVariableInitializedExportedBoolean)
	t.Run("Complex", TestVariableInitializedExportedComplex)
}
func TestVariableInitializedExportedText(t *testing.T) {
	if variables.Text != "John Doe" {
		t.Errorf("Expected Text to be 'John Doe', but got '%s'", variables.Text)
	}
	if got, want := reflect.TypeOf(variables.Text), reflect.TypeOf("John Doe"); got != want {
		t.Errorf("variables.Text = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(variables.Text).Kind(), reflect.String; got != want {
		t.Errorf("variables.Text has kind %v, want %v", got, want)
	}
}

func TestVariableInitializedExportedNumber(t *testing.T) {
	if variables.Number != 42 {
		t.Errorf("Expected Number to be 42, but got %d", variables.Number)
	}
	if got, want := reflect.TypeOf(variables.Number), reflect.TypeOf(42); got != want {
		t.Errorf("variables.Number = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(variables.Number).Kind(), reflect.Int; got != want {
		t.Errorf("variables.Number has kind %v, want %v", got, want)

	}
}
func TestVariableInitializedExportedFloat(t *testing.T) {
	if variables.Float != 3.14 {
		t.Errorf("Expected Float to be 3.14, but got %f", variables.Float)
	}
	if got, want := reflect.TypeOf(variables.Float), reflect.TypeOf(3.14); got != want {
		t.Errorf("variables.Float = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(variables.Float).Kind(), reflect.Float64; got != want {
		t.Errorf("variables.Float has kind %v, want %v", got, want)
	}
}

func TestVariableInitializedExportedBoolean(t *testing.T) {
	if variables.Boolean != true {
		t.Errorf("Expected Boolean to be true, but got %v", variables.Boolean)
	}
	if got, want := reflect.TypeOf(variables.Boolean), reflect.TypeOf(true); got != want {
		t.Errorf("variables.Boolean = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(variables.Boolean).Kind(), reflect.Bool; got != want {
		t.Errorf("variables.Boolean has kind %v, want %v", got, want)
	}
}

func TestVariableInitializedExportedComplex(t *testing.T) {
	if variables.Complex != complex(1, 2) {
		t.Errorf("Expected Complex to be (1+2i), but got %v", variables.Complex)
	}
	if got, want := reflect.TypeOf(variables.Complex), reflect.TypeOf(complex(1, 2)); got != want {
		t.Errorf("variables.Complex = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(variables.Complex).Kind(), reflect.Complex128; got != want {
		t.Errorf("variables.Complex has kind %v, want %v", got, want)
	}
}
