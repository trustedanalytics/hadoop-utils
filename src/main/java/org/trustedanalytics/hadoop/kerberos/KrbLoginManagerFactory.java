/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.hadoop.kerberos;

import com.google.common.annotations.VisibleForTesting;

public class KrbLoginManagerFactory {

    private static class Holder {
        static final KrbLoginManagerFactory factory = new KrbLoginManagerFactory();

        private Holder() {
        }
    }

    private volatile KrbLoginManager managerInstance = null;

    private String kdc;

    private String defaultRealm;

    @VisibleForTesting
    KrbLoginManagerFactory() {
    }

    public static KrbLoginManagerFactory getInstance() {
        return Holder.factory;
    }

    public KrbLoginManager getKrbLoginManagerInstance(String kdcParam, String defaultRealmParam) {
        if (managerInstance == null) {
            synchronized (this) {
                if (managerInstance == null) {
                    kdc = kdcParam;
                    defaultRealm = defaultRealmParam;
                    managerInstance = new HadoopKrbLoginManager(kdc, defaultRealm);
                }
            }
        }
        ensureSameInstanceRequested(kdcParam, defaultRealmParam);
        return managerInstance;
    }

    private void ensureSameInstanceRequested(String kdcParam, String defaultRealmParam) {
        if (!kdcParam.equals(kdc) || !defaultRealmParam.equals(defaultRealm))
            throw new IllegalArgumentException("Not implemented. This factory can not create new "
                    + "instance of KrbLoginManager. " + "KrbLoginManager(" + kdcParam + ","
                    + defaultRealmParam + ") requested, but " + "KrbLoginManager(" + kdc + ","
                    + defaultRealm + ") was previously created");
    }
}
