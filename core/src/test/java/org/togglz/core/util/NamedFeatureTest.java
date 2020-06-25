package org.togglz.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class NamedFeatureTest {

    @Test
    void serializable() throws IOException, ClassNotFoundException {
        NamedFeature feature = new NamedFeature("SERIALIZABLE");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        try {
            try {
                oos.writeObject(feature);
                InputStream is = new ByteArrayInputStream(os.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(is);
                try {
                    Object deserializedFeature = ois.readObject();
                    assertEquals(feature, deserializedFeature, "NamedFeature was not correctly serialized/deserialized");
                } finally {
                    is.close();
                    ois.close();
                }
            } catch (NotSerializableException e) {
                fail("NamedFeature is not serializable");
            }
        } finally {
            os.close();
            oos.close();
        }
    }
}