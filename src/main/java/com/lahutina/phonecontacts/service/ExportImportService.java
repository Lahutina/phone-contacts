package com.lahutina.phonecontacts.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

public interface ExportImportService {
    InputStreamResource exportContacts();

    void importContacts(MultipartFile file);
}
