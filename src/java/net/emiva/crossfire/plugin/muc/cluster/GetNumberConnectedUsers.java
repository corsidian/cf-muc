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
import net.emiva.crossfire.plugin.muc.MultiUserChatService;
import net.emiva.util.cache.ClusterTask;


/**
 * Task to be executed in each cluster node to obtain the total number of
 * users using the multi user chat service.
 *
 * @author Gaston Dombiak
 */
public class GetNumberConnectedUsers implements ClusterTask{
    private Integer count;

    public Object getResult() {
        return count;
    }

    public void run() {
        count = 0;
        MUCPlugin plugin = (MUCPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("muc");
        for (MultiUserChatService mucService : plugin.getMultiUserChatManager().getMultiUserChatServices()) {
            count += mucService.getNumberConnectedUsers(true);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // Do nothing
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // Do nothing
    }
}
