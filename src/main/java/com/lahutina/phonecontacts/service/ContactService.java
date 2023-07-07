package com.lahutina.phonecontacts.service;

import com.lahutina.phonecontacts.entity.Contact;

import java.util.List;

public interface ContactService {

    List<Contact> getAll();

    Contact getById(Long id);

    Contact create(Contact contact);

    Contact update(Long id, Contact contact);

    void delete(Long id);

}
