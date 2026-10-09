package io_taxes

import "fmt"

var (
	Print  = fmt.Print
	Scan   = fmt.Scan
	Printf = fmt.Printf
)

const TaxRate = 0.15

func PrintScanTaxes() {
	var name string
	var income float64
	Print("Enter your name and income: ")
	Scan(&name, &income)
	tax := income * TaxRate
	Printf("Hello, %s! Your tax is %.2f.\n", name, tax)
}
