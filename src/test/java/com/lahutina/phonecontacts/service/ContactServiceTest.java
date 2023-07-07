package com.lahutina.phonecontacts.service;

import com.lahutina.phonecontacts.entity.Contact;
import com.lahutina.phonecontacts.entity.Email;
import com.lahutina.phonecontacts.entity.PhoneNumber;
import com.lahutina.phonecontacts.entity.User;
import com.lahutina.phonecontacts.exception.ContactNotFoundException;
import com.lahutina.phonecontacts.exception.InvalidCredentialsException;
import com.lahutina.phonecontacts.repository.ContactRepository;
import com.lahutina.phonecontacts.repository.UserRepository;
import com.lahutina.phonecontacts.service.impl.ContactServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidatorService validatorService;

    @Mock
    private ContactService contactService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contactService = new ContactServiceImpl(contactRepository, userRepository, validatorService);

        user = new User(1L, "oksankalahutina@gmail.com", "password", null);
        user.setContacts(new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
        when(userRepository.findByEmail(anyString())).thenReturn(user);
    }

    @Test
    void testGetAll() {
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact(1L, "John Doe", new HashSet<>(), new HashSet<>(), user));
        contacts.add(new Contact(2L, "Jane Smith", new HashSet<>(), new HashSet<>(), user));
        user.setContacts(contacts);

        List<Contact> result = contactService.getAll();

        assertEquals(contacts, result);
    }

    @Test
    void testGetById() {
        Contact contact = new Contact(1L, "John Doe", new HashSet<>(), new HashSet<>(), user);
        user.getContacts().add(contact);

        Contact result = contactService.getById(1L);

        assertEquals(contact, result);
    }

    @Test
    void testGetById_ContactNotFound() {
        assertThrows(ContactNotFoundException.class, () -> contactService.getById(1L));
    }


    @Test
    void testCreate() {
        when(validatorService.validateEmail(any(String.class))).thenReturn(true);
        when(validatorService.validatePhoneNumber(any(String.class))).thenReturn(true);

        Contact contact = new Contact(1L, "John Doe", new HashSet<>(), new HashSet<>(), user);
        Email email = new Email(1L, "oksankalahutina@gmail.com", contact);
        PhoneNumber phoneNumber = new PhoneNumber(1L, "+380939333335", contact);
        contact.getEmails().add(email);
        contact.getPhones().add(phoneNumber);

        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        Contact result = contactService.create(contact);

        verify(contactRepository).save(contact);

        assertNotNull(result);
        assertEquals(contact, result);
        assertEquals(user, result.getUser());
    }

    @Test
    void testCreate_InvalidPhone() {
        when(validatorService.validateEmail(any(String.class))).thenReturn(true);
        when(validatorService.validatePhoneNumber(any(String.class))).thenReturn(false);

        Contact contact = new Contact(1L, "John Doe", new HashSet<>(), new HashSet<>(), user);
        Email email = new Email(1L, "oksankalahutina@gmail.com", contact);
        PhoneNumber phoneNumber = new PhoneNumber(1L, "+380939333335", contact);
        contact.getEmails().add(email);
        contact.getPhones().add(phoneNumber);

        assertThrows(InvalidCredentialsException.class, () -> contactService.create(contact));
    }

    @Test
    void testCreate_InvalidEmail() {
        when(validatorService.validateEmail(anyString())).thenReturn(false);
        when(validatorService.validatePhoneNumber(anyString())).thenReturn(true);

        Contact contact = new Contact(1L, "John Doe", new HashSet<>(), new HashSet<>(), user);
        Email email = new Email(1L, "notValid", contact);
        PhoneNumber phoneNumber = new PhoneNumber(1L, "1545", contact);
        contact.getEmails().add(email);
        contact.getPhones().add(phoneNumber);

        assertThrows(InvalidCredentialsException.class, () -> contactService.create(contact));
    }


    @Test
    void testUpdate() {
        Contact existingContact = new Contact(1L, "John Doe", new HashSet<>(), new HashSet<>(), user);
        user.getContacts().add(existingContact);
        Contact updatedContact = new Contact(1L, "Jane Smith", new HashSet<>(), new HashSet<>(), user);

        when(contactRepository.save(any(Contact.class))).thenReturn(updatedContact);

        Contact result = contactService.update(1L, updatedContact);

        verify(contactRepository).save(updatedContact);

        assertNotNull(result);
        assertEquals(updatedContact, result);
        assertEquals(user, result.getUser());
    }

    @Test
    void testDelete() {
        Contact contactToDelete = new Contact(1L, "John Doe", new HashSet<>(), new HashSet<>(), user);
        user.getContacts().add(contactToDelete);

        doNothing().when(contactRepository).delete(contactToDelete);
        contactService.delete(1L);

        verify(contactRepository).delete(contactToDelete);
    }
}
