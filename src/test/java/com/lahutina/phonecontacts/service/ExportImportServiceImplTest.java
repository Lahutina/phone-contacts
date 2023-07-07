package com.lahutina.phonecontacts.service;

import com.lahutina.phonecontacts.entity.Contact;
import com.lahutina.phonecontacts.entity.Email;
import com.lahutina.phonecontacts.entity.PhoneNumber;
import com.lahutina.phonecontacts.service.impl.ExportImportServiceImpl;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExportImportServiceImplTest {

    @Mock
    private ContactService contactService;

    private ExportImportServiceImpl exportImportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exportImportService = new ExportImportServiceImpl(contactService);
    }

    @SneakyThrows
    @Test
    void testExportContacts() {
        List<Contact> contacts = Arrays.asList(
                createContact("John Doe", "john@example.com", "1234567890"),
                createContact("Jane Smith", "jane@example.com", "9876543210")
        );
        when(contactService.getAll()).thenReturn(contacts);

        InputStreamResource result = exportImportService.exportContacts();

        String expectedCsvData = "Name,Email,Phone\n" +
                "John Doe,john@example.com,1234567890\n" +
                "Jane Smith,jane@example.com,9876543210\n";
        assertEquals(expectedCsvData, readInputStreamToString(result.getInputStream()));
    }

    @Test
    void testImportContacts() throws IOException {
        String csvData = """
                Name,Email,Phone
                John Doe,john@example.com;john.doe@example.com,1234567890;5555555555
                Jane Smith,jane@example.com,9876543210""";
        MockMultipartFile file = new MockMultipartFile("file", "contacts.csv", "text/csv", csvData.getBytes());

        exportImportService.importContacts(file);
        System.out.println("great");

        Contact johnContact = createContact("John Doe", "john@example.com", "1234567890");
        Contact janeContact = createContact("Jane Smith", "jane@example.com", "9876543210");
        System.out.println("great");

        verify(contactService).create(johnContact);
        verify(contactService).create(janeContact);
    }

    private Contact createContact(String name, String emails, String phoneNumbers) {
        Contact contact = new Contact(1L, name, new HashSet<>(), new HashSet<>(), null);

        for (String emailStr : emails.split(";")) {
            Email emailObj = new Email(emailStr.trim());
            emailObj.setContact(contact);
            contact.getEmails().add(emailObj);
        }

        for (String phoneNumber : phoneNumbers.split(";")) {
            PhoneNumber phoneNumberObj = new PhoneNumber(phoneNumber.trim());
            phoneNumberObj.setContact(contact);
            contact.getPhones().add(phoneNumberObj);
        }

        return contact;
    }

    private String readInputStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }
}
