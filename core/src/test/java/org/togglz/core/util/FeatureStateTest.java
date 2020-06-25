package org.togglz.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.togglz.core.repository.FeatureState;

import static org.junit.jupiter.api.Assertions.*;

class FeatureStateTest {

    private static final String FEATURE_NAME = "myFeature";
    private static final String STRATEGYID = "myStrategy";
    private static final String PARAM_NAME = "myParam";
    private static final String PARAM_VALUE = "myParamValue";

    @Test
    void serializable() throws IOException, ClassNotFoundException {
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
                    assertEquals(featureState.getFeature(), deserializedFeatureState.getFeature(), "FeatureState.feature was not correctly serialized/deserialized");
                    assertEquals(featureState.getStrategyId(), deserializedFeatureState.getStrategyId(), "FeatureState.strategyId was not correctly serialized/deserialized");
                    assertEquals(featureState.getParameter(PARAM_NAME), deserializedFeatureState.getParameter(PARAM_NAME), "FeatureState.parameters were not correctly serialized/deserialized");
                    assertEquals(featureState.isEnabled(), deserializedFeatureState.isEnabled(), "FeatureState.enabled was not correctly serialized/deserialized");
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
