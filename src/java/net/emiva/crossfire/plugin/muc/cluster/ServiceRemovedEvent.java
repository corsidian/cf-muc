/**
 * $RCSfile$
 * $Revision: $
 * $Date: $
 *
 * Copyright (C) 2005-2008 Jive Software. All rights reserved.
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

package net.emiva.crossfire.plugin.muc.cluster;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.emiva.crossfire.XMPPServer;
import net.emiva.crossfire.plugin.muc.MUCPlugin;
import net.emiva.util.cache.ClusterTask;
import net.emiva.util.cache.ExternalizableUtil;

/**
 * Task that will remove a service from the cluster node. When a service is destroyed
 * in a cluster node the rest of the cluster nodes will need to destroy their copy.
 *
 * @author Daniel Henninger
 */
public class ServiceRemovedEvent implements ClusterTask {
    private String subdomain;

    public ServiceRemovedEvent() {
    }

    public ServiceRemovedEvent(String subdomain) {
        this.subdomain = subdomain;
    }

    public Object getResult() {
        return null;
    }

    public void run() {
        MUCPlugin plugin = (MUCPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("muc");
        plugin.getMultiUserChatManager().unregisterMultiUserChatService(subdomain);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        ExternalizableUtil.getInstance().writeSafeUTF(out, subdomain);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        subdomain = ExternalizableUtil.getInstance().readSafeUTF(in);
    }
}
