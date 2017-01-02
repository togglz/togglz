package org.togglz.slack;

import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.composite.CompositeStateRepository;

public class SlackWrapperStateRepository extends CompositeStateRepository {

    public SlackWrapperStateRepository(StateRepository wrapped, SlackStateRepository slack) {
        super(wrapped, slack);
        this.setSetterSelection(SetterSelection.ALL);
    }
}
