package me.safrain.validator

import org.junit.Test

class OptionalTest extends BaseTest {

    @Test
    void wrongTypeOptional() {
        inspector.validate([
                a: '1'
        ]) {
            V.STRING.isString '?a/[*]'
        }.with {
            assert it
        }
    }


    @Test
    void optionalMultiLevel() {
        inspector.validate([
                a: [:]
        ]) {
            V.STRING.isString '?a/b/*'
        }.with {
            assert !it
        }
    }

    @Test
    void optionalOneLevel() {
        inspector.validate([
                a: ''
        ], {
            V.STRING.isString('a')
            V.STRING.notEmpty('?b')
        }).with {
            assert !it
        }
    }


    @Test
    void anyPropertyOptional() {
        inspector.validate([
                a: null,
        ]) {
            V.NUMBER.isInteger('?/a/?')
        }.with {
            assert !it
        }
    }


    @Test
    void arrayViolationOptional() {
        inspector.validate([1, 2, [:]]) {
            V.NUMBER.isInteger '?[5]'
            V.NUMBER.isInteger '?[2]/t'
        }.with {
            assert !it
        }
    }

}
