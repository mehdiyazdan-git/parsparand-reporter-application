package com.armaninvestment.parsparandreporterapplication.utils;

import org.springframework.http.MediaType;

public enum FileMediaType {
    PDF("pdf", MediaType.APPLICATION_PDF),
    DOCX("docx", MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    DOC("doc", MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    XLSX("xlsx", MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
    XLS("xls", MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
    PPTX("pptx", MediaType.valueOf("application/vnd.openxmlformats-officedocument.presentationml.presentation")),
    PPT("ppt", MediaType.valueOf("application/vnd.openxmlformats-officedocument.presentationml.presentation")),
    JPG("jpg", MediaType.IMAGE_JPEG),
    JPEG("jpeg", MediaType.IMAGE_JPEG),
    PNG("png", MediaType.IMAGE_PNG),
    GIF("gif", MediaType.IMAGE_GIF),
    BMP("bmp", MediaType.valueOf("image/bmp")),
    CSV("csv", MediaType.valueOf("text/csv")),
    TIF("tif", MediaType.valueOf("image/tiff")),
    TXT("txt", MediaType.TEXT_PLAIN),
    ZIP("zip", MediaType.valueOf("application/zip")),
    DEFAULT("default", MediaType.APPLICATION_OCTET_STREAM);

    private final String extension;
    private final MediaType mediaType;

    FileMediaType(String extension, MediaType mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public static MediaType getMediaType(String fileExtension) {
        for (FileMediaType type : FileMediaType.values()) {
            if (type.extension.equalsIgnoreCase(fileExtension)) {
                return type.mediaType;
            }
        }
        return DEFAULT.mediaType;
    }
}

