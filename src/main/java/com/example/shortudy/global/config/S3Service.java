package com.example.shortudy.global.config;

import com.example.shortudy.domain.user.dto.request.PresignedUrlResponse;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    public S3Service(S3Presigner s3Presigner, S3Client s3Client, AwsProperties awsProperties) {
        this.s3Presigner = s3Presigner;
        this.s3Client = s3Client;
        this.awsProperties = awsProperties;
    }

    /**
     * @param key: S3에 저장될 파일의 전체 경로 (ex: profile/1/image.png)
     * @param contentType: 파일 형식(ex: image/png)이 다르면 S3가 업로드를 거부
     * @param contentLength: 제한할 파일 크기(Byte 단위)가 넘으면 S3가 업로드 거부
     */
    public PresignedUrlResponse getPresignedUrl(String key, String contentType, long contentLength) {

        // S3에게 아래 조건의 업로드를 허가해주는 요청서를 만듭니다.
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(key)   // URL 경로에 자동으로 포함
                .contentType(contentType)   // [검증] 이 타입이 아니면 업로드 거부됨
                .contentLength(contentLength)   // [검증] 이 크기보다 크면 업로드 거부
                .build();

        // 위 요청서에 서명을 입혀 임시 URL로 변환
        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))   // 5분뒤 자동 폐기
                .putObjectRequest(putObjectRequest)
                .build();

        // 서버가 가진 Secret Key를 이용해 수학적으로 계산(Sign)만 해서 URL을 생성
        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(putObjectPresignRequest);

        // http(s) 형태의 전체 주소
        String url = presignedPutObjectRequest.url().toString();

        return new PresignedUrlResponse(url, key);
    }

    //TODO S3내 파일을 조회하는 로직 필요?

    /**
     * @param key: 삭제할 파일의 경로
     * S3 서버에 저장된 파일을 삭제하라고 지시
     */
    public void deleteFile(String key) {

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}
