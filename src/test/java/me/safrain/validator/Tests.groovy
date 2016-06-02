package me.safrain.validator

import org.junit.Test

class Tests extends BaseTest {


    @Test
    void manualScope() {
        inspector.validate([
                a: null
        ], {
            V.manual {
                assert !V.STRING.notEmpty('a')
            }
        } as Validator).with {
            assert !it
        }
    }


    @Test
    void elementTypeViolation() {
        inspector.validate([1, 2, 3]) {
            V.NUMBER.isInteger '[0]'
            V.NUMBER.isInteger '[1]'
            V.NUMBER.isInteger '[2]'
        }.with {
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
            V.STRING.isString('a')
            V.STRING.notEmpty('/b')
            V.COMMON.isNull('/c')
            V.NUMBER.notZero('/%%')
        } as Validator).with {
            assert !it
        }
    }

    @Test
    void nullPath() {
        inspector.validate([a: 1], {
            V.COMMON.isNull('a/b')
        } as Validator).with {
            assert it.size() == 1
        }
    }






    @Test
    void manual() {
        inspector.validate([
                a: null,
                b: '2'
        ], {
            V.manual({
                assert !V.STRING.isString('a')
                if (V.STRING.isString('a')) {
                    assert V.STRING.notEmpty('b')
                }
            })
        }).with {
            assert !it
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
                V.STRING.notEmpty('/')
                V.COMMON.notNull('d')
            }
        }
    }

    @Test
    void comment() {
        inspector.validate([
                a: null,
                b: 1
        ]) {
            V.COMMON.notNull 'a #a is null'
            V.STRING.isString 'b #  b is not STRING'
        }.with {
            assert it.size() == 2
            assert it[0].expression.comment == 'a is null'
            assert it[1].expression.comment == '  b is not STRING'
        }
    }

    @Test
    void everyProperty() {
        inspector.validate([
                a: '1',
                b: '2',
                c: '3',
        ]) {
            V.STRING.isString '*'
        }.with {
            assert !it
        }
    }

    @Test
    void everyPropertyEmpty() {
        inspector.validate([
                :
        ]) {
            V.STRING.isString '*'
            V.STRING.isString '*/*'
        }.with {
            assert !it
        }
    }

    @Test
    void anyProperty() {
        inspector.validate([
                a: '1',
                b: 2,
                c: '3',
        ]) {
            V.NUMBER.isInteger('/?')
        }.with {
            assert !it
        }
    }


    @Test
    void anyPropertyNested() {
        inspector.validate([
                a: '1',
                b: [
                        b1: 1,
                        b2: 2
                ],
                c: '3',
        ]) {
            V.NUMBER.isInteger('/?/?')
        }.with {
            assert !it
        }
    }

    @Test
    void iterativePropertyMix() {
        inspector.validate([
                a: [
                        b1: 0,
                        b2: ' '
                ],
                b: [
                        b3: 0,
                        b4: ' '
                ],
                c: [
                        b5: 1,
                        b6: 1,
                ],
        ]) {
            V.NUMBER.isInteger('/*/?')
            V.NUMBER.isInteger('/?/*')
        }.with {
            assert !it
        }
    }

    @Test
    void arrayAccess() {
        inspector.validate([
                a: ['1', '2', '3'],
                b: [],
                c: [1],
        ]) {
            V.STRING.notEmpty('a[0]')
//            V.STRING.notEmpty('a[1]')
//            V.STRING.notEmpty('a[2]')
//            V.STRING.notEmpty('a/[2]')
//            V.COMMON.isEquals('a[-1]', '3')
//            V.COMMON.isEquals('a[-2]', '2')
//            V.COMMON.isEquals('c[-1]', 1)
        }.with {
            assert !it
        }
    }



    @Test
    void eachElement() {
        inspector.validate([1, 2, 3]) {
            V.NUMBER.isInteger '[*]'
        }.with {
            assert !it
        }
    }



    @Test
    void emptyArray() {
        inspector.validate([]) {
            V.NUMBER.isInteger '[*]'
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
            V.NUMBER.isInteger '[*]/a'
        }.with {
            assert it.size() == 0
        }
    }

    @Test
    void arrayEachSomeWrongType() {
        inspector.validate([1, 2, '3']) {
            V.NUMBER.isInteger '[*]'
        }.with {
            assert it.size() == 1
        }
    }

    @Test
    void arrayRange() {
        inspector.validate([1, 2, 3, 4, 5, 6]) {
            V.NUMBER.inRange('[2..5]', 3, 6)
        }.with {
            assert !it
        }
        inspector.validate([1, 2, 3, 4, 5, 6]) {
            V.NUMBER.inRange('[2..-1]', 3, 6)
        }.with {
            assert !it
        }
        inspector.validate([1, 2, 3, 4, 5, 6]) {
            V.NUMBER.inRange('[2..-1]', 5, 6)
        }.with {
            assert it.size() == 1
        }

    }

    @Test
    void arrayRangeSame() {
        inspector.validate([1, 2, 3, 4, 5, 6]) {
            V.COMMON.isEquals('[3..3]', 4)
        }.with {
            assert !it
        }
    }

    @Test
    void nestedIterative() {
        inspector.validate([
                a: [
                        a: [1, 2, 3]
                ],
                b: [
                        a: [1, 2, 3]
                ]
        ]) {
            V.NUMBER.isInteger('*/a/[*]')
        }.with {
            assert !it
        }
    }

}
