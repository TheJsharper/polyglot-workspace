package slice_sort_test

import (
	"testing"

	slicedata "github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/slice_data"
	"github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/slice_edit"
	"github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/slice_sort"
	"github.com/TheJsharper/polyglot-workspace/01-basics/tests/testutil"
)

func TestSliceSortMinMax(t *testing.T) {
	byLength := func(first, second string) int { return len(first) - len(second) }

	t.Run("Sort", func(t *testing.T) {
		numbers := slice_edit.CloneSlice(slicedata.Numbers)
		slice_sort.SortValues(numbers)
		testutil.ExpectEqual(t, numbers, []int{1, 2, 3})
	})
	t.Run("SortFunc", func(t *testing.T) {
		numbers := slice_edit.CloneSlice(slicedata.Numbers)
		slice_sort.SortValuesFunc(numbers, func(first, second int) int { return second - first })
		testutil.ExpectEqual(t, numbers, []int{3, 2, 1})
	})
	t.Run("SortStableFunc", func(t *testing.T) {
		words := []string{"bb", "a", "cc", "d"}
		slice_sort.SortValuesStableFunc(words, byLength)
		testutil.ExpectEqual(t, words, []string{"a", "d", "bb", "cc"}) // equal lengths keep original order
	})
	t.Run("IsSorted", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_sort.IsSortedValues([]int{1, 2, 3}), true)
		testutil.ExpectEqual(t, slice_sort.IsSortedValues(slicedata.Numbers), false)
	})
	t.Run("IsSortedFunc", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_sort.IsSortedValuesFunc([]string{"a", "bb", "ccc"}, byLength), true)
	})
	t.Run("Min", func(t *testing.T) { testutil.ExpectEqual(t, slice_sort.MinValue(slicedata.Numbers), 1) })
	t.Run("MinFunc", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_sort.MinValueFunc(slicedata.Words, byLength), "apple")
	})
	t.Run("Max", func(t *testing.T) { testutil.ExpectEqual(t, slice_sort.MaxValue(slicedata.Numbers), 3) })
	t.Run("MaxFunc", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_sort.MaxValueFunc(slicedata.Words, byLength), "banana")
	})
}
