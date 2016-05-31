package me.safrain.validator

import org.junit.Before

class BaseTest {
    EasyInspector inspector

    @Before
    void setup() {
        inspector = new EasyInspector()
        V.common = inspector.proxy(V.CommonRules)
        V.string = inspector.proxy(V.StringRules)
        V.number = inspector.proxy(V.NumberRules)
        V.ARRAY = inspector.proxy(V.ArrayRules)
    }



}

