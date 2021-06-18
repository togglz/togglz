package org.togglz.spock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.model.parallel.ResourceAccessMode;
import org.togglz.core.Feature;

import groovy.lang.Closure;

/**
 * Creates a {@link org.togglz.testing.TestFeatureManager TestFeatureManager} for the test.
 *
 * <p>
 * Allows the user to control the state of a {@link Feature} for a test or specification.
 * It can only be used to manage one feature enum at a time,
 * as the {@link org.togglz.testing.TestFeatureManager TestFeatureManager} doesn't support multiple features.
 * </p>
 * <h2>Parallel Execution Support</h2>
 * <p>
 * This extension supports Spock's parallel execution capability.
 * Every specification and test annotated with {@code @Togglz} will acquire {@link ResourceAccessMode#READ_WRITE READ_WRITE} lock
 * for the key {@link TogglzExtension#TOGGLZ_FEATURE_RESOURCE}.
 * </p>
 * <h2>Where to apply</h2>
 * <p>
 * Can be applied on individual tests and on the specification.
 * </p>
 * <p>
 * When applied on an individual test it will only affect that test.
 * </p>
 * <p>
 * When applied on an specification it will behave as if every test was annotated with the same annotation.
 * </p>
 * <p>
 * If both test and specification is annotated,
 * then both will be merged, and the feature annotation will override the specification annotation on conflicts.
 * </p>
 *
 * <h3>Here are some examples: </h3>
 * <table>
 *   <tr>
 *     <th>Annotation on Specification</th>
 *     <th>Annotation on Test</th>
 *     <th>Result</th>
 *   </tr>
 *   <tr>
 *     <td>{@code @Togglz(allEnabled = MyFeature)}</td>
 *     <td>-</td>
 *     <td>{@code allEnabled = MyFeature}</td>
 *   </tr>
 *   <tr>
 *     <td>-</td>
 *     <td>{@code @Togglz(allDisabled = MyFeature)}</td>
 *     <td>{@code allDisabled = MyFeature}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code @Togglz(allEnabled = MyFeature)}</td>
 *     <td>{@code @Togglz(allDisabled = MyFeature)}</td>
 *     <td>{@code allDisabled = MyFeature}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code @Togglz(allEnabled = MyFeature)}</td>
 *     <td>{@code @Togglz(disable = {[MyFeature.One]})}</td>
 *     <td>{@code allEnabled = MyFeature, disable = {[MyFeature.One]})}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code @Togglz(allEnabled = MyFeature)}</td>
 *     <td>{@code @Togglz(allDisabled = MyFeature, enable = {[MyFeature.One]})}</td>
 *     <td>{@code allDisabled = MyFeature, enable = {[MyFeature.One]})}</td>
 *   </tr>
 * </table>
 *
 * <h2>Enabling/disabling individual features</h2>
 * <p>
 * You always have to have either {@code allEnabled} or {@code allDisabled} defined for a test.
 * You can inherit the value from the specification (see the Where to apply chapter).
 * </p>
 * <p>
 * If you have used {@code allDisabled} then you can enable individual features using the {@code enable} setting.
 * The same applies for {@code allEnabled} and {@code disabled}.
 * </p>
 * <p>
 * Note: {@code enable}/{@code disabled} expect a closure that returns a list, not a list directly.
 * </p>
 *
 * <h3>Here are some examples: </h3>
 * <pre>
 * &#064;Togglz(allDisabled = MyFeature)
 * class MyTest extends Specification {
 *     def "test allDisabled"() {
 *         expect:
 *         !MyFeature.ONE.active
 *         !MyFeature.TWO.active
 *     }
 *
 *     &#064;Togglz(enable = {[MyFeature.One]})
 *     def "enable feature one"() {
 *         expect:
 *         MyFeature.ONE.active
 *         !MyFeature.TWO.active
 *     }
 *
 *     &#064;Togglz(enable = {[MyFeature.TWO]})
 *     def "enable feature one"() {
 *         expect:
 *         !MyFeature.ONE.active
 *         MyFeature.TWO.active
 *     }
 * }
 * </pre>
 *
 * <h2>Injecting {@link org.togglz.testing.TestFeatureManager TestFeatureManager} into a test</h2>
 * <p>
 * You can declare {@link org.togglz.testing.TestFeatureManager TestFeatureManager} as a parameter to any test that is either
 * directly annotated with {@code @Togglz} or it's Specification is annotated with it.
 * </p>
 *
 * <h3>Here are some examples: </h3>
 * <pre>
 * &#064;Togglz(allDisabled = MyFeature)
 * class MyTest extends Specification {
 *
 *      def "injection in non data-driven feature"(TestFeatureManager testFeatureManager) {
 *          expect:
 *          MyFeatures.values().every { !it.active }
 *
 *          when:
 *          testFeatureManager.enable(MyFeatures.ONE)
 *
 *          then:
 *          MyFeatures.ONE.active
 *      }
 *
 *      def "data-driven disabled"(MyFeatures feature, TestFeatureManager testFeatureManager) {
 *          expect:
 *          MyFeatures.values().every { !it.active }
 *
 *          when:
 *          testFeatureManager.enable(feature)
 *
 *          then:
 *          feature.active
 *
 *          where:
 *          feature << MyFeatures.values()
 *      }
 * }
 * </pre>
 *
 * <h2>Advanced usage</h2>
 * <p>
 * As {@code enable}/{@code disabled} use a closure, you can add logic to control which feature is enabled/disabled.
 * The closure gets {@link org.spockframework.runtime.extension.builtin.PreconditionContext PreconditionContext} as parameter
 * so you can use it to control which feature is returned.
 * </p>
 * <p>Use this feature with care!</p>
 *
 * <h3>Here is an example: </h3>
 * <pre>
 * &#064;Togglz(allDisabled = MyFeatures,
 *     enable = { PreconditionContext pc -> pc.os.windows ? [MyFeatures.ONE] : [MyFeatures.TWO] })
 * def "all disabled with individual enabled using PreconditionContext"() {
 *     given:
 *     def os = OperatingSystem.getCurrent()
 *
 *     expect:
 *     MyFeatures.ONE.active == os.windows
 *     MyFeatures.TWO.active == !os.windows
 *     !MyFeatures.THREE.active
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
@ExtensionAnnotation(TogglzExtension.class)
public @interface Togglz {
    /**
     * Feature class that should have every feature enabled.
     * <p>
     * Example Usage:
     * </p>
     * <p>
     * {@code @Togglz(allEnable = MyFeature)}
     * </p>
     * <p>
     * It is mutually exclusive with {@link #allDisabled()}.
     * </p>
     */
    Class<? extends Feature> allEnabled() default None.class;

    /**
     * Feature class that should have every feature disabled.
     * <p>
     * It is mutually exclusive with {@link #allEnabled()}.
     */
    Class<? extends Feature> allDisabled() default None.class;

    /**
     * Can be used in conjunction with {@link #allDisabled()} to enable individual features again.
     * <p>
     * Define a closure that returns a list of features to enable.
     * </p>
     * <p>
     * Example Usage:
     * </p>
     * <p>
     * {@code @Togglz(allDisable = MyFeature, enable = {[MyFeature.ONE]})}
     * </p>
     * <ul>
     *     <li>It cannot be combined with {@link #allEnabled()}.</li>
     *     <li>It is mutually exclusive with {@link #disable()}.</li>
     * </ul>
     */
    Class<? extends Closure<List<? extends Feature>>> enable() default None.class;

    /**
     * The inverse of {@link #enable()}
     * <ul>
     *     <li>It cannot be combined with {@link #allDisabled()}.</li>
     *     <li>It is mutually exclusive with {@link #enable()}.</li>
     * </ul>
     */
    Class<? extends Closure<List<? extends Feature>>> disable() default None.class;

    /**
     * Marker class to indicate that the field has the default value.
     */
    abstract class None extends Closure<List<? extends Feature>> implements Feature {
        // should not be instantiable
        private None(Object owner) {
            super(owner);
        }
    }
}
