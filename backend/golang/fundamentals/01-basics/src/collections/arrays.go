package collections

var ExampleArray = [3]int{1, 2, 3}

var ExampleArray2 = [3]int{4, 5, 6}

var ExampleArray3 = [3]int{7, 8, 9}

// 10 ways to access array elements (arrays only, no slices).

// 1. Access by constant index.
func AccessByIndex(a [3]int) int { return a[0] }

// 2. Access the last element using len.
func AccessLast(a [3]int) int { return a[len(a)-1] }

// 3. Access by variable index.
func AccessByVariable(a [3]int, i int) int { return a[i] }

// 4. Iterate with a classic for loop and sum.
func AccessForLoop(a [3]int) int {
	sum := 0
	for i := 0; i < len(a); i++ {
		sum += a[i]
	}
	return sum
}

// 5. Iterate with range (index and value) and sum.
func AccessRange(a [3]int) int {
	sum := 0
	for _, v := range a {
		sum += v
	}
	return sum
}

// 6. Range over the index only.
func AccessRangeIndexOnly(a [3]int) int {
	last := 0
	for i := range a {
		last = a[i]
	}
	return last
}

// 7. Access through a pointer to the array (auto-dereferenced).
func AccessViaPointer(a *[3]int) int { return a[1] }

// 8. Modify an element through a pointer.
func AccessAndModify(a *[3]int, i, v int) { a[i] = v }

// 9. Destructure into variables by index.
func AccessDestructure(a [3]int) (int, int, int) { return a[0], a[1], a[2] }

// 10. Access elements of a multidimensional array.
func AccessMatrix(m [2][3]int, row, col int) int { return m[row][col] }

// EmptyArray is the zero value: [0 0 0 0 0].
var EmptyArray [5]int

// DeclaredArray has an explicit length.
var DeclaredArray = [5]int{1, 2, 3, 4, 5}

// InferredArray lets the compiler count the elements with [...].
var InferredArray = [...]int{1, 2, 3, 4, 5}

// TwoD has static lengths for both dimensions.
var TwoD = [2][3]int{
	{1, 2, 3},
	{1, 2, 3},
}

// TwoDInferred lets the compiler count the outer length; the inner one must be explicit.
var TwoDInferred = [...][3]int{
	{1, 2, 3},
	{1, 2, 3},
}
