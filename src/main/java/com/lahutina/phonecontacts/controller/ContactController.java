package com.lahutina.phonecontacts.controller;

import com.lahutina.phonecontacts.entity.Contact;
import com.lahutina.phonecontacts.service.ContactService;
import com.lahutina.phonecontacts.service.ExportImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller class for handling contact-related requests
 */
@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contactService;
    private final ExportImportService exportImportService;


    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        var contacts = contactService.getAll();
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getById(@PathVariable Long id) {
        Contact contact = contactService.getById(id);
        return ResponseEntity.ok(contact);
    }

    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        Contact createdContact = contactService.create(contact);
        return ResponseEntity.ok(createdContact);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable Long id, @RequestBody Contact contact) {
        Contact updatedContact = contactService.update(id, contact);
        return ResponseEntity.ok(updatedContact);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exports contacts as a CSV file.
     *
     * @return The ResponseEntity with the exported CSV content.
     */
    @GetMapping(path = "/export", produces = "text/csv")
    public ResponseEntity<InputStreamResource> exportContacts() {
        InputStreamResource csvContent = exportImportService.exportContacts();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.csv");

        return ResponseEntity.ok().headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvContent);
    }

    /**
     * Imports contacts from a CSV file.
     *
     * @param file The CSV file to import.
     * @return The ResponseEntity indicating the success of the import operation.
     */
    @PostMapping("/import")
    public ResponseEntity<String> importContacts(@RequestParam("file") MultipartFile file) {
        exportImportService.importContacts(file);
        return ResponseEntity.ok("Contacts imported successfully.");
    }
}

