package com.liangheee.message;

import lombok.Data;
import lombok.ToString;

import java.util.Set;

@Data
@ToString(callSuper = true)
public class GroupCreateRequestMessage extends Message {
    private String from;
    private String groupName;
    private Set<String> members;

    public GroupCreateRequestMessage(String from,String groupName, Set<String> members) {
        this.from = from;
        this.groupName = groupName;
        this.members = members;
    }

    @Override
    public int getMessageType() {
        return GroupCreateRequestMessage;
    }
}
