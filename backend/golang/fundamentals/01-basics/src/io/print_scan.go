package io

import "fmt"

// Package-level function variables act as seams: production uses the real
// fmt functions, tests swap them for spies.
var (
	Print  = fmt.Print
	Scan   = fmt.Scan
	Printf = fmt.Printf
)

func PrintScan() {
	var name string
	var age int
	Print("Enter your name and age: ")
	Scan(&name, &age)
	Printf("Hello, %s! You are %d years old.\n", name, age)
}
