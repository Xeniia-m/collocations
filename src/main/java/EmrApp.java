

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;


import java.io.File;

public class EmrApp {
    private static final String JAR_FILE =  "ds-ass2.jar";
    private static final String JAR_BUCKET =  "dsass2bgujar";
    private static final String STOPWORDS_FILE =  "stop_words.txt";
    private static final String STOPWORDS_BUCKET =  "stopwordsbgu";
    private static final String LOCAL_INPUT =  "input";
    private static final String S3_INPUT =  "s3://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/2gram/data";
    
    
   
    public static void main(String[] args) throws Exception {
        File jar_file = new File(JAR_FILE);

        aws aws_ = new aws();
        aws_.create_s3();
        /*
        the jar file, stop words file and input file should be in the bucket
        if not uncomment the next lines
         */
       // aws_.create_s3();
       // aws_.createBucket(JAR_BUCKET);
       // aws_.send_file(JAR_BUCKET, "ass2jar", jar_file);
       // File stop_file = new File(STOPWORDS_FILE);
       
        //   aws_.createBucket("stopwordsbgu");
        //  aws_.send_file(STOPWORDS_BUCKET,"stop",stop_file);
        //   File input = new File(LOCAL_INPUT);
        //  aws_.send_file("dsass2bgujar",LOCAL_INPUT,input);
        AWSCredentials credentials = new ProfileCredentialsProvider("./credentials", "default").getCredentials();

        AmazonElasticMapReduce mapReduce = new AmazonElasticMapReduceClient(credentials);
        mapReduce.setEndpoint("elasticmapreduce.us-east-1.amazonaws.com");

        //Step1
        //--------------------------------------------------------------------------------------
        HadoopJarStepConfig hadoopJarStep1 = new HadoopJarStepConfig()
                .withJar("s3n://dsass2bgujar/ass2jar") // This should be a full map  reduce application.
                .withMainClass("Step_one")
                .withArgs(S3_INPUT, "s3n://dsass2bgu/output1");
        StepConfig stepConfig1 = new StepConfig()
                .withName("step1")
                .withHadoopJarStep(hadoopJarStep1)
                .withActionOnFailure("TERMINATE_JOB_FLOW");

        HadoopJarStepConfig hadoopJarStep2 = new HadoopJarStepConfig()
                .withJar("s3n://dsass2bgujar/ass2jar") // This should be a full map  reduce application.

                .withMainClass("Step_two")
                .withArgs("s3n://dsass2bgu/output1", "s3n://dsass2bgu/output2");
        StepConfig stepConfig2 = new StepConfig()
                .withName("step2")
                .withHadoopJarStep(hadoopJarStep2)
                .withActionOnFailure("TERMINATE_JOB_FLOW");

        //Step3
        //--------------------------------------------------------------------------------------
        HadoopJarStepConfig hadoopJarStep3 = new HadoopJarStepConfig()
                .withJar("s3n://dsass2bgujar/ass2jar") // This should be a full map  reduce application.

                .withMainClass("Step_three")
                .withArgs("s3n://dsass2bgu/output2", "s3n://dsass2bgu/output3");
        StepConfig stepConfig3 = new StepConfig()
                .withName("step3")
                .withHadoopJarStep(hadoopJarStep3)
                .withActionOnFailure("TERMINATE_JOB_FLOW");


        //Step4
        //--------------------------------------------------------------------------------------
        HadoopJarStepConfig hadoopJarStep4 = new HadoopJarStepConfig()
                .withJar("s3n://dsass2bgujar/ass2jar") // This should be a full map  reduce application.

                .withMainClass("Step_four")
                .withArgs("s3n://dsass2bgu/output3", "s3n://dsass2bgu/output", args[0], args[1]);
        StepConfig stepConfig4 = new StepConfig()
                .withName("step4")
                .withHadoopJarStep(hadoopJarStep4)
                .withActionOnFailure("TERMINATE_JOB_FLOW");


        JobFlowInstancesConfig instances = new JobFlowInstancesConfig()
                .withInstanceCount(8)
                .withMasterInstanceType(InstanceType.M4Large.toString())
                .withSlaveInstanceType(InstanceType.M4Large.toString())
                .withHadoopVersion("2.6.0").withEc2KeyName("dsass2")
                .withKeepJobFlowAliveWhenNoSteps(false)
                .withPlacement(new PlacementType("us-east-1b"));

        RunJobFlowRequest runFlowRequest = new RunJobFlowRequest()
                .withName("Job")
                .withInstances(instances)

                .withSteps(stepConfig1,stepConfig2,stepConfig3,stepConfig4) //
                .withLogUri("s3n://dsass2bgu/logs/log")
                .withReleaseLabel("emr-6.3.0")
                .withServiceRole("EMR_DefaultRole")
                .withJobFlowRole("EMR_EC2_DefaultRole");

        RunJobFlowResult runJobFlowResult = mapReduce.runJobFlow(runFlowRequest);
        String jobFlowId1 = runJobFlowResult.getJobFlowId();
        System.out.println("Run job flow with id: " + jobFlowId1);


    }
}
