package slice_compare_test

import (
	"cmp"
	"strconv"
	"testing"

	"github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/slice_compare"
	"github.com/TheJsharper/polyglot-workspace/01-basics/tests/testutil"
)

func TestSliceCompare(t *testing.T) {
	first, second := []int{1, 2, 3}, []int{1, 2, 4}

	t.Run("Equal", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_compare.EqualSlices(first, []int{1, 2, 3}), true)
		testutil.ExpectEqual(t, slice_compare.EqualSlices(first, second), false)
	})
	t.Run("EqualFunc", func(t *testing.T) {
		sameNumber := func(number int, text string) bool { return strconv.Itoa(number) == text }
		testutil.ExpectEqual(t, slice_compare.EqualSlicesFunc(first, []string{"1", "2", "3"}, sameNumber), true)
	})
	t.Run("Compare", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_compare.CompareSlices(first, second), -1)
		testutil.ExpectEqual(t, slice_compare.CompareSlices(second, first), 1)
		testutil.ExpectEqual(t, slice_compare.CompareSlices(first, first), 0)
	})
	t.Run("CompareFunc", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_compare.CompareSlicesFunc(first, second, cmp.Compare[int]), -1)
	})
}
