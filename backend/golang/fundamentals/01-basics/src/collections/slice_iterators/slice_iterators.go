package slice_iterators

import (
	"iter"
	"slices"
)

func AllIndexValues(numbers []int) iter.Seq2[int, int] { return slices.All(numbers) }

func ValuesOf(numbers []int) iter.Seq[int] { return slices.Values(numbers) }

func BackwardValues(numbers []int) iter.Seq2[int, int] { return slices.Backward(numbers) }

func ChunkSlice(numbers []int, size int) iter.Seq[[]int] { return slices.Chunk(numbers, size) }

func CollectValues(values iter.Seq[int]) []int { return slices.Collect(values) }

func AppendValues(numbers []int, values iter.Seq[int]) []int {
	return slices.AppendSeq(numbers, values)
}

func SortedValues(values iter.Seq[int]) []int { return slices.Sorted(values) }

func SortedValuesFunc(values iter.Seq[int], compare func(int, int) int) []int {
	return slices.SortedFunc(values, compare)
}

func SortedValuesStableFunc(values iter.Seq[string], compare func(string, string) int) []string {
	return slices.SortedStableFunc(values, compare)
}
