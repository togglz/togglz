package org.togglz.slack;

import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.composite.CompositeStateRepository;

public class SlackWrapperStateRepository extends CompositeStateRepository {

    public SlackWrapperStateRepository(SlackStateRepository slack, StateRepository other) {
        super(slack, other);
        this.setSetterSelection(SetterSelection.ALL);
    }
}
