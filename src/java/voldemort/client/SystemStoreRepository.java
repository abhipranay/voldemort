/*
 * Copyright 2012-2013 LinkedIn, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package voldemort.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import voldemort.cluster.failuredetector.FailureDetector;
import voldemort.store.system.SystemStoreConstants;

/**
 * A repository that creates and maintains all the system stores in one place.
 * The purpose is to act as a source of truth for all the system stores, since
 * they can be recreated dynamically (in case cluster.xml changes).
 */

public class SystemStoreRepository {

    private Map<String, SystemStoreClient> sysStoreMap;
    private final SystemStoreClientFactory systemStoreFactory;

    public SystemStoreRepository(ClientConfig clientConfig) {
        sysStoreMap = new ConcurrentHashMap<String, SystemStoreClient>();
        this.systemStoreFactory = new SystemStoreClientFactory(clientConfig);
    }

    public void addSystemStore(SystemStoreClient newSysStore, String storeName) {
        this.sysStoreMap.put(storeName, newSysStore);
    }

    public void createSystemStores(ClientConfig config, String clusterXml, FailureDetector fd) {
        for(SystemStoreConstants.SystemStoreName storeName: SystemStoreConstants.SystemStoreName.values()) {
            SystemStoreClient sysStore = this.systemStoreFactory.createSystemStore(storeName.name(),
                                                                                   clusterXml,
                                                                                   fd);
            this.sysStoreMap.put(storeName.name(), sysStore);
        }
    }

    public SystemStoreClient<String, String> getClientRegistryStore() {
        String name = SystemStoreConstants.SystemStoreName.voldsys$_client_registry.name();
        SystemStoreClient<String, String> sysRegistryStore = sysStoreMap.get(name);
        return sysRegistryStore;
    }

    public SystemStoreClient<String, String> getMetadataVersionStore() {
        String name = SystemStoreConstants.SystemStoreName.voldsys$_metadata_version_persistence.name();
        SystemStoreClient<String, String> sysVersionStore = sysStoreMap.get(name);
        return sysVersionStore;
    }

    public void close() {
        this.systemStoreFactory.close();
    }
}
