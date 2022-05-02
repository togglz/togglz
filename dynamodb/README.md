DynamoDB state repository
-----

[DynamoDB](https://aws.amazon.com/dynamodb/) is a fully managed no-sql database available on Amazon AWS that can be easily
 configured to use as a state repository for togglz.

To use DynamoDB as a state repository with togglz, you must provision a table in the AWS.

To do this from the command line (change your table name out for the one you want to use):


```
aws dynamodb create-table --table-name YOUR_TABLE_NAME \
 --region=us-west-1 \
 --attribute-definitions AttributeName=featureName,AttributeType=S \
 --key-schema AttributeName=featureName,KeyType=HASH \
 --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
 ```

 Then create a state repository with togglz:

 For example, using it in combination with the spring-boot autoconfiguration you only need to provide the
 `StateRepository` instance - which relies on being passed in a configured `AmazonDynamoDBClient` instance

 ```java

     @Bean
     AWSCredentials awsCredentials() {
         return new InstanceProfileCredentialsProvider().getCredentials();
     }

     @Bean
     public AmazonDynamoDBClient dynamoDBClient(AWSCredentials credentials) {
         return new AmazonDynamoDBClient(credentials);
     }

     @Bean
     public DynamoDBStateRepository dynamoDBStateRepository(AmazonDynamoDBClient amazonDynamoDBClient) {
         return new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(amazonDynamoDBClient).withStateStoredInTable("togglz").build();
     }
 ```

 Caching
 ----
 To decrease reads from the dynamo tables, you can wrap the state repository with a caching one - like this example which caches
 for 30 seconds:

 ```java
    @Bean
    public StateRepository stateRepository(AmazonDynamoDBClient amazonDynamoDBClient) {
        DynamoDBStateRepository dynamoDBStateRepository = new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(amazonDynamoDBClient).withStateStoredInTable("togglz").build();
        return new CachingStateRepository(dynamoDBStateRepository, 30, TimeUnit.SECONDS);
    }
 ```




