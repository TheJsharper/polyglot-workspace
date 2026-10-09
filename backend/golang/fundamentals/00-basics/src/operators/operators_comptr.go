package operators

var Age int = 18

var Adult bool = Age >= 18

var Minor bool = Age < 18

var Senior bool = Age >= 65

var Child bool = Age < 13

var Teenager bool = Age >= 13 && Age < 18

var YoungAdult bool = Age >= 18 && Age < 30

var MiddleAged bool = Age >= 30 && Age < 65

var Elderly bool = Age >= 65

var NotAdult bool = Age < 18 || Age >= 65

var NotMinor bool = Age >= 18 || Age >= 65

var NotSenior bool = Age < 65 || Age < 18

var NotChild bool = Age >= 13 || Age >= 65
