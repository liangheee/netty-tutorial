package com.liangheee.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class GroupQuitResponseMessage extends AbstractResponseMessage {
    private String from;
    private String content;

    public GroupQuitResponseMessage(boolean success, String reason) {
        super(success, reason);
    }

    public GroupQuitResponseMessage(String from, String content) {
        this.from = from;
        this.content = content;
    }

    @Override
    public int getMessageType() {
        return GroupQuitResponseMessage;
    }
}
