package slice_edit

import "slices"

func InsertValues(numbers []int, index int, values ...int) []int {
	return slices.Insert(numbers, index, values...)
}

func DeleteRange(numbers []int, start, end int) []int { return slices.Delete(numbers, start, end) }

func DeleteMatching(numbers []int, remove func(int) bool) []int {
	return slices.DeleteFunc(numbers, remove)
}

func ReplaceRange(numbers []int, start, end int, values ...int) []int {
	return slices.Replace(numbers, start, end, values...)
}

func CompactValues(numbers []int) []int { return slices.Compact(numbers) }

func CompactValuesFunc(words []string, equal func(string, string) bool) []string {
	return slices.CompactFunc(words, equal)
}

func ConcatSlices(parts ...[]int) []int { return slices.Concat(parts...) }

func RepeatSlice(numbers []int, count int) []int { return slices.Repeat(numbers, count) }

func ReverseValues(numbers []int) { slices.Reverse(numbers) }

func CloneSlice(numbers []int) []int { return slices.Clone(numbers) }

func ClipSlice(numbers []int) []int { return slices.Clip(numbers) }

func GrowSlice(numbers []int, extra int) []int { return slices.Grow(numbers, extra) }
