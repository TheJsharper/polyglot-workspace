package slice_iterators_test

import (
	"slices"
	"testing"

	slicedata "github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/slice_data"
	"github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/slice_iterators"
	"github.com/TheJsharper/polyglot-workspace/01-basics/tests/testutil"
)

func TestSliceIterators(t *testing.T) {
	numbers := slices.Clone(slicedata.Numbers) // [3 1 2]

	t.Run("All", func(t *testing.T) {
		valuesByIndex := map[int]int{}
		for index, value := range slice_iterators.AllIndexValues(numbers) {
			valuesByIndex[index] = value
		}
		testutil.ExpectEqual(t, valuesByIndex, map[int]int{0: 3, 1: 1, 2: 2})
	})
	t.Run("Values", func(t *testing.T) {
		testutil.ExpectEqual(t, slices.Collect(slice_iterators.ValuesOf(numbers)), []int{3, 1, 2})
	})
	t.Run("Backward", func(t *testing.T) {
		var reversed []int
		for _, value := range slice_iterators.BackwardValues(numbers) {
			reversed = append(reversed, value)
		}
		testutil.ExpectEqual(t, reversed, []int{2, 1, 3})
	})
	t.Run("Chunk", func(t *testing.T) {
		var chunks [][]int
		for chunk := range slice_iterators.ChunkSlice([]int{1, 2, 3, 4, 5}, 2) {
			chunks = append(chunks, chunk)
		}
		testutil.ExpectEqual(t, chunks, [][]int{{1, 2}, {3, 4}, {5}})
	})
	t.Run("Collect", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_iterators.CollectValues(slices.Values(numbers)), numbers)
	})
	t.Run("AppendSeq", func(t *testing.T) {
		got := slice_iterators.AppendValues([]int{9}, slices.Values(numbers))
		testutil.ExpectEqual(t, got, []int{9, 3, 1, 2})
	})
	t.Run("Sorted", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_iterators.SortedValues(slices.Values(numbers)), []int{1, 2, 3})
	})
	t.Run("SortedFunc", func(t *testing.T) {
		descending := func(first, second int) int { return second - first }
		testutil.ExpectEqual(t, slice_iterators.SortedValuesFunc(slices.Values(numbers), descending), []int{3, 2, 1})
	})
	t.Run("SortedStableFunc", func(t *testing.T) {
		byLength := func(first, second string) int { return len(first) - len(second) }
		got := slice_iterators.SortedValuesStableFunc(slices.Values(slicedata.Words), byLength)
		testutil.ExpectEqual(t, got, []string{"apple", "banana", "cherry"})
	})
}
