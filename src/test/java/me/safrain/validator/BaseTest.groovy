package me.safrain.validator

import me.safrain.validator.accessor.DefaultArrayAccessor
import me.safrain.validator.accessor.DefaultPropertyAccessor
import org.junit.Before

class BaseTest {
    EasyInspector inspector

    @Before
    void setup() {
        inspector = new EasyInspector(new Config(
                propertyAccessor: new DefaultPropertyAccessor(),
                arrayAccessor: new DefaultArrayAccessor()
        ))
        V.COMMON = inspector.proxy(V.CommonRules)
        V.STRING = inspector.proxy(V.StringRules)
        V.NUMBER = inspector.proxy(V.NumberRules)
        V.ARRAY = inspector.proxy(V.ArrayRules)
    }
}

