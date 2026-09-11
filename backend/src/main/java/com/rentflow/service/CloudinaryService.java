package com.rentflow.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rentflow.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public UploadResult upload(MultipartFile file, UUID organizationId) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            throw new BadRequestException("Only image and video files are supported");
        }

        String resourceType = contentType.startsWith("video/") ? "video" : "image";
        String publicId = "rentflow/" + organizationId + "/" + UUID.randomUUID();

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", resourceType,
                            "overwrite", false));
            return new UploadResult(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id"),
                    resourceType);
        } catch (IOException | RuntimeException ex) {
            throw new BadRequestException("File upload failed");
        }
    }

    public void delete(String publicId, String resourceType) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType == null ? "image" : resourceType));
        } catch (Exception ex) {
            throw new BadRequestException("File deletion failed");
        }
    }

    public record UploadResult(String secureUrl, String publicId, String resourceType) {
    }
}