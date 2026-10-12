package arrays

var MyMadeArray []int = make([]int, 5)

var MyMadeArrayLen int = len(MyMadeArray)

var MyMadeArrayCap int = cap(MyMadeArray)

var IsMyMadeArrayCapEqualLen bool = MyMadeArrayCap == MyMadeArrayLen

var MyMadeArrayAppend []int = append(MyMadeArray, 6)

var MyMadeArrayAppendLen int = len(MyMadeArrayAppend)

var MyMadeArrayAppendCap int = cap(MyMadeArrayAppend)

var IsMyMadeArrayAppendCapEqualLen bool = MyMadeArrayAppendCap == MyMadeArrayAppendLen

var MyMadeArrayFull []int = append(MyMadeArray, 6, 7, 8, 9, 10)

var MyMadeArrayFullLen int = len(MyMadeArrayFull)

var MyMadeArrayFullCap int = cap(MyMadeArrayFull)

var IsMyMadeArrayFullCapEqualLen bool = MyMadeArrayFullCap == MyMadeArrayFullLen

var MyMadeArrayFulledCleared []int = func() []int {
	clear(MyMadeArrayFull)
	return MyMadeArrayFull
}()

var MyMadeArrayFulledClearedLen int = len(MyMadeArrayFulledCleared)

var MyMadeArrayFulledClearedCap int = cap(MyMadeArrayFulledCleared)

var IsMyMadeArrayFulledClearedCapEqualLen bool = MyMadeArrayFulledClearedCap == MyMadeArrayFulledClearedLen
