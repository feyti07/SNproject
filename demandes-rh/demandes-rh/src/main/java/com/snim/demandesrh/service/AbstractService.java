package com.snim.demandesrh.service;

import java.util.List;

public interface AbstractService<T> {

    long save(T dto);

    List<T> findAll();

    T findById(Integer id);

    void delete(Integer id);



}

