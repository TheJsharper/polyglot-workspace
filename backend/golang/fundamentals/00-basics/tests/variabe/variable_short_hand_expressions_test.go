package variable_test

import (
	"reflect"
	"testing"

	variables "example.com/fundamentals/00-basics/src/variable"
)

func TestShortHandVariableInitializedExportedVariables(t *testing.T) {
	t.Run("Name", TestVariableShortName)
	t.Run("Age", TestVariableShortAge)
	t.Run("Email", TestVariableShortEmail)
	t.Run("Address", TestVariableShortAddress)
	t.Run("Phone", TestVariableShortPhone)
	t.Run("Occupation", TestVariableShortOccupation)
}

func TestVariableShortName(t *testing.T) {
	if got, want := variables.Name, "John Doe"; got != want {
		t.Errorf("Expected Name to be 'John Doe', but got '%s'", variables.Name)
	}
	if got, want := reflect.TypeOf(variables.Name), reflect.TypeOf("John Doe"); got != want {
		t.Errorf("variables.Name = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(variables.Name).Kind(), reflect.String; got != want {
		t.Errorf("variables.Name has kind %v, want %v", got, want)
	}

}
func TestVariableShortAge(t *testing.T) {
	if got, want := variables.Age, 30; got != want {
		t.Errorf("Expected Age to be 30, but got %d", variables.Age)
	}
	if got, want := reflect.TypeOf(variables.Age), reflect.TypeOf(30); got != want {
		t.Errorf("variables.Age = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(variables.Age).Kind(), reflect.Int; got != want {
		t.Errorf("variables.Age has kind %v, want %v", got, want)
	}
}

func TestVariableShortEmail(t *testing.T) {
	if got, want := variables.Email, "john.doe@example.com"; got != want {
		t.Errorf("Expected Email to be 'john.doe@example.com', but got '%s'", variables.Email)
	}
	if got, want := reflect.TypeOf(variables.Email), reflect.TypeOf("john.doe@example.com"); got != want {
		t.Errorf("variables.Email = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(variables.Email).Kind(), reflect.String; got != want {
		t.Errorf("variables.Email has kind %v, want %v", got, want)
	}
}

func TestVariableShortAddress(t *testing.T) {
	if got, want := variables.Address, "123 Main St"; got != want {
		t.Errorf("Expected Address to be '123 Main St', but got '%s'", variables.Address)
	}
	if got, want := reflect.TypeOf(variables.Address), reflect.TypeOf("123 Main St"); got != want {
		t.Errorf("variables.Address = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(variables.Address).Kind(), reflect.String; got != want {
		t.Errorf("variables.Address has kind %v, want %v", got, want)
	}
}

func TestVariableShortPhone(t *testing.T) {
	if got, want := variables.Phone, "555-1234"; got != want {
		t.Errorf("Expected Phone to be '555-1234', but got '%s'", variables.Phone)
	}
	if got, want := reflect.TypeOf(variables.Phone), reflect.TypeOf("555-1234"); got != want {
		t.Errorf("variables.Phone = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(variables.Phone).Kind(), reflect.String; got != want {
		t.Errorf("variables.Phone has kind %v, want %v", got, want)
	}
}

func TestVariableShortOccupation(t *testing.T) {
	if got, want := variables.Occupation, "Software Engineer"; got != want {
		t.Errorf("Expected Occupation to be 'Software Engineer', but got '%s'", variables.Occupation)
	}
	if got, want := reflect.TypeOf(variables.Occupation), reflect.TypeOf("Software Engineer"); got != want {
		t.Errorf("variables.Occupation = %T, want %T", got, want)
	}
	if got, want := reflect.TypeOf(variables.Occupation).Kind(), reflect.String; got != want {
		t.Errorf("variables.Occupation has kind %v, want %v", got, want)
	}
}
