/**
 * $RCSfile: $
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

import net.emiva.crossfire.plugin.muc.spi.LocalMUCRoom;
import net.emiva.util.cache.ExternalizableUtil;


/**
 * Task that adds a new member to the room in the other cluster nodes.
 *
 * @author Gaston Dombiak
 */
public class AddMember extends MUCRoomTask {
    private String bareJID;
    private String nickname;

    public AddMember() {
        super();
    }

    public AddMember(LocalMUCRoom room, String bareJID, String nickname) {
        super(room);
        this.bareJID = bareJID;
        this.nickname = nickname;
    }

    public String getBareJID() {
        return bareJID;
    }

    public String getNickname() {
        return nickname;
    }

    public Object getResult() {
        return null;
    }

    public void run() {
        // Execute the operation considering that we may still be joining the cluster
        execute(new Runnable() {
            public void run() {
                getRoom().memberAdded(AddMember.this);
            }
        });
    }

    @Override
	public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        ExternalizableUtil.getInstance().writeSafeUTF(out, bareJID);
        ExternalizableUtil.getInstance().writeSafeUTF(out, nickname);
    }

    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        bareJID = ExternalizableUtil.getInstance().readSafeUTF(in);
        nickname = ExternalizableUtil.getInstance().readSafeUTF(in);
    }
}
