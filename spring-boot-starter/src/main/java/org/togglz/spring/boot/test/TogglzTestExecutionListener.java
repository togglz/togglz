package org.togglz.spring.boot.test;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;

public class TogglzTestExecutionListener extends AbstractTestExecutionListener {

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		ApplicationContext context = testContext.getApplicationContext();
		if (context.getBeanNamesForType(FeatureManager.class).length!=1) {
			return;
		}
		FeatureManager manager = context.getBean(FeatureManager.class);
		for (Feature feature : manager.getFeatures()) {
			FeatureState defaults = manager.getMetaData(feature).getDefaultFeatureState();
			FeatureState state = manager.getFeatureState(feature);
			if (defaults.isEnabled()) {
				state.enable();
			} else {
				state.disable();
			}
			manager.setFeatureState(state);
		}
	}
}
