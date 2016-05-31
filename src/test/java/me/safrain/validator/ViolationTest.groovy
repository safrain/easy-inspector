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
            V.string.notEmpty('b #test')
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
            V.string.isString('a/b') // Property
            V.string.isString('a/*') // EveryProperty
            V.string.isString('a/?') // AnyProperty
            V.string.isString('[0]') // ArrayIndexAccess
            V.string.isString('[?]') // AnyArrayElement
            V.string.isString('[*]') // EvertArrayElement
            V.string.isString('[1..3]') // ArrayRangeAccess
            V.string.isString('c') // Property not found
            V.string.isString('b[5]') // Array index not found
            V.string.isString('b[1..3]') // Range out of bound
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
            V.common.notNull('a')
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
            V.number.isInteger('*/a/[*]')
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
            V.number.isInteger '[*]/a'
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
            V.number.isInteger('/?/?')
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
            V.string.isString '*'
            V.string.isString '?*'
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
            V.number.isInteger('/?')
        }.with {
            assert it
        }

        inspector.validate([
                :
        ]) {
            V.number.isInteger('/?')
        }.with {
            assert it
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


}
