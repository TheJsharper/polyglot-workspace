package variables

var Text string = "John Doe"

var Number int = 42

var Float float64 = 3.14

var Boolean bool = true

var Complex complex128 = complex(1, 2)

var (
	// Uninitialized variables
	UninitializedString  string
	UninitializedInt     int
	UninitializedFloat   float64
	UninitializedBool    bool
	UninitializedComplex complex128
)

var (
	// Initialized variables
	InitializedString  string     = "Hello, World!"
	InitializedInt     int        = 100
	InitializedFloat   float64    = 2.718
	InitializedBool    bool       = false
	InitializedComplex complex128 = complex(3, 4)
)
