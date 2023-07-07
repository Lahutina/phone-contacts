package com.lahutina.phonecontacts.service.impl;

import com.lahutina.phonecontacts.entity.Contact;
import com.lahutina.phonecontacts.entity.Email;
import com.lahutina.phonecontacts.entity.PhoneNumber;
import com.lahutina.phonecontacts.service.ContactService;
import com.lahutina.phonecontacts.service.ExportImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for exporting and importing contacts in csv format.
 */
@Service
@RequiredArgsConstructor
public class ExportImportServiceImpl implements ExportImportService {
    private final ContactService contactService;

    public InputStreamResource exportContacts() {
        List<Contact> contacts = contactService.getAll();
        StringBuilder csvData = new StringBuilder();

        csvData.append("Name,Email,Phone\n"); // header

        for (Contact contact : contacts) {
            // all data in contact to one string
            String name = contact.getName();
            String emails = contact.getEmails().stream()
                    .map(Email::getEmail)
                    .collect(Collectors.joining(";"));
            String phoneNumbers = contact.getPhones().stream()
                    .map(PhoneNumber::getNumber)
                    .collect(Collectors.joining(";"));
            csvData.append(name).append(",")
                    .append(emails).append(",")
                    .append(phoneNumbers).append("\n");
        }

        return new InputStreamResource(new ByteArrayInputStream(
                csvData.toString().getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void importContacts(MultipartFile file) {
        List<Contact> contacts = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            reader.readLine(); // skip headers

            String line;
            while ((line = reader.readLine()) != null) {
                // split one line to different contact fields
                String[] data = line.split(",");
                String name = data[0].trim();
                String[] emails = data[1].split(";");
                String[] phoneNumbers = data[2].split(";");

                Contact contact = new Contact(null, name, new HashSet<>(), new HashSet<>(), null);

                for (String email : emails) {
                    Email emailObj = new Email(email.trim());
                    emailObj.setContact(contact);
                    contact.getEmails().add(emailObj);
                }

                for (String phoneNumber : phoneNumbers) {
                    PhoneNumber phoneNumberObj = new PhoneNumber(phoneNumber.trim());
                    phoneNumberObj.setContact(contact);
                    contact.getPhones().add(phoneNumberObj);
                }

                contacts.add(contact);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the file.", e);
        }

        for (Contact contact : contacts) {
            contactService.create(contact);
        }
    }
}
