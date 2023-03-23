package org.togglz.s3;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.utils.IoUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

class AmazonS3ClientMOCK implements S3Client {
    Map<String, Map<String, String>> repo = new HashMap<>();

    @Override
    public ResponseInputStream<GetObjectResponse> getObject(GetObjectRequest getObjectRequest) {
        String s3Object = repo.get(getObjectRequest.bucket()).get(getObjectRequest.key());
        if (s3Object == null) {
            InputStream empty = new InputStream() {
                @Override
                public int read() {
                    return -1;  // end of stream
                }
            };
            return new ResponseInputStream<>(GetObjectResponse.builder().build(), AbortableInputStream.create(empty));
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s3Object.getBytes());
        return new ResponseInputStream<>(GetObjectResponse.builder().build(), AbortableInputStream.create(byteArrayInputStream));
    }

    @Override
    public PutObjectResponse putObject(PutObjectRequest putObjectRequest, RequestBody requestBody) throws AwsServiceException, SdkClientException {
        Map<String, String> r2 = repo.get(putObjectRequest.bucket());

        String s = null;
        try {
            s = IoUtils.toUtf8String(requestBody.contentStreamProvider().newStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (r2.isEmpty()) {
            r2.put(putObjectRequest.key(), s);
            repo.put(putObjectRequest.bucket(), r2);
        } else {
            r2.put(putObjectRequest.key(), s);
        }
        return PutObjectResponse.builder().build();
    }

    @Override
    public CreateBucketResponse createBucket(CreateBucketRequest createBucketRequest) throws AwsServiceException, SdkClientException {
        repo.put(createBucketRequest.bucket(), new HashMap<>());
        return CreateBucketResponse.builder().build();
    }

    @Override
    public String serviceName() {
        return null;
    }

    @Override
    public void close() {

    }
}
