package me.safrain.validator

import org.junit.Before

class BaseTest {
    EasyInspector inspector

    @Before
    void setup() {
        inspector = new EasyInspector()
        V.COMMON = inspector.proxy(V.CommonRules)
        V.STRING = inspector.proxy(V.StringRules)
        V.NUMBER = inspector.proxy(V.NumberRules)
        V.ARRAY = inspector.proxy(V.ArrayRules)
    }



}

