/**
 *
 * Copyright 2014 Vyacheslav Blinov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smackx.amp;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;

import java.util.Iterator;

/**
 * Manages AMP stanzas within messages. A AMPManager provides a high level access to
 * get and set AMP rules to messages.
 *
 * See http://xmpp.org/extensions/xep-0079.html for AMP extension details
 *
 * @author Vyacheslav Blinov
 */
public class AMPManager {


    // Enable the AMP support on every established connection
    // The ServiceDiscoveryManager class should have been already initialized
    static {
        Connection.addConnectionCreationListener(new ConnectionCreationListener() {
            public void connectionCreated(Connection connection) {
                AMPManager.setServiceEnabled(connection, true);
            }
        });
    }

    /**
     * Enables or disables the AMP support on a given connection.<p>
     *
     * Before starting to send AMP messages to a user, check that the user can handle XHTML
     * messages. Enable the AMP support to indicate that this client handles XHTML messages.
     *
     * @param connection the connection where the service will be enabled or disabled
     * @param enabled indicates if the service will be enabled or disabled
     */
    public synchronized static void setServiceEnabled(Connection connection, boolean enabled) {
        if (isServiceEnabled(connection) == enabled)
            return;

        if (enabled) {
            ServiceDiscoveryManager.getInstanceFor(connection).addFeature(AMPExtension.NAMESPACE);
        }
        else {
            ServiceDiscoveryManager.getInstanceFor(connection).removeFeature(AMPExtension.NAMESPACE);
        }
    }

    /**
     * Returns true if the AMP support is enabled for the given connection.
     *
     * @param connection the connection to look for AMP support
     * @return a boolean indicating if the AMP support is enabled for the given connection
     */
    public static boolean isServiceEnabled(Connection connection) {
        connection.getServiceName();
        return ServiceDiscoveryManager.getInstanceFor(connection).includesFeature(AMPExtension.NAMESPACE);
    }

    /**
     * Check if server supports specified action
     * @param connection active xmpp connection
     * @param action action to check
     * @return true if this action is supported.
     */
    public static boolean isActionSupported(Connection connection, AMPExtension.Action action) {
        String featureName = AMPExtension.NAMESPACE + "?action=" + action.toString();
        return isFeatureSupportedByServer(connection, featureName, AMPExtension.NAMESPACE);
    }

    /**
     * Check if server supports specified condition
     * @param connection active xmpp connection
     * @param conditionName name of condition to check
     * @return true if this condition is supported.
     * @see AMPDeliverCondition
     * @see AMPExpireAtCondition
     * @see AMPMatchResourceCondition
     */
    public static boolean isConditionSupported(Connection connection, String conditionName) {
        String featureName = AMPExtension.NAMESPACE + "?condition=" + conditionName;
        return isFeatureSupportedByServer(connection, featureName, AMPExtension.NAMESPACE);
    }

    private static boolean isFeatureSupportedByServer(Connection connection, String featureName, String node) {
        try {
            ServiceDiscoveryManager discoveryManager = ServiceDiscoveryManager.getInstanceFor(connection);
            DiscoverInfo info = discoveryManager.discoverInfo(connection.getServiceName(), node);
            Iterator<DiscoverInfo.Feature> it = info.getFeatures();
            while (it.hasNext()) {
                DiscoverInfo.Feature feature = it.next();
                if (featureName.equals(feature.getVar())) {
                    return true;
                }
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return false;
    }
}