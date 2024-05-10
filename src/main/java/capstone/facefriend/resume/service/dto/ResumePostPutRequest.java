package capstone.facefriend.resume.service.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ResumePostPutRequest(
        List<MultipartFile> images,
        List<String> categories,
        String content
) {
}