package slices_basics

// NilSlice is the zero value: nil, len 0, cap 0.
var NilSlice []int

// LiteralSlice has no length in its type, unlike an array.
var LiteralSlice = []int{1, 2, 3, 4, 5}

// MadeSlice is created with make(type, len, cap).
var MadeSlice = make([]int, 3, 5)
