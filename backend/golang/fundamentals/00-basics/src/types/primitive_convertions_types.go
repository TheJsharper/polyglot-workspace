package types

var value float32 = 3.14

var value_1 int = 3

var Result float32 = float32(value_1)

var value_2 float64 = 3.141592653589793

var Result_1 float64 = float64(value_1)

var CelsiusValue int = 25

var nineFive float32 = 9.0 / 5

var FahrenheitValue float32 = (float32(CelsiusValue)*nineFive + float32(32))

var KelvinValue float32 = (float32(CelsiusValue) + float32(273.15))
