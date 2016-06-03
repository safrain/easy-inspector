package me.safrain.validator

import org.junit.Test

class ObjectAccessTest extends BaseTest {

    class A {
        int a
        String b
    }

    @Test
    void arrayAccess() {
        inspector.validate([
                a: new A(
                        a: 1,
                        b: '2'
                )
        ]) {
            V.NUMBER.isInteger('a/a')
            V.STRING.notEmpty('a/b')
        }.with {
            assert !it
        }
    }

}
