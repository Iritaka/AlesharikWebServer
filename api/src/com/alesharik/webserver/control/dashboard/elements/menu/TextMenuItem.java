package com.alesharik.webserver.control.dashboard.elements.menu;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import one.nio.lock.RWLock;

import javax.annotation.concurrent.ThreadSafe;

/**
 * This class represent text item with link on content
 */
@ThreadSafe
public final class TextMenuItem extends MenuItem {
    private static final String TEXT_ITEM_TYPE = "text";

    private final RWLock lock;
    private String contentId;

    public TextMenuItem(String fa, String text) {
        super(fa, text, TEXT_ITEM_TYPE);
        lock = new RWLock();
    }

    @Override
    public void serialize(JsonObject object, JsonSerializationContext context) {
        try {
            lock.lockRead();
            object.add("contentId", context.serialize(this.contentId));
        } finally {
            lock.unlockRead();
        }
    }

    public TextMenuItem setContentId(String contentId) {
        try {
            lock.lockWrite();
            this.contentId = contentId;
            return this;
        } finally {
            lock.unlockWrite();
        }
    }

    public String getContentId() {
        try {
            lock.lockRead();
            return contentId;
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof TextMenuItem)) return false;
        if(!super.equals(o)) return false;

        TextMenuItem that = (TextMenuItem) o;

        return getContentId() != null ? getContentId().equals(that.getContentId()) : that.getContentId() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getContentId() != null ? getContentId().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TextMenuItem{" +
                "type='" + getType() + '\'' +
                ", fa='" + getFa() + '\'' +
                ", text='" + getText() + '\'' +
                ", contentId='" + getContentId() + '\'' +
                '}';
    }
}