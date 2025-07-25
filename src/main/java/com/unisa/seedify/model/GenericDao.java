package com.unisa.seedify.model;

import java.sql.SQLException;

public interface GenericDao<T> {
    void doSave(T bean) throws SQLException;

    // TODO add 'void doDelete(EntityPrimaryKey primaryKey) throws SQLException';
    void doDelete(T bean) throws SQLException;

    void doUpdate(T bean) throws SQLException;

    T doRetrive(EntityPrimaryKey primaryKey) throws SQLException;
}
