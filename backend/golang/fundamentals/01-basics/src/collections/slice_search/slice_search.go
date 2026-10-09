package slice_search

import "slices"

func BinarySearchValue(sortedNumbers []int, target int) (int, bool) {
	return slices.BinarySearch(sortedNumbers, target)
}

func BinarySearchValueFunc(sortedNumbers []int, target int, compare func(int, int) int) (int, bool) {
	return slices.BinarySearchFunc(sortedNumbers, target, compare)
}

func IndexOfValue(numbers []int, target int) int { return slices.Index(numbers, target) }

func IndexOfFunc(numbers []int, match func(int) bool) int { return slices.IndexFunc(numbers, match) }

func ContainsValue(words []string, target string) bool { return slices.Contains(words, target) }

func ContainsMatch(words []string, match func(string) bool) bool {
	return slices.ContainsFunc(words, match)
}
