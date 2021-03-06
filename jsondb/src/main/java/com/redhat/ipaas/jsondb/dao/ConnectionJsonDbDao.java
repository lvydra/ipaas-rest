/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.jsondb.dao;

import com.redhat.ipaas.dao.ConnectionDao;
import com.redhat.ipaas.jsondb.JsonDB;
import com.redhat.ipaas.model.connection.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Service
@Configuration
@ConditionalOnProperty(value = "dao.kind", havingValue = "jsondb")
public class ConnectionJsonDbDao extends JsonDbDao<Connection> implements ConnectionDao {

    @Autowired
    public ConnectionJsonDbDao(JsonDB jsondb) {
        super(jsondb);
    }

    @Override
    public Class<Connection> getType() {
        return Connection.class;
    }

    @Override
    public String getCollectionPath() {
        return "/connections";
    }

}
