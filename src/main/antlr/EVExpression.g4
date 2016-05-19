grammar EVExpression;
@header {
package antlr.generated;
}

expression: optionalTag? '/'? (unit ('/' unit)*)? (' '+ '#' comment)? EOF;

unit: (
    propertyAccess |
    arrayAccess |
    propertyEveryAccess |
    propertyAnyAccess
);

arrayAccess: propertyAccess? '[' SPACE* (
    arrayRangeAccess |
    arrayIndexAccess |
    arrayEveryAccess |
    arrayAnyAccess
) SPACE* ']';

arrayRangeAccess: arrayIndex SPACE* DOT DOT SPACE* arrayIndex;
arrayIndexAccess: arrayIndex;
arrayEveryAccess: '*';
arrayAnyAccess: '?';
arrayIndex: NEGATIVE? NUMBER+;

optionalTag : '?';
comment:  .*;
propertyAccess: (PROPERTY_CHAR | NEGATIVE | NUMBER)+;
propertyEveryAccess: '*';
propertyAnyAccess: '?';

PROPERTY_CHAR:
    '%' ('/' | ' ' | '%' | '[' | ']') |
    ~('/' | ' ' | '%' | '[' | ']' | '0'..'9' | '-' | '.');

NEGATIVE: '-';
NUMBER: '0'..'9';
DOT: '.';
SPACE: ' ';