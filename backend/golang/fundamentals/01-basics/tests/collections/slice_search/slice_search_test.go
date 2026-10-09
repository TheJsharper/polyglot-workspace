package slice_search_test

import (
	"cmp"
	"strings"
	"testing"

	slicedata "github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/slice_data"
	"github.com/TheJsharper/polyglot-workspace/01-basics/src/collections/slice_search"
	"github.com/TheJsharper/polyglot-workspace/01-basics/tests/testutil"
)

func TestSliceSearch(t *testing.T) {
	sortedNumbers := []int{1, 2, 3, 5}

	t.Run("BinarySearch", func(t *testing.T) {
		index, found := slice_search.BinarySearchValue(sortedNumbers, 3)
		testutil.ExpectEqual(t, []any{index, found}, []any{2, true})
		index, found = slice_search.BinarySearchValue(sortedNumbers, 4) // missing: insertion point
		testutil.ExpectEqual(t, []any{index, found}, []any{3, false})
	})
	t.Run("BinarySearchFunc", func(t *testing.T) {
		index, found := slice_search.BinarySearchValueFunc(sortedNumbers, 5, cmp.Compare[int])
		testutil.ExpectEqual(t, []any{index, found}, []any{3, true})
	})
	t.Run("Index", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_search.IndexOfValue(slicedata.Numbers, 2), 2)
	})
	t.Run("IndexFunc", func(t *testing.T) {
		lessThanThree := func(value int) bool { return value < 3 }
		testutil.ExpectEqual(t, slice_search.IndexOfFunc(slicedata.Numbers, lessThanThree), 1)
	})
	t.Run("Contains", func(t *testing.T) {
		testutil.ExpectEqual(t, slice_search.ContainsValue(slicedata.Words, "apple"), true)
	})
	t.Run("ContainsFunc", func(t *testing.T) {
		startsWithCh := func(word string) bool { return strings.HasPrefix(word, "ch") }
		testutil.ExpectEqual(t, slice_search.ContainsMatch(slicedata.Words, startsWithCh), true)
	})
}
