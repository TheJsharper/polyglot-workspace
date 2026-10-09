package slice_compare

import "slices"

func EqualSlices(first, second []int) bool { return slices.Equal(first, second) }

func EqualSlicesFunc(numbers []int, texts []string, equal func(int, string) bool) bool {
	return slices.EqualFunc(numbers, texts, equal)
}

func CompareSlices(first, second []int) int { return slices.Compare(first, second) }

func CompareSlicesFunc(first, second []int, compare func(int, int) int) int {
	return slices.CompareFunc(first, second, compare)
}
