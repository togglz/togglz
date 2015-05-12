package org.togglz.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import org.togglz.core.repository.FeatureState;

public class FeatureStateTest {

    private static final String FEATURE_NAME = "myFeature";
    private static final String STRATEGYID = "myStrategy";
    private static final String PARAM_NAME = "myParam";
    private static final String PARAM_VALUE = "myParamValue";

    @Test
    public void serializable() throws IOException, ClassNotFoundException {
        NamedFeature feature = new NamedFeature(FEATURE_NAME);
        FeatureState featureState = new FeatureState(feature);
        featureState.setStrategyId(STRATEGYID);
        featureState.setParameter(PARAM_NAME, PARAM_VALUE);
        featureState.setEnabled(true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        try {
            try {
                oos.writeObject(featureState);
                InputStream is = new ByteArrayInputStream(os.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(is);
                try {
                    FeatureState deserializedFeatureState = (FeatureState) ois.readObject();
                    assertEquals("FeatureState.feature was not correctly serialized/deserialized",
                        featureState.getFeature(), deserializedFeatureState.getFeature());
                    assertEquals("FeatureState.strategyId was not correctly serialized/deserialized",
                        featureState.getStrategyId(), deserializedFeatureState.getStrategyId());
                    assertEquals("FeatureState.parameters were not correctly serialized/deserialized",
                        featureState.getParameter(PARAM_NAME), deserializedFeatureState.getParameter(PARAM_NAME));
                    assertEquals("FeatureState.enabled was not correctly serialized/deserialized",
                        featureState.isEnabled(), deserializedFeatureState.isEnabled());
                } finally {
                    is.close();
                    ois.close();
                }
            } catch (NotSerializableException e) {
                fail("FeatureState is not serializable");
            }
        } finally {
            os.close();
            oos.close();
        }
    }

}
