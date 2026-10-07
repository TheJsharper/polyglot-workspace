package types_test

import (
	"testing"

	"example.com/fundamentals/00-basics/src/types"
)

func TestIntegerMaxValues(t *testing.T) {
	if types.Int8 != 127 {
		t.Errorf("Int8 = %d, want 127", types.Int8)
	}
	if types.Int16 != 32767 {
		t.Errorf("Int16 = %d, want 32767", types.Int16)
	}
	if types.Int32 != 2147483647 {
		t.Errorf("Int32 = %d, want 2147483647", types.Int32)
	}
	if types.Int64 != 9223372036854775807 {
		t.Errorf("Int64 = %d, want 9223372036854775807", types.Int64)
	}
	if types.Uint8 != 255 {
		t.Errorf("Uint8 = %d, want 255", types.Uint8)
	}
	if types.Uint16 != 65535 {
		t.Errorf("Uint16 = %d, want 65535", types.Uint16)
	}
	if types.Uint32 != 4294967295 {
		t.Errorf("Uint32 = %d, want 4294967295", types.Uint32)
	}
	if types.Uint64 != 18446744073709551615 {
		t.Errorf("Uint64 = %d, want 18446744073709551615", types.Uint64)
	}
}

func TestIntegerMinValues(t *testing.T) {
	if got := types.Int8 + 1; got != -128 {
		t.Errorf("int8 minimum = %d, want -128", got)
	}
	if got := types.Int16 + 1; got != -32768 {
		t.Errorf("int16 minimum = %d, want -32768", got)
	}
	if got := types.Int32 + 1; got != -2147483648 {
		t.Errorf("int32 minimum = %d, want -2147483648", got)
	}
	if got := types.Int64 + 1; got != -9223372036854775808 {
		t.Errorf("int64 minimum = %d, want -9223372036854775808", got)
	}
	if got := types.Uint8 + 1; got != 0 {
		t.Errorf("uint8 minimum = %d, want 0", got)
	}
	if got := types.Uint16 + 1; got != 0 {
		t.Errorf("uint16 minimum = %d, want 0", got)
	}
	if got := types.Uint32 + 1; got != 0 {
		t.Errorf("uint32 minimum = %d, want 0", got)
	}
	if got := types.Uint64 + 1; got != 0 {
		t.Errorf("uint64 minimum = %d, want 0", got)
	}
}
