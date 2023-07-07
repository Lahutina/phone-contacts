package com.lahutina.phonecontacts.service.impl;

import com.lahutina.phonecontacts.entity.Contact;

import com.lahutina.phonecontacts.entity.Email;
import com.lahutina.phonecontacts.entity.PhoneNumber;
import com.lahutina.phonecontacts.entity.User;
import com.lahutina.phonecontacts.exception.ContactNameExistsException;
import com.lahutina.phonecontacts.exception.ContactNotFoundException;
import com.lahutina.phonecontacts.exception.InvalidCredentialsException;
import com.lahutina.phonecontacts.exception.UserNotFoundException;
import com.lahutina.phonecontacts.repository.ContactRepository;
import com.lahutina.phonecontacts.repository.UserRepository;
import com.lahutina.phonecontacts.service.ContactService;
import com.lahutina.phonecontacts.service.ValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 *  Service implementation for managing contacts crud operation.
 */
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final ValidatorService validatorService;

    private User getAuthenticatedUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user != null) {
            user = userRepository.findByEmail(user.getEmail());
            return user;
        } else {
            throw new UserNotFoundException("User is not authenticated");
        }
    }

    @Override
    public List<Contact> getAll() {
        User authenticatedUser = getAuthenticatedUser();
        return authenticatedUser.getContacts();
    }

    @Override
    public Contact getById(Long id) {
        User authenticatedUser = getAuthenticatedUser();
        return authenticatedUser.getContacts()
                .stream()
                .filter(contact -> contact.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ContactNotFoundException("Contact not found"));
    }

    @Override
    public Contact create(Contact contact) {
        User authenticatedUser = getAuthenticatedUser();
        checkIfContactWithSameNameExists(authenticatedUser, contact);
        validateContactEmailsAndPhones(contact);

        contact.setUser(authenticatedUser);
        contact.getEmails().forEach(e -> e.setContact(contact));
        contact.getPhones().forEach(p -> p.setContact(contact));

        return contactRepository.save(contact);
    }

    @Override
    public Contact update(Long id, Contact contact) {
        User authenticatedUser = getAuthenticatedUser();
        checkIfContactWithSameNameExists(authenticatedUser, contact);

        Contact existingContact = authenticatedUser.getContacts()
                .stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ContactNotFoundException("Contact not found"));

        validateContactEmailsAndPhones(contact);

        contactRepository.delete(existingContact);

        contact.setId(existingContact.getId());
        contact.setUser(authenticatedUser);
        contact.getEmails().forEach(e -> e.setContact(contact));
        contact.getPhones().forEach(p -> p.setContact(contact));

        return contactRepository.save(contact);
    }

    @Override
    public void delete(Long id) {
        User authenticatedUser = getAuthenticatedUser();
        Contact contact = authenticatedUser.getContacts()
                .stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ContactNotFoundException("Contact not found"));

        contactRepository.delete(contact);
    }

    // Checks if a contact with the same name already exists for the user.
    private void checkIfContactWithSameNameExists(User authenticatedUser, Contact contact) {
        if (authenticatedUser.getContacts().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(contact.getName()))) {
            throw new ContactNameExistsException("Contact name already exists");
        }
    }

    // Validates the email and phone number of a contact.
    private void validateContactEmailsAndPhones(Contact contact) {
        for (Email email : contact.getEmails()) {
            if (!validatorService.validateEmail(email.getEmail())) {
                throw new InvalidCredentialsException("Invalid email " + email.getEmail());
            }
        }

        for (PhoneNumber phoneNumber : contact.getPhones()) {
            if (!validatorService.validatePhoneNumber(phoneNumber.getNumber())) {
                throw new InvalidCredentialsException("Invalid phone number: " + phoneNumber.getNumber());
            }
        }
    }
}
