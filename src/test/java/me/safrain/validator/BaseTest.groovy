package me.safrain.validator

import org.junit.Before
import org.junit.Test

class BaseTest {
    EasyInspector inspector

    @Before
    void setup() {
        inspector = new EasyInspector()
        V.common = inspector.proxy(V.CommonRules)
        V.string = inspector.proxy(V.StringRules)
        V.number = inspector.proxy(V.NumberRules)
    }

    @Test
    void manualScope() {
        inspector.validate([
                a: null
        ], {
            V.manual {
                assert !V.string.notEmpty('a')
            }
        } as Validator).with {
            assert !it
        }
    }

    @Test
    void oneLevel() {
        inspector.validate([
                a  : '',
                b  : '1',
                c  : null,
                '%': 1
        ], {
            V.string.isString('a')
            V.string.notEmpty('/b')
            V.common.isNull('//c')
            V.number.notZero('/%%')
        } as Validator).with {
            assert !it
        }
    }

    @Test
    void nullPath() {
        inspector.validate([a: 1], {
            V.common.isNull('a/b')
        } as Validator).with {
            assert it.size() == 1
        }
    }


    @Test
    void expressionInViolation() {
        inspector.validate([
                a: ''
        ], {
            V.string.isString('a')
            V.string.notEmpty('b')
        }).with {
            assert it.size() == 1
            assert it[0].expression.expression == 'b'
        }
    }

    @Test
    void wrongType() {
        inspector.validate([
                a: '1'
        ]) {
            V.string.isString '?a/*'
        }.with {
            assert !it
        }
    }

    @Test
    void optionalMultiLevel() {
        inspector.validate([
                a: '1'
        ]) {
            V.string.isString '?a/b/*'
        }.with {
            assert !it
        }
    }

    @Test
    void optionalOneLevel() {
        inspector.validate([
                a: ''
        ], {
            V.string.isString('a')
            V.string.notEmpty('?b')
        }).with {
            assert !it
        }
    }

    @Test
    void manual() {
        inspector.validate([
                a: null,
                b: '2'
        ], {
            V.manual({
                assert !V.string.isString('a')
                if (V.string.isString('a')) {
                    assert V.string.notEmpty('b')
                }
            })
        }).with {
            assert !it
        }
    }

    @Test
    void manualViolation() {
        inspector.validate([a: null, b: '2'], {
            V.manual({
                if (!V.string.isString('a')) {
                    if (V.common.notNull('b')) {
                        it << new Violation('error')
                    }
                }
            })
        }).with {
            assert it.size() == 1
        }
    }

    @Test
    void scope() {
        inspector.validate([
                a: null,
                b: [
                        c: '1',
                        d: []
                ]]) {
            V.scope('b/c') {
                V.string.notEmpty('/')
                V.common.notNull('d')
            }
        }
    }

    @Test
    void comment() {
        inspector.validate([
                a: null,
                b: 1
        ]) {
            V.common.notNull 'a #a is null'
            V.string.isString 'b #  b is not string'
        }.with {
            assert it.size() == 2
            assert it[0].expression.comment == 'a is null'
            assert it[1].expression.comment == '  b is not string'
        }
    }

    @Test
    void allProperty() {
        // Any property
        inspector.validate([
                a: '1',
                b: '2',
                c: '3',
        ]) {
            V.string.isString '*'
        }.with {
            assert !it
        }
    }

    @Test
    void allPropertyViolation() {
        inspector.validate([
                a: '1',
                b: null,
                c: '3',
        ]) {
            V.string.isString '*'
            V.string.isString '?*'
        }.with {
            assert it.size() == 2
            assert it[0].expression.expression == '*'
            assert it[1].expression.expression == '?*'
        }
    }

    @Test
    void arrayAccess() {
        inspector.validate([
                a: ['1', '2', '3'],
                b: [],
                c: [1],
        ]) {
            V.string.notEmpty('a[0]')
            V.string.notEmpty('a[1]')
            V.string.notEmpty('a[2]')
            V.string.notEmpty('a/[2]')
            V.common.isEquals('a[-1]', '3')
            V.common.isEquals('a[-2]', '2')
            V.common.isEquals('c[-1]', 1)
        }.with {
            assert !it
        }
    }

    @Test
    void arrayRangeViolation() {
        inspector.validate([
                a: ['1', '2', '3'],
                b: [],
                c: [1],
        ]) {
            V.string.notEmpty('b[0]')
            V.string.notEmpty('b/[0]')
            V.string.notEmpty('a/[-4]')
        }.with {
            assert it.size() == 3
        }
    }

    @Test
    void elementTypeViolation() {
        inspector.validate([1, 2, 3]) {
            V.number.isInteger '[0]'
            V.number.isInteger '[1]'
            V.number.isInteger '[2]'
        }.with {
            assert !it
        }
    }

    @Test
    void arrayViolationOptional() {
        inspector.validate([1, 2, 3]) {
            V.number.isInteger '?[5]'
            V.number.isInteger '?[1]/t'
            V.number.isInteger '?[5]/t'
        }.with {
            assert !it
        }
    }

    @Test
    void eachElement() {
        inspector.validate([1, 2, 3]) {
            V.number.isInteger '[*]'
        }.with {
            assert !it
        }
    }

    @Test
    void emptyArray() {
        inspector.validate([]) {
            V.number.isInteger '[*]'
        }.with {
            assert it.size() == 0
        }
    }

    @Test
    void propertyOverArray() {
        inspector.validate([
                [a: 1],
                [a: 1],
                [a: 1]
        ]) {
            V.number.isInteger '[*]/a'
        }.with {
            assert it.size() == 0
        }
    }

    @Test
    void arrayEachSomeWrongType() {
        inspector.validate([1, 2, '3']) {
            V.number.isInteger '[*]'
        }.with {
            assert it.size() == 1
        }
    }
}

