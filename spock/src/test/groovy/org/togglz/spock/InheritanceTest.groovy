package org.togglz.spock

import org.togglz.testing.TestFeatureManager

import spock.lang.Specification

@Togglz(allEnabled = MyFeatures)
abstract class EnabledParentTest extends Specification {

    def "method in parent class"() {
        expect:
        MyFeatures.values().active.every { it == expected }
    }

    boolean getExpected() {
        true
    }
}


class EnabledChildTest extends EnabledParentTest {

    @Togglz(allDisabled = MyFeatures)
    def "can override on method level"() {
        expect:
        MyFeatures.values().active.every { !it }
    }

    @Togglz(disable = { [MyFeatures.ONE, MyFeatures.THREE] })
    def "can disable individual features method level"() {
        expect:
        !MyFeatures.ONE.active
        MyFeatures.TWO.active
        !MyFeatures.THREE.active
    }

    @Togglz(allEnabled = MyFeatures, disable = { [MyFeatures.TWO] })
    def "all enabled with individual disabled"() {
        expect:
        MyFeatures.ONE.active
        !MyFeatures.TWO.active
        MyFeatures.THREE.active
    }

}

@Togglz(allDisabled = MyFeatures)
class EnabledChildOverrideTest extends EnabledParentTest {

    @Togglz(enable = { [MyFeatures.ONE, MyFeatures.THREE] })
    def "can enable individual features method level"() {
        expect:
        MyFeatures.ONE.active
        !MyFeatures.TWO.active
        MyFeatures.THREE.active
    }

    @Override
    boolean getExpected() {
        false
    }
}

@Togglz(allDisabled = MyFeatures)
abstract class DisabledParentTest extends Specification {

    def "method in parent class"() {
        expect:
        MyFeatures.values().active.every { it == expected }
    }

    boolean getExpected() {
        false
    }
}

class DisabledChildTest extends DisabledParentTest {

    @Togglz(allEnabled = MyFeatures)
    def "can override on method level"() {
        expect:
        MyFeatures.values().active.every()
    }

    @Togglz(enable = { [MyFeatures.ONE, MyFeatures.THREE] })
    def "can enable individual features method level"() {
        expect:
        MyFeatures.ONE.active
        !MyFeatures.TWO.active
        MyFeatures.THREE.active
    }

    @Togglz(allDisabled = MyFeatures, enable = { [MyFeatures.TWO] })
    def "all enabled with individual enabled"() {
        expect:
        !MyFeatures.ONE.active
        MyFeatures.TWO.active
        !MyFeatures.THREE.active
    }

    def "data-driven disabled"(MyFeatures feature, TestFeatureManager testFeatureManager) {
        expect:
        MyFeatures.values().active.every { !it }

        when:
        testFeatureManager.enable(feature)

        then:
        feature.active

        where:
        feature << MyFeatures.values()
    }
}


@Togglz(allEnabled = MyFeatures)
class DisabledChildOverrideTest extends DisabledParentTest {

    @Togglz(disable = { [MyFeatures.ONE, MyFeatures.THREE] })
    def "can disable individual features method level"() {
        expect:
        !MyFeatures.ONE.active
        MyFeatures.TWO.active
        !MyFeatures.THREE.active
    }

    @Override
    boolean getExpected() {
        true
    }
}
