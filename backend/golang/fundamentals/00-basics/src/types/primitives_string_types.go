package types

import "strings"

var TextString string = "Hello, World!"

var ContainsHello bool = strings.Contains(TextString, "Hello")

var HasPrefix bool = strings.HasPrefix(TextString, "Hello")

var HasSuffix bool = strings.HasSuffix(TextString, "World!")

var IndexOfHello int = strings.Index(TextString, "Hello")

var LastIndexOfWorld int = strings.LastIndex(TextString, "World")

var CountOfL int = strings.Count(TextString, "l")

var SplitByComma []string = strings.Split(TextString, ",")

var JoinWithSpace string = strings.Join([]string{"Hello", "World"}, " ")

var FieldsOfString string = "   Trimmed String   "

var TrimmedString string = strings.TrimSpace(FieldsOfString)

var ToLowerCase string = strings.ToLower(TextString)

var ToUpperCase string = strings.ToUpper(TextString)

var TrimSpaces string = strings.TrimSpace("   Go is awesome!   ")

var ReplacedString string = strings.Replace(TextString, "World", "Gophers", 1)

var ReplaceAllString string = strings.ReplaceAll(TextString, "l", "L")

var RepeatedString string = strings.Repeat("Go! ", 3)

var SplitNString []string = strings.SplitN(TextString, " ", 2)

var SplitAfterString []string = strings.SplitAfter(TextString, ",")

var SplitAfterNString []string = strings.SplitAfterN(TextString, " ", 2)

var TrimLeftString string = strings.TrimLeft(TextString, "Hello")

var TrimRightString string = strings.TrimRight(TextString, "World!")

var TrimPrefixString string = strings.TrimPrefix(TextString, "Hello, ")

var TrimSuffixString string = strings.TrimSuffix(TextString, " World!")

var EqualFoldString bool = strings.EqualFold("GoLang", "golang")

var CompareString int = strings.Compare("Go", "GoLang")

var IndexByteString int = strings.IndexByte(TextString, 'o')

var LastIndexByteString int = strings.LastIndexByte(TextString, 'o')

var IndexRuneString int = strings.IndexRune(TextString, 'W')

var LastIndexRuneString int = strings.LastIndexFunc(TextString, func(r rune) bool {
	return r == 'o'
})

var MapStringToUpper string = strings.Map(func(r rune) rune {
	if r >= 'a' && r <= 'z' {
		return r - 32
	}
	return r
}, TextString)

var MapStringToLower string = strings.Map(func(r rune) rune {
	if r >= 'A' && r <= 'Z' {
		return r + 32
	}

	return r
}, TextString)

var TrimFuncString string = strings.TrimFunc(TextString, func(r rune) bool {
	return r == 'H' || r == '!'
})

var TrimLeftFuncString string = strings.TrimLeftFunc(TextString, func(r rune) bool {
	return r == 'H' || r == '!'
})

var TrimRightFuncString string = strings.TrimRightFunc(TextString, func(r rune) bool {
	return r == 'H' || r == '!'
})

var FieldsFuncString []string = strings.FieldsFunc(TextString, func(r rune) bool {
	return r == ' ' || r == ','
})

var SplitFuncString []string = strings.FieldsFunc(TextString, func(r rune) bool {
	return r == ' ' || r == ','
})
