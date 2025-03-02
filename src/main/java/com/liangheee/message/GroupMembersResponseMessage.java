package com.liangheee.message;

import lombok.Data;
import lombok.ToString;

import java.util.Set;

@Data
@ToString(callSuper = true)
public class GroupMembersResponseMessage extends AbstractResponseMessage {

    private Set<String> members;

    public GroupMembersResponseMessage(boolean success, String message) {
        super(success,message);
    }

    public GroupMembersResponseMessage(Set<String> members) {
        this.members = members;
    }

    @Override
    public int getMessageType() {
        return GroupMembersResponseMessage;
    }
}
