package org.togglz.console.handlers.index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.togglz.console.model.FeatureModel;
import org.togglz.core.Feature;
import org.togglz.core.metadata.EmptyFeatureMetaData;
import org.togglz.core.metadata.FeatureMetaData;

import java.util.LinkedList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IndexPageTabTest {

    private IndexPageTab indexPageTab;

    @BeforeEach
    void setUp() {
        indexPageTab = IndexPageTab.allTab(0);
    }

    @Test
    void shouldNotSetLabel() {
        assertNull(indexPageTab.getLabel());
    }

    @Test
    void shoulBeIndex0() {
        assertEquals(0, indexPageTab.getIndex());
    }

    @Test
    void shouldBeAllTab() {
        assertTrue(indexPageTab.isAllTab());
    }

    @Test
    void shouldHaveEmptyRows() {
        assertNotNull(indexPageTab.getRows());
        assertEquals(0, indexPageTab.getRows().size());
    }

    @Test
    void shouldCreateGroupedTab() {
        final IndexPageTab indexPageTab = IndexPageTab.groupTab(1, "testLabel");

        assertEquals(1, indexPageTab.getIndex());
        assertEquals("testLabel", indexPageTab.getLabel());
    }

    @Test
    void shouldBeEqualWhenLabelIsEqual() {
        IndexPageTab actual = IndexPageTab.groupTab(2, "testLabel");

        assertEquals(-9, indexPageTab.compareTo(actual));
    }

    @Test
    void shouldAddRow() {
        assertEquals(0, indexPageTab.getRows().size());

        final Feature feature = () -> "someTestName";
        FeatureModel featureModel = new FeatureModel(feature, new EmptyFeatureMetaData(feature), new LinkedList<>());
        indexPageTab.add(featureModel);

        assertNotNull(indexPageTab.getRows());
        assertEquals(1, indexPageTab.getRows().size());
    }

    @ParameterizedTest
    @MethodSource("provideComparablesAndLabels")
    void shouldNotHandleEditPaths(Integer comparable, String labels) {
        IndexPageTab actual = IndexPageTab.groupTab(2, labels);

        assertEquals(comparable, indexPageTab.compareTo(actual));
    }

    private static Stream<Arguments> provideComparablesAndLabels() {
        return Stream.of(
                Arguments.of(-9, "testLabel"),
                Arguments.of(-4, "test"),
                Arguments.of(0, "")
        );
    }
}