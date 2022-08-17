import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class JON {


    private JONObject rootCache;


    public String toJON(Object object) throws IllegalAccessException {

        rootCache = new JONObject(object);

        return toJON(rootCache);

    }

    private String toJON(JONObject jonObject) throws IllegalAccessException {

        StringBuilder str = new StringBuilder("{");
        Object object = jonObject.getSelf();
        Class<?> clazz = object.getClass();

        for (Field field : clazz.getDeclaredFields()) {

            if (str.length() > 1) {
                str.append(',');
            }

            field.setAccessible(true);

            //add name
            str.append(field.getName()).append(":");

            //add value
            String value = null;
            if (
                    String.class.equals(field.getType()) ||
                            Integer.class.equals(field.getType()) ||
                            Double.class.equals(field.getType()) ||
                            Float.class.equals(field.getType()) ||
                            Short.class.equals(field.getType()) ||
                            Byte.class.equals(field.getType()) ||
                            Long.class.equals(field.getType()) ||
                            Boolean.class.equals(field.getType()) || field.getType().isPrimitive())
            {
                value = String.valueOf(field.get(object));

            } else {

                JONObject newJon = new JONObject(field.get(object));

                String path = exists(newJon);

                if (path != null) {
                    value = path;
                } else {
                    jonObject.add(newJon);

                    value = toJON(newJon);
                }
            }

            if (value != null) {
                str.append(value);
            }

        }

        str.append("}");

        return str.toString();

    }

    private String exists(JONObject toBeChecked) {
        String str = "";
        return exists(str, rootCache, toBeChecked);
    }

    private String exists(String str, JONObject in, JONObject toBeChecked) {
        str += ">";
        if(in.getSelf().equals(toBeChecked.getSelf())) {
            return str;
        } else if(in.hasChildren()) {

            for(JONObject child : in.getChildren()) {
                String temp = exists(str, child, toBeChecked);
                if (temp != null) {
                    return temp;
                } else {
                    str += "<";
                }
            }
            return null;
        } else {
            return null;
        }
    }

    private JONObject rootJON = null;

    public <T> T fromJON(String jon, Class<T> clazz) throws IllegalAccessException, NoSuchFieldException, InstantiationException {
        return fromJON(jon, clazz, null);
    }

    private  <T> T fromJON(String jon, Class<T> clazz, JONObject parent) throws IllegalAccessException, NoSuchFieldException, InstantiationException {

        jon = jon.substring(1, jon.length()-1);

        ArrayList<String> params = new ArrayList<>(List.of(jon.split(",")));

        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);

        T object = clazz.cast(unsafe.allocateInstance(clazz));
        JONObject jonObject = new JONObject(object);

        if (parent == null) {
            rootJON = jonObject;
        } else {
            parent.add(jonObject);
        }

        for (int i = 0; i < params.size(); i++) {

            String[] args = params.get(i).split(":");

            String name = args[0];

            Field field = clazz.getDeclaredField(name);

            StringBuilder textValue = new StringBuilder(args[1]);

            args = null;

            if (textValue.charAt(0) == '{') {

                textValue = new StringBuilder();

                int open = 0;

                for (int j = i; j < params.size(); j++) {

                    String param = params.get(j);
                    params.remove(j);
                    j-=1;

                    boolean stop = false;
                    for (int k = 1; k < param.length(); k++) {

                        char character = param.charAt(k);

                        if (character == '{') {
                            open++;
                        } else if (character == '}') {
                            open--;
                            if (open == 0) {
                                stop = true;
                                break;
                            }
                        }
                    }

                    textValue.append(param).append(",");

                    if (stop) {
                        i-=1;
                        break;
                    }
                }

                //cut name of field off
                textValue = new StringBuilder(textValue.substring(textValue.toString().indexOf(':') + 1));
                //cut the last , off
                textValue = new StringBuilder(textValue.substring(0, textValue.length() - 1));

                field.setAccessible(true);

                Object value = fromJON(textValue.toString(), field.getType(), jonObject);

                if (Modifier.isFinal(field.getModifiers())) {
                    changeFinalField(object, field, value);
                } else {
                    field.set(object, value);
                }

            } else {

                field.setAccessible(true);

                Object value = textValue.toString();

                if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
                    value = Integer.parseInt(textValue.toString());
                } else if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
                    value = Double.parseDouble(textValue.toString());
                } else if (field.getType().equals(Float.class) || field.getType().equals(float.class)) {
                    value = Float.parseFloat(textValue.toString());
                } else if (field.getType().equals(Short.class) || field.getType().equals(short.class)) {
                    value = Short.parseShort(textValue.toString());
                } else if (field.getType().equals(Byte.class) || field.getType().equals(byte.class)) {
                    value = Byte.parseByte(textValue.toString());
                } else if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
                    value = Long.parseLong(textValue.toString());
                } else if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
                    value = Boolean.parseBoolean(textValue.toString());
                }

                if (value instanceof String v && v.matches("[<>]*?")){

                    JONObject last = new JONObject(null);
                    last.add(rootJON);
                    int index = 0;
                    for (int j = 0; j < v.length(); j++) {
                        char c = v.charAt(j);

                        if (c == '>') {
                            last = last.getChildren().get(index);
                        } else if (c == '<') {
                            index++;
                        }
                    }

                    if (Modifier.isFinal(field.getModifiers())) {
                        changeFinalField(object, field, last.getSelf());
                    } else {
                        field.set(object, last.getSelf());
                    }

                } else {

                    if (Modifier.isFinal(field.getModifiers())) {
                        changeFinalField(object, field, value);
                    } else {
                        field.set(object, value);
                    }

                }

            }


        }

        return object;


    }

    private void changeFinalField(Object object, Field field, Object value) throws NoSuchFieldException, IllegalAccessException {
        //get Unsafe object
        final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);

        //change value of field
        long fieldOffset = unsafe.objectFieldOffset(field);

        if (value.getClass().equals(Integer.class)) {
            unsafe.putInt(object, fieldOffset, (Integer) value);
        } else if (value.getClass().equals(Double.class)) {
            unsafe.putDouble(object, fieldOffset, (Double) value);
        } else if (value.getClass().equals(Float.class)) {
            unsafe.putFloat(object, fieldOffset, (Float) value);
        } else if (value.getClass().equals(Short.class)) {
            unsafe.putShort(object, fieldOffset, (Short) value);
        } else if (value.getClass().equals(Byte.class)) {
            unsafe.putByte(object, fieldOffset, (Byte) value);
        } else if (value.getClass().equals(Long.class)) {
            unsafe.putLong(object, fieldOffset, (Long) value);
        } else if (value.getClass().equals(Boolean.class)) {
            unsafe.putBoolean(object, fieldOffset, (Boolean) value);
        } else {
            unsafe.putObject(object, fieldOffset, value);
        }
    }

}
