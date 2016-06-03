package me.safrain.validator

import org.junit.Test

class MiscTest extends BaseTest {
    @Test
    void testGet() {
        inspector.validate([a: [1]]) {
            assert V.get('a') == [1]
        }
    }

    @Test
    void testGetNotExist() {
        inspector.validate([a: [1]]) {
            assert V.get('b') == null
        }
    }

    @Test
    void testGetMulti() {
        inspector.validate([
                a: [1, 2, 3],
                b: [
                        x: 1,
                        y: 2,
                        z: 3
                ]
        ]
        ) {
            assert V.get('a/[*]') == 1
            assert V.getAll('a/[*]') == [1, 2, 3]
            assert V.getAll('a/[?]') == [1]
            assert V.get('b/*') == 1
            assert V.getAll('b/*') == [1, 2, 3]
            assert V.getAll('b/?') == [1]
        }
    }

    @Test
    void testRejectedType() {
        inspector.validate([a: [1, 2, 3]]) {
            assert V.get('a/*') == null
            assert V.getAll('a/*') == []
            assert V.getAll('a/?') == []
        }
    }


}
