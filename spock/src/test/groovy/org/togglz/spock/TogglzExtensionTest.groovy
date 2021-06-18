package org.togglz.spock

import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.extension.builtin.PreconditionContext
import org.togglz.testing.TestFeatureManager

import spock.lang.FailsWith
import spock.lang.Specification
import spock.util.environment.OperatingSystem

class TogglzExtensionTest extends Specification {

    @Togglz(allEnabled = MyFeatures)
    def "all enabled"() {
        expect:
        MyFeatures.values().active.every()
    }

    @Togglz(allDisabled = MyFeatures)
    def "all disabled"() {
        expect:
        MyFeatures.values().active.every { !it }
    }

    @Togglz(allEnabled = MyFeatures, disable = { [MyFeatures.TWO] })
    def "all enabled with individual disabled"() {
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

    @Togglz(allDisabled = MyFeatures, enable = { PreconditionContext pc -> pc.os.windows ? [MyFeatures.ONE] : [MyFeatures.TWO] })
    def "all disabled with individual enabled using PreconditionContext"() {
        given:
        def os = OperatingSystem.getCurrent()

        expect:
        MyFeatures.ONE.active == os.windows
        MyFeatures.TWO.active == !os.windows
        !MyFeatures.THREE.active
    }

    @Togglz(allDisabled = MyFeatures, enable = { os.windows ? [MyFeatures.ONE] : [MyFeatures.TWO] })
    def "all disabled with individual enabled using PreconditionContext as delegate"() {
        given:
        def os = OperatingSystem.getCurrent()

        expect:
        MyFeatures.ONE.active == os.windows
        MyFeatures.TWO.active == !os.windows
        !MyFeatures.THREE.active
    }

    @Togglz(allDisabled = MyFeatures)
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

    @Togglz(allEnabled = MyFeatures)
    def "injection in non data-driven feature"(TestFeatureManager testFeatureManager) {
        expect:
        MyFeatures.values().active.every()

        when:
        testFeatureManager.disable(MyFeatures.ONE)

        then:
        !MyFeatures.ONE.active
    }

    @FailsWith(ConditionNotSatisfiedError)
    @Togglz(allDisabled = MyFeatures)
    def "canary should fail"() {
        expect:
        false
    }
}


@Togglz(allEnabled = MyFeatures)
class AllEnabledClassTest extends Specification {

    def "all enabled"() {
        expect:
        MyFeatures.values().active.every()
    }
}


@Togglz(allDisabled = MyFeatures)
class AllDisabledClassTest extends Specification {

    def "all disabled"() {
        expect:
        MyFeatures.values().active.every { !it }
    }
}
