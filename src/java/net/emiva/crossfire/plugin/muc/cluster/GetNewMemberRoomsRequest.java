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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.emiva.crossfire.XMPPServer;
import net.emiva.crossfire.plugin.muc.MUCPlugin;
import net.emiva.crossfire.plugin.muc.MUCRole;
import net.emiva.crossfire.plugin.muc.MUCRoom;
import net.emiva.crossfire.plugin.muc.MultiUserChatService;
import net.emiva.crossfire.plugin.muc.spi.LocalMUCRoom;
import net.emiva.util.cache.ClusterTask;


/**
 * Task requested by each cluster node when a new node joins the cluster. Each existing cluster
 * node will request the list of rooms with occupants <tt>hosted by</tt> the new cluster node.
 *
 * @author Gaston Dombiak
 */
public class GetNewMemberRoomsRequest implements ClusterTask {
    private List<RoomInfo> rooms;

    public GetNewMemberRoomsRequest() {
    }

    public Object getResult() {
        return rooms;
    }

    public void run() {
        rooms = new ArrayList<RoomInfo>();

        MUCPlugin plugin = (MUCPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("muc");
        // Get all services that have local occupants and include them in the reply
        for (MultiUserChatService mucService : plugin.getMultiUserChatManager().getMultiUserChatServices()) {
            // Get rooms that have local occupants and include them in the reply
            for (MUCRoom room : mucService.getChatRooms()) {
                LocalMUCRoom localRoom = (LocalMUCRoom) room;
                Collection<MUCRole> localOccupants = new ArrayList<MUCRole>();
                for (MUCRole occupant : room.getOccupants()) {
                    if (occupant.isLocal()) {
                        localOccupants.add(occupant);
                    }
                }
                if (!localOccupants.isEmpty()) {
                    rooms.add(new RoomInfo(localRoom, localOccupants));
                }
            }
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // Do nothing
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // Do nothing
    }
}
