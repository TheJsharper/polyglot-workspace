package constants_test

import (
	"reflect"
	"testing"

	constants "example.com/fundamentals/00-basics/src/constants"
)

func TestConstants(t *testing.T) {
	t.Run("AppName", TestAppName)
	t.Run("MaxUsers", TestMaxUsers)
	t.Run("Pi", TestPi)
	t.Run("IsProduction", TestIsProduction)
	t.Run("ComplexNumber", TestComplexNumber)
}

func TestAppName(t *testing.T) {
	if constants.AppName != "Fundamentals" {
		t.Errorf("Expected AppName to be 'Fundamentals', but got '%s'", constants.AppName)
	}
	if got, want := reflect.TypeOf(constants.AppName), reflect.TypeOf("Fundamentals"); got != want {
		t.Errorf("constants.AppName = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(constants.AppName).Kind(), reflect.String; got != want {
		t.Errorf("constants.AppName has kind %v, want %v", got, want)
	}
	if got := reflect.TypeOf(constants.AppName).AssignableTo(reflect.TypeOf("")); !got {
		t.Errorf("constants.AppName is not assignable to string")
	}
}

func TestMaxUsers(t *testing.T) {
	if constants.MaxUsers != 100 {
		t.Errorf("Expected MaxUsers to be 100, but got %d", constants.MaxUsers)
	}
	if got, want := reflect.TypeOf(constants.MaxUsers), reflect.TypeOf(100); got != want {
		t.Errorf("constants.MaxUsers = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(constants.MaxUsers).Kind(), reflect.Int; got != want {
		t.Errorf("constants.MaxUsers has kind %v, want %v", got, want)
	}
	if got := reflect.TypeOf(constants.MaxUsers).AssignableTo(reflect.TypeOf(100)); !got {
		t.Errorf("constants.MaxUsers is not assignable to int")
	}
}

func TestPi(t *testing.T) {
	if constants.Pi != 3.14159 {
		t.Errorf("Expected Pi to be 3.14159, but got %f", constants.Pi)
	}
	if got, want := reflect.TypeOf(constants.Pi), reflect.TypeOf(3.14159); got != want {
		t.Errorf("constants.Pi = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(constants.Pi).Kind(), reflect.Float64; got != want {
		t.Errorf("constants.Pi has kind %v, want %v", got, want)
	}
	if got := reflect.TypeOf(constants.Pi).AssignableTo(reflect.TypeOf(3.14159)); !got {
		t.Errorf("constants.Pi is not assignable to float64")
	}
}

func TestIsProduction(t *testing.T) {
	if constants.IsProduction != false {
		t.Errorf("Expected IsProduction to be false, but got %v", constants.IsProduction)
	}
	if got, want := reflect.TypeOf(constants.IsProduction), reflect.TypeOf(false); got != want {
		t.Errorf("constants.IsProduction = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(constants.IsProduction).Kind(), reflect.Bool; got != want {
		t.Errorf("constants.IsProduction has kind %v, want %v", got, want)
	}
	if got := reflect.TypeOf(constants.IsProduction).AssignableTo(reflect.TypeOf(false)); !got {
		t.Errorf("constants.IsProduction is not assignable to bool")
	}
}
func TestComplexNumber(t *testing.T) {
	if constants.ComplexNumber != complex(1, 2) {
		t.Errorf("Expected ComplexNumber to be (1+2i), but got %v", constants.ComplexNumber)
	}
	if got, want := reflect.TypeOf(constants.ComplexNumber), reflect.TypeOf(complex(1, 2)); got != want {
		t.Errorf("constants.ComplexNumber = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(constants.ComplexNumber).Kind(), reflect.Complex128; got != want {
		t.Errorf("constants.ComplexNumber has kind %v, want %v", got, want)
	}
	if got := reflect.TypeOf(constants.ComplexNumber).AssignableTo(reflect.TypeOf(complex(1, 2))); !got {
		t.Errorf("constants.ComplexNumber is not assignable to complex128")
	}
}
