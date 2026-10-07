package types

/*
Signed integer types are used to represent whole numbers, both positive and negative. They allocate one bit for sign representation, which allows them to represent negative values. The signed integer types in Go include int, int8, int16, int32, and int64.
*/
var Integer int = 42

var Int8 int8 = 127

var Int16 int16 = 32767

var Int32 int32 = 2147483647

var Int64 int64 = 9223372036854775807

/*

Unsigned integer types are used to represent non-negative whole numbers. They have a larger positive range compared to their signed counterparts, as they do not allocate any bits for sign representation. The unsigned integer types in Go include uint, uint8, uint16, uint32, and uint64.

*/

var UnsignedInt uint = 42

var Uint8 uint8 = 255

var Uint16 uint16 = 65535

var Uint32 uint32 = 4294967295

var Uint64 uint64 = 18446744073709551615
