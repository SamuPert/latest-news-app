package com.samupert.stacks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.CertificateValidation;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.route53.*;
import software.amazon.awscdk.services.route53.targets.ApiGateway;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.constructs.Construct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LatestNewsStack extends Stack {

    public LatestNewsStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);

        // Test, build and deploy the Lambda function from source.
        Function latestNewsLambda = createLatestNewsLambda();

        // Fetch the existing hosted zone by zone-id and zone name.
        IHostedZone samupertDotComHostedZone = getHostedZone();
        
        // Request a certificate from AWS Certificate Manager.
        Certificate apiCertificate = getApiCertificate(samupertDotComHostedZone);

        // Create the API Gateway.
        RestApi restApi = createApiGateway(apiCertificate);

        // Map GET /news to the GetLatestNewsLambda lambda function.
        Resource newsResource = restApi.getRoot().addResource("news");
        newsResource.addMethod("GET", new LambdaIntegration(latestNewsLambda));

        // Create an alias for api.samupert.com that points to the API Gateway.
        ARecord.Builder.create(this, "ApiGatewayAliasRecord")
                       .zone(samupertDotComHostedZone)
                       .recordName("api")
                       .target(RecordTarget.fromAlias(new ApiGateway(restApi)))
                       .build();
    }

    @NotNull
    private IHostedZone getHostedZone() {
        HostedZoneAttributes hostedZoneAttributes = HostedZoneAttributes.builder()
                                                                        .hostedZoneId("Z04006722AURQ0F5CG9GT")
                                                                        .zoneName("samupert.com")
                                                                        .build();
        return HostedZone.fromHostedZoneAttributes(this, "SamupertDotComZone", hostedZoneAttributes);
    }

    @NotNull
    private Certificate getApiCertificate(IHostedZone samupertDotComHostedZone) {
        return Certificate.Builder.create(this, "ApiCertificate")
                                  .domainName("api.samupert.com")
                                  .validation(CertificateValidation.fromDns(samupertDotComHostedZone))
                                  .build();
    }

    private Function createLatestNewsLambda(){

        List<String> packagingInstructions = Arrays.asList(
                "/bin/sh",
                "-c",
                "cd get-latest-news-lambda " +
                "&& chmod +x ./gradlew " +
                "&& ./gradlew --stop " +
                "&& ./gradlew clean " +
                "&& ./gradlew build " +
                "&& ls -la /asset-input/get-latest-news-lambda/build/distributions " +
                "&& cp /asset-input/get-latest-news-lambda/build/distributions/* /asset-output/"
        );

        BundlingOptions bundlingOptions = BundlingOptions.builder()
                .command(packagingInstructions)
                .image(Runtime.JAVA_17.getBundlingImage())
                .user("root")
                .outputType(BundlingOutput.AUTO_DISCOVER)
                .command(packagingInstructions)
                .build();

        AssetOptions lambdaBuildOptions = AssetOptions.builder()
                                            .bundling(bundlingOptions)
                                            .build();

        Map<String, String> environmentVariables = Map.of("API_KEY", "sample_api_key");

        Function latestNewsFunction = new Function(this, "GetLatestNewsLambda", FunctionProps.builder()
                                                                      .runtime(Runtime.JAVA_17)
                                                                      .code(Code.fromAsset("../software/", lambdaBuildOptions))
                                                                      .handler("com.samupert.GetLatestNewsLambdaHandler")
                                                                      .memorySize(512)
                                                                      .timeout(Duration.seconds(20))
                                                                      .logRetention(RetentionDays.ONE_DAY)
                                                                      .environment(environmentVariables)
                                                                      .build());

        // Enable SnapStart property
        CfnFunction cfnLatestNewsFunction = (CfnFunction) latestNewsFunction.getNode().getDefaultChild();
        CfnFunction.SnapStartProperty snapStartProperty = CfnFunction.SnapStartProperty.builder()
                                                                                       .applyOn("PublishedVersions")
                                                                                       .build();
        assert cfnLatestNewsFunction != null;
        cfnLatestNewsFunction.setSnapStart(snapStartProperty);

        return latestNewsFunction;
    }

    private RestApi createApiGateway(Certificate apiCertificate){

        DomainNameOptions domainNameOptions = DomainNameOptions.builder()
                                                        .domainName("api.samupert.com")
                                                        .certificate(apiCertificate)
                                                        .securityPolicy(SecurityPolicy.TLS_1_2)
                                                        .endpointType(EndpointType.REGIONAL)
                                                        .build();

        return RestApi.Builder.create(this, "RestApiForSamupertDotCom")
                              .domainName(domainNameOptions)
                              .endpointTypes(List.of(EndpointType.EDGE))
                              .description("REST API for api.samupert.com")
                              .build();
    }
}
