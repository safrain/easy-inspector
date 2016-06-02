package me.safrain.validator

import org.junit.Test

class ViolationTest extends BaseTest {

    static class MockExceptionRule {
        public boolean mockException(Object obj) {
            throw new RuntimeException()
        }
    }

    @Test
    void violationExpression() {
        inspector.validate([
                a: ''
        ], {
            V.STRING.notEmpty('b #test')
        }).with {
            assert it.size() == 1
            assert it[0].expression.expression == 'b #test'
        }
    }

    @Test
    void violationTypeRejected() {
        inspector.validate([
                a: [],
                b: [1, 2]
        ]) {
            V.STRING.isString('a/b') // Property
            V.STRING.isString('a/*') // EveryProperty
            V.STRING.isString('a/?') // AnyProperty
            V.STRING.isString('[0]') // ArrayIndexAccess
            V.STRING.isString('[?]') // AnyArrayElement
            V.STRING.isString('[*]') // EvertArrayElement
            V.STRING.isString('[1..3]') // ArrayRangeAccess
            V.STRING.isString('c') // Property not found
            V.STRING.isString('b[5]') // Array index not found
            V.STRING.isString('b[1..3]') // Range out of bound
        }.with {
            assert it.size() == 10
            it.each {
                assert it.type == Violation.Type.SEGMENT_REJECTED
                assert !it.throwable
            }
        }
    }

    @Test
    void violationTypeInvalid() {
        inspector.validate([
                a: null
        ]) {
            V.COMMON.notNull('a')
        }.with {
            assert it.size() == 1
            assert it[0].type == Violation.Type.INVALID
            assert it[0].object == null
            assert !it[0].throwable
        }
    }

    @Test
    void violationTypeException() {
        inspector.validate([
                a: null
        ]) {
            def v = inspector.proxy(MockExceptionRule) as MockExceptionRule
            v.mockException('a')
        }.with {
            assert it.size() == 1
            assert it[0].type == Violation.Type.EXCEPTION
            assert it[0].object == null
            assert it[0].throwable
        }
    }

    @Test
    void nestedIterativeViolation() {
        inspector.validate([
                a: [
                        a: [1, '2', '3']
                ],
                b: [
                        a: [1, '2', '3']
                ]
        ]) {
            V.NUMBER.isInteger('*/a/[*]')
        }.with {
            // No more than 1 violations because of suppress mode
            assert it.size() == 1
            assert it[0].type == Violation.Type.INVALID
        }
    }

    @Test
    void iterativeViolation() {
        inspector.validate([
                [a: 1],
                [a: '2'],
                [a: 3],
        ]) {
            V.NUMBER.isInteger '[*]/a'
        }.with {
            assert it.size() == 1
            assert it[0].type == Violation.Type.INVALID
        }
    }

    @Test
    void anyPropertyNestedViolation() {
        inspector.validate([
                a: '1',
                b: [
                        b1: '1',
                        b2: '2'
                ],
                c: '3',
        ]) {
            V.NUMBER.isInteger('/?/?')
        }.with {
            assert it
        }
    }

    @Test
    void allPropertyViolation() {
        inspector.validate([
                a: '1',
                b: null,
                c: '3',
        ]) {
            V.STRING.isString '*'
            V.STRING.isString '?*'
        }.with {
            assert it.size() == 2
            assert it[0].expression.expression == '*'
            assert it[1].expression.expression == '?*'
        }
    }

    @Test
    void anyPropertyViolation() {
        inspector.validate([
                a: '1',
                b: '2',
                c: '3',
        ]) {
            V.NUMBER.isInteger('/?')
        }.with {
            assert it
        }

        inspector.validate([
                :
        ]) {
            V.NUMBER.isInteger('/?')
        }.with {
            assert it
        }
    }

    @Test
    void manualViolation() {
        inspector.validate([a: null, b: '2'], {
            V.manual({
                if (!V.STRING.isString('a')) {
                    if (V.COMMON.notNull('b')) {
                        it << new Violation('error')
                    }
                }
            })
        }).with {
            assert it.size() == 1
        }
    }


}
