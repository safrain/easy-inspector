package me.safrain.validator

import org.junit.Test

class OptionalTest extends BaseTest {

    @Test
    void wrongTypeOptional() {
        inspector.validate([
                a: '1'
        ]) {
            V.string.isString '?a/*'
        }.with {
            assert it
        }
    }


    @Test
    void optionalMultiLevel() {
        inspector.validate([
                a: [:]
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
    void anyPropertyOptional() {
        inspector.validate([
                a: null,
        ]) {
            V.number.isInteger('?/a/?')
        }.with {
            assert !it
        }
    }


    @Test
    void arrayViolationOptional() {
        inspector.validate([1, 2, [:]]) {
            V.number.isInteger '?[5]'
            V.number.isInteger '?[2]/t'
        }.with {
            assert !it
        }
    }

}
