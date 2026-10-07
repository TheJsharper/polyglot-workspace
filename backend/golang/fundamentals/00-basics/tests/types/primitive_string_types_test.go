package types_test

import (
	"reflect"
	"testing"

	strings "example.com/fundamentals/00-basics/src/types"
)

func TestRunStringTests(t *testing.T) {
	t.Run("TestTextStringValue", TestTextStringValue)
	t.Run("TestContainsHelloValue", TestContainsHelloValue)
	t.Run("TestHasPrefixValue", TestHasPrefixValue)
	t.Run("TestHasSuffixValue", TestHasSuffixValue)
	t.Run("TestIndexOfHelloValue", TestIndexOfHelloValue)
	t.Run("TestLastIndexOfWorldValue", TestLastIndexOfWorldValue)
	t.Run("TestCountOfLValue", TestCountOfLValue)
	t.Run("TestSplitByCommaValue", TestSplitByCommaValue)
	t.Run("TestJoinWithSpaceValue", TestJoinWithSpaceValue)
	t.Run("TestFieldsOfStringValue", TestFieldsOfStringValue)
	t.Run("TestTrimmedStringValue", TestTrimmedStringValue)
	t.Run("TestToLowerCaseValue", TestToLowerCaseValue)
	t.Run("TestToUpperCaseValue", TestToUpperCaseValue)
	t.Run("TestTrimSpacesValue", TestTrimSpacesValue)
	t.Run("TestFieldsFuncStringValue", TestFieldsFuncStringValue)
	t.Run("TestSplitFuncStringValue", TestSplitFuncStringValue)
}

func TestTextStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.TextString).Kind(); got != reflect.String {
		t.Errorf("TextStringValue type = %s, want string", got)
	}
	if got, want := strings.TextString, "Hello, World!"; got != want {
		t.Errorf("TextStringValue = %s, want %s", got, want)
	}
}

func TestContainsHelloValue(t *testing.T) {
	if got := reflect.TypeOf(strings.ContainsHello).Kind(); got != reflect.Bool {
		t.Errorf("ContainsHelloValue type = %s, want bool", got)
	}
	if got, want := strings.ContainsHello, true; got != want {
		t.Errorf("ContainsHelloValue = %v, want %v", got, want)
	}
}

func TestHasPrefixValue(t *testing.T) {
	if got := reflect.TypeOf(strings.HasPrefix).Kind(); got != reflect.Bool {
		t.Errorf("HasPrefixValue type = %s, want bool", got)
	}
	if got, want := strings.HasPrefix, true; got != want {
		t.Errorf("HasPrefixValue = %v, want %v", got, want)
	}
}

func TestHasSuffixValue(t *testing.T) {
	if got := reflect.TypeOf(strings.HasSuffix).Kind(); got != reflect.Bool {
		t.Errorf("HasSuffixValue type = %s, want bool", got)
	}
	if got, want := strings.HasSuffix, true; got != want {
		t.Errorf("HasSuffixValue = %v, want %v", got, want)
	}
}

func TestIndexOfHelloValue(t *testing.T) {
	if got := reflect.TypeOf(strings.IndexOfHello).Kind(); got != reflect.Int {
		t.Errorf("IndexOfHelloValue type = %s, want int", got)
	}
	if got, want := strings.IndexOfHello, 0; got != want {
		t.Errorf("IndexOfHelloValue = %d, want %d", got, want)
	}
}

func TestLastIndexOfWorldValue(t *testing.T) {
	if got := reflect.TypeOf(strings.LastIndexOfWorld).Kind(); got != reflect.Int {
		t.Errorf("LastIndexOfWorldValue type = %s, want int", got)
	}
	if got, want := strings.LastIndexOfWorld, 7; got != want {
		t.Errorf("LastIndexOfWorldValue = %d, want %d", got, want)
	}
}

func TestCountOfLValue(t *testing.T) {
	if got := reflect.TypeOf(strings.CountOfL).Kind(); got != reflect.Int {
		t.Errorf("CountOfLValue type = %s, want int", got)
	}
	if got, want := strings.CountOfL, 3; got != want {
		t.Errorf("CountOfLValue = %d, want %d", got, want)
	}
}

func TestSplitByCommaValue(t *testing.T) {
	if got := reflect.TypeOf(strings.SplitByComma).Kind(); got != reflect.Slice {
		t.Errorf("SplitByCommaValue type = %s, want slice", got)
	}
	if got, want := strings.SplitByComma, []string{"Hello", " World!"}; !reflect.DeepEqual(got, want) {
		t.Errorf("SplitByCommaValue = %v, want %v", got, want)
	}
}

func TestJoinWithSpaceValue(t *testing.T) {
	if got := reflect.TypeOf(strings.JoinWithSpace).Kind(); got != reflect.String {
		t.Errorf("JoinWithSpaceValue type = %s, want string", got)
	}
	if got, want := strings.JoinWithSpace, "Hello World"; got != want {
		t.Errorf("JoinWithSpaceValue = %s, want %s", got, want)
	}
}
func TestFieldsOfStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.FieldsOfString).Kind(); got != reflect.String {
		t.Errorf("FieldsOfStringValue type = %s, want string", got)
	}
	if got, want := strings.FieldsOfString, "   Trimmed String   "; got != want {
		t.Errorf("FieldsOfStringValue = %s, want %s", got, want)
	}
}

func TestTrimmedStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.TrimmedString).Kind(); got != reflect.String {
		t.Errorf("TrimmedStringValue type = %s, want string", got)
	}
	if got, want := strings.TrimmedString, "Trimmed String"; got != want {
		t.Errorf("TrimmedStringValue = %s, want %s", got, want)
	}
}

func TestToLowerCaseValue(t *testing.T) {
	if got := reflect.TypeOf(strings.ToLowerCase).Kind(); got != reflect.String {
		t.Errorf("ToLowerCaseValue type = %s, want string", got)
	}
	if got, want := strings.ToLowerCase, "hello, world!"; got != want {
		t.Errorf("ToLowerCaseValue = %s, want %s", got, want)
	}
}

func TestToUpperCaseValue(t *testing.T) {
	if got := reflect.TypeOf(strings.ToUpperCase).Kind(); got != reflect.String {
		t.Errorf("ToUpperCaseValue type = %s, want string", got)
	}
	if got, want := strings.ToUpperCase, "HELLO, WORLD!"; got != want {
		t.Errorf("ToUpperCaseValue = %s, want %s", got, want)
	}
}
func TestTrimSpacesValue(t *testing.T) {
	if got := reflect.TypeOf(strings.TrimSpaces).Kind(); got != reflect.String {
		t.Errorf("TrimSpacesValue type = %s, want string", got)
	}
	if got, want := strings.TrimSpaces, "Go is awesome!"; got != want {
		t.Errorf("TrimSpacesValue = %s, want %s", got, want)
	}
}

func TestReplacedStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.ReplacedString).Kind(); got != reflect.String {
		t.Errorf("ReplacedStringValue type = %s, want string", got)
	}
	if got, want := strings.ReplacedString, "Hello, Gophers!"; got != want {
		t.Errorf("ReplacedStringValue = %s, want %s", got, want)
	}
}

func TestReplaceAllStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.ReplaceAllString).Kind(); got != reflect.String {
		t.Errorf("ReplaceAllStringValue type = %s, want string", got)
	}
	if got, want := strings.ReplaceAllString, "HeLLo, WorLd!"; got != want {
		t.Errorf("ReplaceAllStringValue = %s, want %s", got, want)
	}
}

func TestRepeatedStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.RepeatedString).Kind(); got != reflect.String {
		t.Errorf("RepeatedStringValue type = %s, want string", got)
	}
	if got, want := strings.RepeatedString, "Go! Go! Go! "; got != want {
		t.Errorf("RepeatedStringValue = %s, want %s", got, want)
	}
}

func TestSplitNStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.SplitNString).Kind(); got != reflect.Slice {
		t.Errorf("SplitNStringValue type = %s, want slice", got)
	}
	if got, want := strings.SplitNString, []string{"Go", "is", "awesome!"}; !reflect.DeepEqual(got, want) {
		t.Errorf("SplitNStringValue = %s, want %s", got, want)
	}
}

func TestSplitAfterStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.SplitAfterString).Kind(); got != reflect.Slice {
		t.Errorf("SplitAfterStringValue type = %s, want slice", got)
	}
	if got, want := strings.SplitAfterString, []string{"Hello,", " World!"}; !reflect.DeepEqual(got, want) {
		t.Errorf("SplitAfterStringValue = %v, want %v", got, want)
	}
}

func TestSplitAfterNStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.SplitAfterNString).Kind(); got != reflect.Slice {
		t.Errorf("SplitAfterNStringValue type = %s, want slice", got)
	}
	if got, want := strings.SplitAfterNString, []string{"Hello,", " World!"}; !reflect.DeepEqual(got, want) {
		t.Errorf("SplitAfterNStringValue = %v, want %v", got, want)
	}
}

func TestTrimLeftStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.TrimLeftString).Kind(); got != reflect.String {
		t.Errorf("TrimLeftStringValue type = %s, want string", got)
	}
	if got, want := strings.TrimLeftString, "Go is awesome!"; got != want {
		t.Errorf("TrimLeftStringValue = %s, want %s", got, want)
	}
}

func TestTrimRightStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.TrimRightString).Kind(); got != reflect.String {
		t.Errorf("TrimRightStringValue type = %s, want string", got)
	}
	if got, want := strings.TrimRightString, "Hello, Go is awesome!"; got != want {
		t.Errorf("TrimRightStringValue = %s, want %s", got, want)
	}
}

func TestTrimPrefixStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.TrimPrefixString).Kind(); got != reflect.String {
		t.Errorf("TrimPrefixStringValue type = %s, want string", got)
	}
	if got, want := strings.TrimPrefixString, "World!"; got != want {
		t.Errorf("TrimPrefixStringValue = %s, want %s", got, want)
	}
}

func TestTrimSuffixStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.TrimSuffixString).Kind(); got != reflect.String {
		t.Errorf("TrimSuffixStringValue type = %s, want string", got)
	}
	if got, want := strings.TrimSuffixString, "Hello,"; got != want {
		t.Errorf("TrimSuffixStringValue = %s, want %s", got, want)
	}
}

func TestEqualFoldStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.EqualFoldString).Kind(); got != reflect.Bool {
		t.Errorf("EqualFoldStringValue type = %s, want bool", got)
	}
	if got, want := strings.EqualFoldString, true; got != want {
		t.Errorf("EqualFoldStringValue = %v, want %v", got, want)
	}
}

func TestCompareStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.CompareString).Kind(); got != reflect.Int {
		t.Errorf("CompareStringValue type = %s, want int", got)
	}
	if got, want := strings.CompareString, -1; got != want {
		t.Errorf("CompareStringValue = %d, want %d", got, want)
	}
}

func TestIndexByteStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.IndexByteString).Kind(); got != reflect.Int {
		t.Errorf("IndexByteStringValue type = %s, want int", got)
	}
	if got, want := strings.IndexByteString, 4; got != want {
		t.Errorf("IndexByteStringValue = %d, want %d", got, want)
	}
}

func TestLastIndexByteStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.LastIndexByteString).Kind(); got != reflect.Int {
		t.Errorf("LastIndexByteStringValue type = %s, want int", got)
	}
	if got, want := strings.LastIndexByteString, 8; got != want {
		t.Errorf("LastIndexByteStringValue = %d, want %d", got, want)
	}
}
func TestIndexRuneStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.IndexRuneString).Kind(); got != reflect.Int {
		t.Errorf("IndexRuneStringValue type = %s, want int", got)
	}
	if got, want := strings.IndexRuneString, 6; got != want {
		t.Errorf("IndexRuneStringValue = %d, want %d", got, want)
	}
}

func TestLastIndexRuneStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.LastIndexRuneString).Kind(); got != reflect.Int {
		t.Errorf("LastIndexRuneStringValue type = %s, want int", got)
	}
	if got, want := strings.LastIndexRuneString, 8; got != want {
		t.Errorf("LastIndexRuneStringValue = %d, want %d", got, want)
	}
}

func TestMapStringToUpperValue(t *testing.T) {
	if got := reflect.TypeOf(strings.MapStringToUpper).Kind(); got != reflect.String {
		t.Errorf("MapStringToUpperValue type = %s, want string", got)
	}
	if got, want := strings.MapStringToUpper, "HELLO, WORLD!"; got != want {
		t.Errorf("MapStringToUpperValue = %s, want %s", got, want)
	}
}
func TestMapStringToLowerValue(t *testing.T) {
	if got := reflect.TypeOf(strings.MapStringToLower).Kind(); got != reflect.String {
		t.Errorf("MapStringToLowerValue type = %s, want string", got)
	}
	if got, want := strings.MapStringToLower, "hello, world!"; got != want {
		t.Errorf("MapStringToLowerValue = %s, want %s", got, want)
	}
}

func TestTrimFuncStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.TrimFuncString).Kind(); got != reflect.String {
		t.Errorf("TrimFuncStringValue type = %s, want string", got)
	}
	if got, want := strings.TrimFuncString, "ello, World"; got != want {
		t.Errorf("TrimFuncStringValue = %s, want %s", got, want)
	}
}

func TestTrimLeftFuncStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.TrimLeftFuncString).Kind(); got != reflect.String {
		t.Errorf("TrimLeftFuncStringValue type = %s, want string", got)
	}
	if got, want := strings.TrimLeftFuncString, "ello, World!"; got != want {
		t.Errorf("TrimLeftFuncStringValue = %s, want %s", got, want)
	}
}

func TestTrimRightFuncStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.TrimRightFuncString).Kind(); got != reflect.String {
		t.Errorf("TrimRightFuncStringValue type = %s, want string", got)
	}
	if got, want := strings.TrimRightFuncString, "Hello, World"; got != want {
		t.Errorf("TrimRightFuncStringValue = %s, want %s", got, want)
	}
}

func TestFieldsFuncStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.FieldsFuncString).Kind(); got != reflect.Slice {
		t.Errorf("FieldsFuncStringValue type = %s, want slice", got)
	}
	if got, want := strings.FieldsFuncString, []string{"Hello", "World!"}; !reflect.DeepEqual(got, want) {
		t.Errorf("FieldsFuncStringValue = %v, want %v", got, want)
	}
}

func TestSplitFuncStringValue(t *testing.T) {
	if got := reflect.TypeOf(strings.SplitFuncString).Kind(); got != reflect.Slice {
		t.Errorf("SplitFuncStringValue type = %s, want slice", got)
	}
	if got, want := strings.SplitFuncString, []string{"Hello", "World!"}; !reflect.DeepEqual(got, want) {
		t.Errorf("SplitFuncStringValue = %v, want %v", got, want)
	}
}
