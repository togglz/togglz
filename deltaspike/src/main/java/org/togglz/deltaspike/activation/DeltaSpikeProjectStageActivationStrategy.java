package org.togglz.deltaspike.activation;

import java.util.List;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.togglz.core.activation.AbstractTokenizedActivationStrategy;
import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

/**
 * <p>
 * An activation strategy based on the active {@code ProjectStage} within the DeltaSpike environment.
 * </p>
 * <p>
 * Although only one {@code ProjectStage} can be active at any given time, one or more stage names can be specified in a
 * comma-separated value via the "{@value #PARAM_STAGES}" parameter. This strategy works by only activating the feature
 * if at least one of the stages are currently active. Stage names are case sensitive and should match the stage class
 * name.
 * </p>
 * <p>
 * If a given stage is prefixed with the NOT operator ({@code !}), the feature will only be active if the stage is
 * <b>not</b> active. If the value of the "{@value #PARAM_STAGES}" parameter was {@code "Development,!Production"}, the
 * feature would only be active if "Development" is active or if "Production" is not active.
 * </p>
 *
 * @author Alasdair Mercer
 * @see AbstractTokenizedActivationStrategy
 */
public class DeltaSpikeProjectStageActivationStrategy extends AbstractTokenizedActivationStrategy {

    public static final String ID = "deltaspike-project-stage";
    public static final String PARAM_STAGES = "stages";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "DeltaSpike Project Stage";
    }

    @Override
    protected boolean isActive(FeatureState featureState, FeatureUser user, List<Token> tokens) {
        ProjectStage activeProjectStage = ProjectStageProducer.getInstance().getProjectStage();

        for (Token token : tokens) {
            ProjectStage projectStage = ProjectStage.valueOf(token.getValue());
            if (activeProjectStage.equals(projectStage) != token.isNegated()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            ParameterBuilder.create(PARAM_STAGES)
                .label("Project Stages")
                .description("A comma-separated list of stage names for which the feature should be active. A stage "
                    + "can be negated by prefixing the name with the NOT operator (!).")
        };
    }

    @Override
    public String getTokenParameterName() {
        return PARAM_STAGES;
    }
}
