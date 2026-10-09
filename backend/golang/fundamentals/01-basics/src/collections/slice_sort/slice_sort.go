package slice_sort

import "slices"

func SortValues(numbers []int) { slices.Sort(numbers) }

func SortValuesFunc(numbers []int, compare func(int, int) int) { slices.SortFunc(numbers, compare) }

func SortValuesStableFunc(words []string, compare func(string, string) int) {
	slices.SortStableFunc(words, compare)
}

func IsSortedValues(numbers []int) bool { return slices.IsSorted(numbers) }

func IsSortedValuesFunc(words []string, compare func(string, string) int) bool {
	return slices.IsSortedFunc(words, compare)
}

func MinValue(numbers []int) int { return slices.Min(numbers) }

func MinValueFunc(words []string, compare func(string, string) int) string {
	return slices.MinFunc(words, compare)
}

func MaxValue(numbers []int) int { return slices.Max(numbers) }

func MaxValueFunc(words []string, compare func(string, string) int) string {
	return slices.MaxFunc(words, compare)
}
