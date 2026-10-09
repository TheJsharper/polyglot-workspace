package operators_test

import (
	"reflect"
	"testing"

	operators "example.com/fundamentals/00-basics/src/operators"
)

func TestRunAllComparisons(t *testing.T) {
	t.Run("Adult", TestOperatorAdult)
	t.Run("Minor", TestOperatorMinor)
	t.Run("Senior", TestOperatorSenior)
	t.Run("Child", TestOperatorChild)
	t.Run("Teenager", TestOperatorTeenager)
	t.Run("YoungAdult", TestOperatorYoungAdult)
	t.Run("MiddleAged", TestOperatorMiddleAged)
	t.Run("Elderly", TestOperatorElderly)
	t.Run("NotAdult", TestOperatorNotAdult)
	t.Run("NotMinor", TestOperatorNotMinor)
	t.Run("NotSenior", TestOperatorNotSenior)
	t.Run("NotChild", TestOperatorNotChild)
}

func TestOperatorAdult(t *testing.T) {
	if got, want := operators.Adult, operators.Age >= 18; got != want {
		t.Errorf("Expected Adult to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.Adult), reflect.TypeOf(true); got != want {
		t.Errorf("Expected Adult to be %v, got %v", want, got)
	}
}

func TestOperatorMinor(t *testing.T) {
	if got, want := operators.Minor, operators.Age < 18; got != want {
		t.Errorf("Expected Minor to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.Minor), reflect.TypeOf(true); got != want {
		t.Errorf("Expected Minor to be %v, got %v", want, got)
	}
}

func TestOperatorSenior(t *testing.T) {
	if got, want := operators.Senior, operators.Age >= 65; got != want {
		t.Errorf("Expected Senior to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.Senior), reflect.TypeOf(true); got != want {
		t.Errorf("Expected Senior to be %v, got %v", want, got)
	}
}

func TestOperatorChild(t *testing.T) {
	if got, want := operators.Child, operators.Age < 13; got != want {
		t.Errorf("Expected Child to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.Child), reflect.TypeOf(true); got != want {
		t.Errorf("Expected Child to be %v, got %v", want, got)
	}
}

func TestOperatorTeenager(t *testing.T) {
	if got, want := operators.Teenager, operators.Age >= 13 && operators.Age < 18; got != want {
		t.Errorf("Expected Teenager to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.Teenager), reflect.TypeOf(true); got != want {
		t.Errorf("Expected Teenager to be %v, got %v", want, got)
	}
}

func TestOperatorYoungAdult(t *testing.T) {
	if got, want := operators.YoungAdult, operators.Age >= 18 && operators.Age < 30; got != want {
		t.Errorf("Expected YoungAdult to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.YoungAdult), reflect.TypeOf(true); got != want {
		t.Errorf("Expected YoungAdult to be %v, got %v", want, got)
	}
}

func TestOperatorMiddleAged(t *testing.T) {
	if got, want := operators.MiddleAged, operators.Age >= 30 && operators.Age < 65; got != want {
		t.Errorf("Expected MiddleAged to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.MiddleAged), reflect.TypeOf(true); got != want {
		t.Errorf("Expected MiddleAged to be %v, got %v", want, got)
	}
}

func TestOperatorElderly(t *testing.T) {
	if got, want := operators.Elderly, operators.Age >= 65; got != want {
		t.Errorf("Expected Elderly to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.Elderly), reflect.TypeOf(true); got != want {
		t.Errorf("Expected Elderly to be %v, got %v", want, got)
	}
}

func TestOperatorNotAdult(t *testing.T) {
	if got, want := operators.NotAdult, operators.Age < 18 || operators.Age >= 65; got != want {
		t.Errorf("Expected NotAdult to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.NotAdult), reflect.TypeOf(true); got != want {
		t.Errorf("Expected NotAdult to be %v, got %v", want, got)
	}
}

func TestOperatorNotMinor(t *testing.T) {
	if got, want := operators.NotMinor, operators.Age >= 18 || operators.Age >= 65; got != want {
		t.Errorf("Expected NotMinor to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.NotMinor), reflect.TypeOf(true); got != want {
		t.Errorf("Expected NotMinor to be %v, got %v", want, got)
	}
}

func TestOperatorNotSenior(t *testing.T) {
	if got, want := operators.NotSenior, operators.Age < 65 || operators.Age < 18; got != want {
		t.Errorf("Expected NotSenior to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.NotSenior), reflect.TypeOf(true); got != want {
		t.Errorf("Expected NotSenior to be %v, got %v", want, got)
	}
}

func TestOperatorNotChild(t *testing.T) {
	if got, want := operators.NotChild, operators.Age >= 13 || operators.Age >= 65; got != want {
		t.Errorf("Expected NotChild to be %v, got %v", want, got)
	}
	if got, want := reflect.TypeOf(operators.NotChild), reflect.TypeOf(true); got != want {
		t.Errorf("Expected NotChild to be %v, got %v", want, got)
	}
}
