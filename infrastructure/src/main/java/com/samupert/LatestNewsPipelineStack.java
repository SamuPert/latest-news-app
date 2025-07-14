package com.samupert;

import com.samupert.stages.LatestNewsPipelineStage;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.StageProps;
import software.amazon.awscdk.pipelines.*;
import software.amazon.awscdk.services.codebuild.BuildEnvironment;
import software.amazon.awscdk.services.codebuild.ComputeType;
import software.amazon.awscdk.services.codebuild.LinuxBuildImage;
import software.amazon.awscdk.services.codecommit.IRepository;
import software.amazon.awscdk.services.codecommit.Repository;
import software.constructs.Construct;

import java.util.List;

public class LatestNewsPipelineStack extends Stack {
    public LatestNewsPipelineStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);

        IRepository repository = Repository.fromRepositoryName(this, "AppRepository", "latest-news-app");

        CodePipeline pipeline = createCodePipeline(repository);

        StageDeployment prodStage = pipeline.addStage(new LatestNewsPipelineStage(this, "Production", StageProps.builder().env(props.getEnv()).build()));
    }

    private CodePipeline createCodePipeline(IRepository repository) {
        // Create the build step for CodeBuild
        CodeBuildStep cbs = getSynthesizeStep(repository, "infrastructure");

        // Create the self-mutable pipeline.
        return CodePipeline.Builder.create(this, "GetLatestNewsPipeline")
                .pipelineName("GetLatestNewsPipeline")
                .selfMutation(true)
                .synth(cbs)
                .build();
    }

    private CodeBuildStep getSynthesizeStep(IRepository repoToSynth, String folderToSynth) {

        // Create the build environment for the pipeline.
        BuildEnvironment buildEnvironment = BuildEnvironment.builder()
                // Images provided by CodeBuild: https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-available.html
                .buildImage(LinuxBuildImage.AMAZON_LINUX_2_5)   // aws/codebuild/amazonlinux2-x86_64-standard:5.0
                .computeType(ComputeType.SMALL)
                .privileged(true)
                .build();

        return CodeBuildStep.Builder.create("Synthesize")
                .input(CodePipelineSource.codeCommit(repoToSynth, "main"))
                .buildEnvironment(buildEnvironment)
                .commands(List.of(
                        "npm install -g aws-cdk",
                        "cd " + folderToSynth,
                        "cdk synth"
                ))
                .primaryOutputDirectory(folderToSynth + "/cdk.out")
                .build();
    }
}
