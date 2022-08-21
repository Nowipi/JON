package io.github.nowipi.JON;

import java.util.ArrayList;

public class JONObject {

    private Object self;
    private final ArrayList<JONObject> children = new ArrayList<>();

    public JONObject(Object self) {
        this.self = self;
    }

    public JONObject add(JONObject jonObject) {
        children.add(jonObject);
        return jonObject;
    }

    public boolean hasSelf() {
        return self != null;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public Object getSelf() {
        return self;
    }

    public ArrayList<JONObject> getChildren() {
        return children;
    }

    private JONObject getChild(Object value) {
        for (JONObject jonObject : children) {
            if (jonObject.getSelf().equals(value)) {
                return jonObject;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JONObject jonObject))
            return false;

        if (!hasSelf() || !self.equals(jonObject.self)) {
            return false;
        }

        for (int i = 0; i < children.size(); i++) {
            if (!children.get(i).equals(jonObject.getChildren().get(i))) {
                return false;
            }
        }

        return true;
    }
}
