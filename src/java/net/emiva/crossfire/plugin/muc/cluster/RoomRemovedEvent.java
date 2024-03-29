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

import net.emiva.crossfire.plugin.muc.MultiUserChatService;
import net.emiva.crossfire.plugin.muc.spi.LocalMUCRoom;
import net.emiva.util.cache.ClusterTask;


/**
 * Task that will remove a local room from the cluster node. When a room is destroyed
 * in a cluster node the rest of the cluster nodes will need to destroy their copy
 * and send notifications to the room occupants hosted in the local cluster node.
 *
 * @author Gaston Dombiak
 */
public class RoomRemovedEvent implements ClusterTask {
    private LocalMUCRoom room;

    public RoomRemovedEvent() {
    }

    public RoomRemovedEvent(LocalMUCRoom room) {
        this.room = room;
    }

    public Object getResult() {
        return null;
    }

    public void run() {
        MultiUserChatService mucService = room.getMUCService();
        mucService.chatRoomRemoved(room);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        room.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        room = new LocalMUCRoom();
        room.readExternal(in);
    }
}
