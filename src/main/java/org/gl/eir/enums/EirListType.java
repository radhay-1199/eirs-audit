package org.gl.eir.enums;

import lombok.Getter;

@Getter
public enum EirListType {
    TRACKED_LIST(ListName.TRACKEDLIST),
    BLOCKED_LIST(ListName.BLOCKEDLIST),
    EXCEPTION_LIST(ListName.EXCEPTIONLIST),
    BLOCKED_TAC(ListName.BLOCKEDTACLIST),
    ALLOWED_TAC(ListName.ALLOWEDTACLIST);

    private final ListName correspondingList;

    EirListType(ListName correspondingList) {
        this.correspondingList = correspondingList;
    }

    public static EirListType fromListName(ListName listName) {
        for (EirListType eirListType : values()) {
            if (eirListType.correspondingList == listName) {
                return eirListType;
            }
        }
        throw new IllegalArgumentException("No corresponding EirListType found for ListName: " + listName);
    }
}
