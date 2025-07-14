package com.samupert.stages;

import com.samupert.stacks.LatestNewsStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.constructs.Construct;

public class LatestNewsPipelineStage extends Stage {

    public LatestNewsPipelineStage(@NotNull Construct scope, @NotNull String id, @Nullable StageProps props) {
        super(scope, id, props);

        LatestNewsStack latestNewsStack = new LatestNewsStack(this, "LatestNews", null);
    }
}
