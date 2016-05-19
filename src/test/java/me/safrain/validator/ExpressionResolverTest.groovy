package me.safrain.validator

import me.safrain.validator.expression.resolver.ANTLRExpressionResolver
import me.safrain.validator.expression.segments.*
import org.junit.Before
import org.junit.Test

class ExpressionResolverTest {
    ANTLRExpressionResolver resolver

    @Before
    void setup() {
        resolver = new ANTLRExpressionResolver()
    }

    @Test
    void plain0() {
        def e = resolver.resolve('?a')
        println e
    }

    @Test
    void plain() {
        resolver.resolve('?/s0/s1/s2').with {
            assert it.optional
            assert it.segments[0] instanceof PropertySegment
            assert it.segments[1] instanceof PropertySegment
            assert it.segments[2] instanceof PropertySegment

            [
                    's0',
                    's1',
                    's2'
            ].eachWithIndex { o, i ->
                it.segments[i].propertyName == o
            }
        }

        resolver.resolve('/s0/s1/s2').with {
            assert !it.optional
            [
                    's0',
                    's1',
                    's2'
            ].eachWithIndex { o, i ->
                it.segments[i].propertyName == o
            }
        }
    }

    @Test
    void comment() {
        resolver.resolve('?/s0/s1/s1   #comment').with {
            assert it.segments.size() == 3
            assert it.optional
            assert it.comment == 'comment'
        }
    }


    @Test
    void escape() {
        resolver.resolve('s%%0/s%/1/% 3/%%%%').with {
            assert it.segments.every { it instanceof PropertySegment }
            [
                    's%0',
                    's/1',
                    ' 3',
                    '%%'
            ].eachWithIndex { o, i ->
                it.segments[i].propertyName == o
            }
        }
    }

    @Test
    void allProperty() {
        resolver.resolve('/s1/s2/*').with {
            assert it.segments.size() == 3
            assert it.segments[0] instanceof PropertySegment
            assert it.segments[1] instanceof PropertySegment
            assert it.segments[2] instanceof AnyPropertySegment
        }
    }

    @Test
    void allPropertySegment() {
        '/s1/s2[0] array'
        '/[0] array'
        '[0] array'
        '/s1/s2[0, 3] array range'
        '/[0, 3] array range'
        '[0, 3] array range'
        '/s1/s2[*] every element'
        '/s1/s2[?] any element'
        '/s1/? any property'
        '/s1/* every property'


        resolver.resolve('s1/s2/*').with {
            assert it.segments[0..1].every { it instanceof PropertySegment }
            assert it.segments[2] instanceof AnyPropertySegment
        }
    }

    @Test
    void testArrayIndexSegment() {

        resolver.resolve('a[0]').with {
            assert it.segments.size() == 2
            assert it.segments[0] instanceof PropertySegment
            assert it.segments[1] instanceof ArrayIndexAccessSegment
        }
    }

    @Test
    void allArrayElementSegment() {

        resolver.resolve('a[*]').with {
            assert it.segments.size() == 2
            assert it.segments[0] instanceof PropertySegment
            assert it.segments[1] instanceof EveryArrayElementSegment
        }
    }

    @Test
    void arrayRange() {
        resolver.resolve('a[0..1]').with {
            assert it.segments.size() == 2
            assert it.segments[0] instanceof PropertySegment
            assert it.segments[1] instanceof ArrayRangeAccessSegment
        }

    }

    @Test
    void arrayRangeReverse() {
        resolver.resolve('a[9..-1]').with {
            assert it.segments.size() == 2
            assert it.segments[0] instanceof PropertySegment
            assert it.segments[1] instanceof ArrayRangeAccessSegment
        }

        resolver.resolve('a/b[-1..0]').with {
            assert it.segments.size() == 3
            assert it.segments[0] instanceof PropertySegment
            assert it.segments[1] instanceof PropertySegment
            assert it.segments[2] instanceof ArrayRangeAccessSegment

        }
    }


}

