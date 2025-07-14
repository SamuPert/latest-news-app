package com.samupert;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class LatestNewsPipelineApp {
    public static void main(final String[] args) {
        App app = new App();

        Environment environment = Environment.builder()
                .account("539242623851")
                .region("eu-central-1")
                .build();

        new LatestNewsPipelineStack(app, "LatestNewsCdkPipelineStack", StackProps.builder().env(environment).build());

        app.synth();
    }
}

