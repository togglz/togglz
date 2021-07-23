package org.togglz.spock


import spock.lang.Specification
import spock.lang.Subject

@Subject(TogglzInterceptor)
class TogglzInterceptorTest extends Specification {
    static final Closure<List<MyFeatures>> EXAMPLE_CLOSURE = { [MyFeatures.ONE] }

    def "throws exception if no allDisable or allEnabled is set"() {
        given:
        Togglz specAnnotation = Mock() {
            allEnabled() >> Togglz.None
            allDisabled() >> Togglz.None
        }
        Togglz featureAnnotation = Mock() {
            allEnabled() >> Togglz.None
            allDisabled() >> Togglz.None
        }

        when:
        new TogglzInterceptor(specAnnotation, featureAnnotation)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == 'One of allEnabled or allDisabled must be set'
    }

    def "throws exception if both allDisable or allEnabled is set"() {
        given:
        Togglz featureAnnotation = Mock() {
            allEnabled() >> MyFeatures
            allDisabled() >> MyFeatures
        }

        when:
        new TogglzInterceptor(null, featureAnnotation)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == 'Only one of allEnabled or allDisabled must be set'
    }

    def "needs at least one annotation"() {
        when:
        new TogglzInterceptor(null, null)

        then:
        def ex = thrown(NullPointerException)
        ex.message == 'At least one of specAnnotation and featureAnnotation must be non-null'
    }

    def "throws exception if allEnabled() is used with enable()"() {
        given:
        Togglz featureAnnotation = Mock() {
            allEnabled() >> MyFeatures
            allDisabled() >> Togglz.None
            enable() >> EXAMPLE_CLOSURE.class
        }

        when:
        new TogglzInterceptor(null, featureAnnotation)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == 'You cannot combine allEnable with enable'
    }

    def "throws exception if allDisabled() is used with disable()"() {
        given:
        Togglz featureAnnotation = Mock() {
            allEnabled() >> Togglz.None
            allDisabled() >> MyFeatures
            disable() >> EXAMPLE_CLOSURE.class
        }

        when:
        new TogglzInterceptor(null, featureAnnotation)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == 'You cannot combine allDisabled with allDisabled'
    }
}
