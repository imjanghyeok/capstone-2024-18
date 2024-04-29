package capstone.facefriend.member.service;


import capstone.facefriend.member.domain.*;
import capstone.facefriend.member.exception.MemberException;
import capstone.facefriend.member.exception.MemberExceptionType;
import capstone.facefriend.member.multipartFile.ByteArrayMultipartFile;
import capstone.facefriend.member.service.dto.FaceInfoResponse;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static capstone.facefriend.member.exception.MemberExceptionType.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class BucketService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.default-profile}")
    private String defaultProfileS3Url;

    @Value("${cloud.aws.s3.origin-postfix}")
    private String originPostfix;

    @Value("${cloud.aws.s3.generated-postfix}")
    private String generatedPostfix;

    @Value("${cloud.aws.s3.resume-postfix}")
    private String resumePostfix;

    private final AmazonS3 amazonS3;
    private final MemberRepository memberRepository;
    private final FaceInfoRepository faceInfoRepository;

    // FaceInfo : origin 업로드 & generated 업로드
    public FaceInfoResponse uploadOriginAndGenerated(MultipartFile origin, ByteArrayMultipartFile generated, Long memberId) throws IOException {
        // set metadata
        ObjectMetadata originMetadata = new ObjectMetadata();
        originMetadata.setContentLength(origin.getInputStream().available());
        originMetadata.setContentType("image/jpeg");

        String originObjectName = memberId + originPostfix;
        amazonS3.putObject(
                new PutObjectRequest(
                        bucketName,
                        originObjectName,
                        origin.getInputStream(), // origin
                        originMetadata
                ).withCannedAcl(CannedAccessControlList.PublicRead)
        );
        String originS3Url = amazonS3.getUrl(bucketName, originObjectName).toString();

        // set metadata
        ObjectMetadata generatedMetadata = new ObjectMetadata();
        generatedMetadata.setContentLength(generated.getInputStream().available());
        generatedMetadata.setContentType("image/jpeg");

        String generatedObjectName = memberId + generatedPostfix;
        amazonS3.putObject(
                new PutObjectRequest(
                        bucketName,
                        generatedObjectName,
                        generated.getInputStream(), // generated
                        generatedMetadata
                ).withCannedAcl(CannedAccessControlList.PublicRead)
        );
        String generatedS3Url = amazonS3.getUrl(bucketName, generatedObjectName).toString();

        // FaceInfo 저장
        FaceInfo faceInfo = FaceInfo.builder()
                .originS3Url(originS3Url)
                .generatedS3url(generatedS3Url)
                .build();
        faceInfoRepository.save(faceInfo);

        // Member 최신화 후 저장
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(NOT_FOUND));
        member.setFaceInfo(faceInfo);
        memberRepository.save(member);

        return new FaceInfoResponse(originS3Url, generatedS3Url);
    }

    // FaceInfo : origin 수정 -> generated 수정
    public FaceInfoResponse updateOriginAndGenerated(MultipartFile origin, ByteArrayMultipartFile generated, Long memberId) throws IOException {
        deleteOriginAndGenerated(memberId); // 기존에 저장되어있던 사진 삭제
        return uploadOriginAndGenerated(origin, generated, memberId); // 새로 사진 저장
    }

    // FaceInfo : origin 삭제 -> generated 삭제
    public FaceInfoResponse deleteOriginAndGenerated(Long memberId) {
        String originObjectName = memberId + originPostfix;
        amazonS3.deleteObject(new DeleteObjectRequest(bucketName, originObjectName));

        String generatedObjectName = memberId + generatedPostfix;
        amazonS3.deleteObject(new DeleteObjectRequest(bucketName, generatedObjectName));

        return new FaceInfoResponse(defaultProfileS3Url, defaultProfileS3Url);
    }

    // Resume : resumeImages 업로드
    public List<String> uploadResumeImages(MultipartFile[] resumeImages, Long memberId) throws IOException {
        int count = 0;
        List<String> resumeImageS3Urls = new ArrayList<>();

        for (MultipartFile resumeImage : resumeImages) {
            // set metadata
            ObjectMetadata resumeMetadata = new ObjectMetadata();
            resumeMetadata.setContentLength(resumeImage.getInputStream().available());
            resumeMetadata.setContentType("image/jpeg");

            String resumeImageObjectName = memberId + resumePostfix + count++; // ex) bucketName/1-resume-1.jpg
            amazonS3.putObject(
                    new PutObjectRequest(
                            bucketName,
                            resumeImageObjectName,
                            resumeImage.getInputStream(), // resume
                            resumeMetadata
                    ).withCannedAcl(CannedAccessControlList.PublicRead)
            );
            resumeImageS3Urls.add(amazonS3.getUrl(bucketName, resumeImageObjectName).toString());
        }
        return resumeImageS3Urls;
    }
}

