package slice_edit_test

import (
	"strings"
	"testing"

	slicedata "github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/slice_data"
	"github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/slice_edit"
	"github.com/TheJsharper/polyglot-workspace/01-basics/tests/testutil"
)

func TestSliceEdit(t *testing.T) {
	t.Run("Insert", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_edit.InsertValues([]int{1, 4}, 1, 2, 3), []int{1, 2, 3, 4})
	})
	t.Run("Delete", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_edit.DeleteRange([]int{1, 2, 3, 4}, 1, 3), []int{1, 4})
	})
	t.Run("DeleteFunc", func(t *testing.T) {
		isEven := func(value int) bool { return value%2 == 0 }
		testutil.ExpectEqual(t, slice_edit.DeleteMatching([]int{1, 2, 3, 4}, isEven), []int{1, 3})
	})
	t.Run("Replace", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_edit.ReplaceRange([]int{1, 2, 3}, 0, 2, 9), []int{9, 3})
	})
	t.Run("Compact", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_edit.CompactValues([]int{1, 1, 2, 2, 2, 3}), []int{1, 2, 3})
	})
	t.Run("CompactFunc", func(t *testing.T) {
		got := slice_edit.CompactValuesFunc([]string{"a", "A", "b"}, strings.EqualFold)
		testutil.ExpectEqual(t, got, []string{"a", "b"})
	})
	t.Run("Concat", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_edit.ConcatSlices([]int{1}, []int{2, 3}, []int{4}), []int{1, 2, 3, 4})
	})
	t.Run("Repeat", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_edit.RepeatSlice([]int{1, 2}, 2), []int{1, 2, 1, 2})
	})
	t.Run("Reverse", func(t *testing.T) {
		numbers := slice_edit.CloneSlice(slicedata.Numbers)
		slice_edit.ReverseValues(numbers)
		testutil.ExpectEqual(t, numbers, []int{2, 1, 3})
	})
	t.Run("Clone", func(t *testing.T) {
		numbers := slice_edit.CloneSlice(slicedata.Numbers)
		numbers[0] = 99
		testutil.ExpectEqual(t, slicedata.Numbers[0], 3) // original untouched
	})
	t.Run("Clip", func(t *testing.T) {
		numbers := make([]int, 2, 10)
		testutil.ExpectEqual(t, cap(slice_edit.ClipSlice(numbers)), 2)
	})
	t.Run("Grow", func(t *testing.T) {
		grown := slice_edit.GrowSlice([]int{1}, 10)
		testutil.ExpectEqual(t, len(grown), 1)
		testutil.ExpectEqual(t, cap(grown) >= 11, true)
	})
}
