package org.togglz.slack;

import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.composite.CompositeStateRepository;

/**
 * @author Tomasz Skowroński
 * @since 2.4.0
 */
public class SlackWrapperStateRepository extends CompositeStateRepository {

    public SlackWrapperStateRepository(StateRepository wrapped, SlackStateRepository notifications) {
        super(wrapped, notifications);
        this.setSetterSelection(SetterSelection.ALL);
    }
}
