package com.lahutina.phonecontacts.controller;

import com.lahutina.phonecontacts.entity.Contact;
import com.lahutina.phonecontacts.entity.Email;
import com.lahutina.phonecontacts.entity.PhoneNumber;
import com.lahutina.phonecontacts.entity.User;
import com.lahutina.phonecontacts.service.ContactService;
import com.lahutina.phonecontacts.service.ExportImportService;
import com.lahutina.phonecontacts.service.JwtService;
import com.lahutina.phonecontacts.service.impl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
public class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;
    @MockBean
    private ExportImportService exportImportService;

    private List<Contact> contactList;
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User(1L, "oksankalahutina@gmail.com", "password", null);
        Contact contact1 = new Contact(1L, "John Doe", Collections.singleton(new Email(1L, "john.doe@example.com", null)), Collections.singleton(new PhoneNumber(1L, "1234567890", null)), user);
        Contact contact2 = new Contact(2L, "Jane Smith", Collections.singleton(new Email(2L, "jane.smith@example.com", null)), Collections.singleton(new PhoneNumber(2L, "9876543210", null)), user);
        user.setContacts(Arrays.asList(contact1, contact2));
        contactList = Arrays.asList(contact1, contact2);
    }


    @Test
    @WithMockUser
    public void testGetAllContacts() throws Exception {
        when(contactService.getAll()).thenReturn(contactList);

        mockMvc.perform(MockMvcRequestBuilders.get("/contacts")).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(contactList.size()));
    }


    @Test
    @WithMockUser
    public void testGetContactById() throws Exception {
        Long contactId = 1L;
        Contact contact = contactList.get(0);
        when(contactService.getById(eq(contactId))).thenReturn(contact);

        mockMvc.perform(MockMvcRequestBuilders.get("/contacts/{id}", contactId)).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.id").value(contact.getId())).andExpect(MockMvcResultMatchers.jsonPath("$.name").value(contact.getName())).andExpect(MockMvcResultMatchers.jsonPath("$.emails[0].email").value(contact.getEmails().iterator().next().getEmail())).andExpect(MockMvcResultMatchers.jsonPath("$.phones[0].number").value(contact.getPhones().iterator().next().getNumber()));
    }

    @Test
    @WithMockUser
    public void testCreateContact() throws Exception {
        Contact contact = new Contact(1L, "New Contact", Collections.singleton(new Email(1L, "xxxi@gmail.com", null)), Collections.singleton(new PhoneNumber(1L, "+380939333335", null)), user);
        when(contactService.create(any(Contact.class))).thenReturn(contact);

        String requestBody = "{\"name\":\"New Contact\",\"emails\":[\"xxxi@gmail.com\"],\"phones\":[\"+380939333335\"]}";

        mockMvc.perform(MockMvcRequestBuilders.post("/contacts").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.id").value(contact.getId())).andExpect(MockMvcResultMatchers.jsonPath("$.name").value(contact.getName())).andExpect(MockMvcResultMatchers.jsonPath("$.emails[0].email").value(contact.getEmails().iterator().next().getEmail())).andExpect(MockMvcResultMatchers.jsonPath("$.phones[0].number").value(contact.getPhones().iterator().next().getNumber()));
    }


    @Test
    @WithMockUser
    public void testUpdateContact() throws Exception {
        Long contactId = 2L;
        Contact updatedContact = new Contact(contactId, "Updated Contact", Collections.singleton(new Email(2L, "updated.contact@example.com", null)), Collections.singleton(new PhoneNumber(2L, "+380685644218", null)), user);
        when(contactService.update(eq(contactId), any(Contact.class))).thenReturn(updatedContact);

        mockMvc.perform(MockMvcRequestBuilders.put("/contacts/{id}", contactId).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Updated Contact\",\"emails\":[{\"email\":\"updated.contact@example.com\"}],\"phoneNumbers\":[{\"phoneNumber\":\"+380685644218\"}]}")).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.id").value(updatedContact.getId())).andExpect(MockMvcResultMatchers.jsonPath("$.name").value(updatedContact.getName())).andExpect(MockMvcResultMatchers.jsonPath("$.emails[0].email").value(updatedContact.getEmails().iterator().next().getEmail())).andExpect(MockMvcResultMatchers.jsonPath("$.phones[0].number").value(updatedContact.getPhones().iterator().next().getNumber()));
    }

    @Test
    @WithMockUser
    public void testDeleteContact() throws Exception {
        Long contactId = 1L;

        doNothing().when(contactService).delete(eq(contactId));

        mockMvc.perform(MockMvcRequestBuilders.delete("/contacts/{id}", contactId).with(csrf())).andExpect(status().isNoContent());
    }


    @Test
    @WithMockUser
    public void testExportContacts() throws Exception {
        byte[] csvData = "Name,Email,Phone\nxxxtest,testuser@gmail.com;testuser1@gmail.com,+380939333335;+380939333336;+380939333337\n".getBytes();
        InputStreamResource csvContent = new InputStreamResource(new ByteArrayInputStream(csvData));
        when(exportImportService.exportContacts()).thenReturn(csvContent);

        mockMvc.perform(MockMvcRequestBuilders.get("/contacts/export")).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().contentType("text/csv")).andExpect(MockMvcResultMatchers.header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.csv")).andExpect(MockMvcResultMatchers.content().bytes(csvData));
    }

    @Test
    @WithMockUser
    public void testImportContacts() throws Exception {
        String csvContent = "Name,Email,Phone\nxxxtest,testuser@gmail.com;testuser1@gmail.com,+380939333335;+380939333336;+380939333337\n";
        MockMultipartFile csvFile = new MockMultipartFile("file", "contacts.csv", "text/csv", csvContent.getBytes());

        doNothing().when(exportImportService).importContacts(eq(csvFile));

        mockMvc.perform(MockMvcRequestBuilders.multipart("/contacts/import").file(csvFile).with(csrf())).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().string("Contacts imported successfully."));
    }
}