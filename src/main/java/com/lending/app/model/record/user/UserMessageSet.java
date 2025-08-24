package com.lending.app.model.record.user;

import java.util.Set;

public record UserMessageSet(
        Set<UserMessage> users
) {
}
