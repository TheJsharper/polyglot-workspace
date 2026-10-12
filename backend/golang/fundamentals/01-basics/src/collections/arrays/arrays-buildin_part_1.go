package arrays

var MyArray []int = []int{1, 2, 3, 4, 5}

var EmptyArrayBuiltin []int = []int{}

var IsNilArray bool = ([]int(nil)) == nil

var IsNilArrayExplicit bool = ([]int(nil)) == nil

var MyArrayLen int = len(MyArray)

var EmptyArrayBuiltinLen int = len(EmptyArrayBuiltin)

var NilArrayLen int = len([]int(nil))

var MyArrayAppend []int = append(MyArray, 6)

var EmptyArrayBuiltinAppend []int = append(EmptyArrayBuiltin, 6)

var NilArrayAppend []int = append([]int(nil), 6)

var MyArrayCap int = cap(MyArray)

var EmptyArrayBuiltinCap int = cap(EmptyArrayBuiltin)

var NilArrayCap int = cap([]int(nil))

var IsCapEqualLen bool = MyArrayCap == MyArrayLen

var IsEmptyArrayBuiltinCapEqualLen bool = EmptyArrayBuiltinCap == EmptyArrayBuiltinLen

var IsNilArrayCapEqualLen bool = NilArrayCap == NilArrayLen
