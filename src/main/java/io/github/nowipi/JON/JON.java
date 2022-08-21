package io.github.nowipi.JON;

import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class JON {


    private JONObject toPathCache = null;
    private JONObject fromPathCache = null;

    private String arrayToJON(Object[] array, JONObject jonObject) {
        String str = "[";
        for (Object item : array) {
            str += toJON(item, jonObject) + ',';
        }
        str = JONUtils.replaceLast(',', str);
        str += ']';
        return str;
    }

    private String exists(JONObject toBeChecked) {
        if (toPathCache == null)
            return null;
        return exists("", toPathCache, toBeChecked);
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

    public String toJON(Object object) {
        return toJON(object, null);
    }
    private String toJON(Object object, JONObject parent) {

        if (object == null) {
            return "null";
        }

        Class<?> clazz = object.getClass();

        if (clazz.isEnum() || clazz.isRecord() || clazz.isAnnotation()) {
            return null;
        }

        JONObject jonObject = new JONObject(object);

        String path = exists(jonObject);

        if (path != null) {
            return path;
        }

        if (parent == null) {
            toPathCache = jonObject;
            parent = toPathCache;
        } else {
            parent.add(jonObject);
        }

        //PRIMITIVE OR STRING
        if (JONUtils.isPrimitive(clazz) || CharSequence.class.isAssignableFrom(clazz)) {
            return String.valueOf(object);
            //IMPLEMENTS LIST
        }  else if (List.class.isAssignableFrom(clazz)) {
            Object[] array = ((List<?>) object).toArray();
            return arrayToJON(array, jonObject);
            //ARRAY
        } else if (clazz.isArray()) {
            Object[] array;
            if (object instanceof int[] a) {
                array = JONUtils.toWrapperArray(a);
            } else if (object instanceof double[] a) {
                array = JONUtils.toWrapperArray(a);
            } else if (object instanceof float[] a) {
                array = JONUtils.toWrapperArray(a);
            } else if (object instanceof short[] a) {
                array = JONUtils.toWrapperArray(a);
            } else if (object instanceof byte[] a) {
                array = JONUtils.toWrapperArray(a);
            } else if (object instanceof long[] a) {
                array = JONUtils.toWrapperArray(a);
            } else if (object instanceof boolean[] a) {
                array = JONUtils.toWrapperArray(a);
            } else if (object instanceof char[] a) {
                array = JONUtils.toWrapperArray(a);
            } else {
                array = (Object[]) object;
            }
            return arrayToJON(array, jonObject);
            //CLASS
        } else {
            String str = "{";
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    str += field.getName() + ':' + toJON(field.get(object), jonObject) + ',';
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            str = str.substring(0, str.length()-1);
            str += '}';
            return str;
        }

    }

    public  <T> T fromJON(String jon, Class<T> clazz) {
        return fromJON(jon, clazz, null);
    }

    private <T> T fromJON(String jon, Class<T> clazz, JONObject parent) {


        if (jon.equals("null")) {
            return null;
        }

        char firstCharacter = jon.charAt(0);

        switch (firstCharacter) {
            case '{' -> {
                return fromClassJON(jon, clazz, parent);
            }
            case '<', '>' -> {
                return fromPathJON(jon, clazz);
            }
            default -> {
                return fromPrimitiveJON(jon, clazz, parent);
            }
        }
    }

    private  <T> T fromJON(String jon, Class<T> clazz, Type type, JONObject parent) {

        if (jon.equals("null")) {
            return null;
        }

        char firstCharacter = jon.charAt(0);

        switch (firstCharacter) {
            case '[' -> {
                return fromArrayJON(jon, clazz, type, parent);
            }
            default -> {
                if (type instanceof Class<?> typeClass) {
                    return (T) fromJON(jon, typeClass, parent);
                } else if (type == null) {
                    return fromJON(jon, clazz, parent);
                }
            }
        }
        return null;
    }
    private  <T> T fromClassJON(String jon, Class<T> clazz, JONObject parent) {
        if (jon.charAt(0) == '{' && jon.charAt(jon.length()-1) == '}') {

            jon = jon.substring(1, jon.length()-1);

            try {

                Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                Unsafe unsafe = (Unsafe) unsafeField.get(null);

                T object = clazz.cast(unsafe.allocateInstance(clazz));

                JONObject jonObject = new JONObject(object);
                if (parent == null) {
                    fromPathCache = jonObject;
                } else {
                    parent.add(jonObject);
                }

                ArrayList<String> attribs = new ArrayList<>(List.of(jon.split(",")));

                for (int i = 0; i < attribs.size(); i++) {
                    String name = attribs.get(i).split(":")[0];
                    String val = getAttribValue(i, attribs);
                    if (val.contains("{") | val.contains("[")) {
                        i--;
                    }
                    Field field = clazz.getDeclaredField(name);
                    field.setAccessible(true);
                    if (List.class.isAssignableFrom(field.getType())) {
                        setField(object, field, fromJON(val, field.getType(), field.getGenericType(), jonObject));
                    } else if (field.getType().isArray()) {
                        setField(object, field, fromJON(val, field.getType(), field.getType().arrayType(), jonObject));
                    } else {
                        setField(object, field, fromJON(val, field.getType(), jonObject));
                    }
                }

                return object;

            } catch (NoSuchFieldException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        } else {
            System.err.println("Not a valid class JON (" + jon + ")");
            return null;
        }
    }

    private <T, B> T fromArrayJON(String jon, Class<T> arrayClass,Type type, JONObject parent) {

        jon = jon.substring(1, jon.length()-1);

        if (List.class.isAssignableFrom(arrayClass)) {

            List<B> array = new ArrayList<>();

            JONObject jonObject = new JONObject(array);
            if (parent == null) {
                fromPathCache = jonObject;
            } else {
                parent.add(jonObject);
            }

            ArrayList<String> elements = new ArrayList<>(List.of(jon.split(",")));
            for (int i = 0; i < elements.size(); i++) {
                String element = getAttribValue(i, elements);
                if (element.contains("{") | element.contains("[")) {
                    i--;
                }

                if (type instanceof ParameterizedType parameterizedType) {
                    Object o = fromJON(element, (Class<?>)parameterizedType.getRawType(), parameterizedType.getActualTypeArguments()[0], jonObject);
                    array.add((B) o);
                }

            }
            return (T) array;
        } else if (arrayClass.isArray()) {

            List<T> array = new ArrayList<>();

            JONObject jonObject = new JONObject(array);
            if (parent == null) {
                fromPathCache = jonObject;
            } else {
                parent.add(jonObject);
            }

            ArrayList<String> elements = new ArrayList<>(List.of(jon.split(",")));
            for (int i = 0; i < elements.size(); i++) {
                String element = getAttribValue(i, elements);
                if (element.contains("{") | element.contains("[")) {
                    i--;
                }

                T o = (T)fromJON(element, arrayClass.getComponentType(), jonObject);
                array.add(o);

            }

            T a = (T) Array.newInstance(arrayClass.getComponentType(), array.size());
            for (int i = 0; i < array.size(); i++) {
                Array.set(a, i, array.get(i));
            }
            return a;
        }
        return null;
    }

    private <T> T fromPrimitiveJON(String jon, Class<T> clazz, JONObject parent) {
        if (JONUtils.isPrimitive(clazz) || CharSequence.class.isAssignableFrom(clazz)) {
            Object val = jon;
            JONObject jonObject = new JONObject(val);
            if (parent == null) {
                fromPathCache = jonObject;
            } else {
                parent.add(jonObject);
            }
            if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
                val = Integer.parseInt(jon);
            } else if (clazz.equals(double.class) || clazz.equals(Double.class)) {
                val = Double.parseDouble(jon);
            } else if (clazz.equals(float.class) || clazz.equals(Float.class)) {
                val = Float.parseFloat(jon);
            } else if (clazz.equals(short.class) || clazz.equals(Short.class)) {
                val = Short.parseShort(jon);
            } else if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
                val = Byte.parseByte(jon);
            } else if (clazz.equals(long.class) || clazz.equals(Long.class)) {
                val = Long.parseLong(jon);
            } else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
                val = Boolean.parseBoolean(jon);
            } else if (clazz.equals(char.class) || clazz.equals(Character.class)) {
                val = jon.charAt(0);
            }
            return (T) val;
        } else {
            System.err.println("Not a valid primitive or String JON (" + jon + ") of " + clazz);
            return null;
        }
    }

    private <T> T fromPathJON(String jon, Class<T> clazz) {
        if (jon.matches("[<>]*?")){

            JONObject last = new JONObject(null);
            last.add(fromPathCache);
            int index = 0;
            for (int j = 0; j < jon.length(); j++) {
                char c = jon.charAt(j);

                if (c == '>') {
                    last = last.getChildren().get(index);
                    index = 0;
                } else if (c == '<') {
                    index++;
                }
            }
            return (T) last.getSelf();
        } else {
            System.err.println("Not a valid path JON (" + jon + ")");
            return null;
        }
    }

    private String getAttribValue(int attribIndex, ArrayList<String> attribs) {
        String attrib = attribs.get(attribIndex);

        String attribValue;
        int firstColon = attrib.indexOf(":");
        if (firstColon != -1 && attrib.charAt(0) != '{') {
            attribValue = attrib.substring(attrib.indexOf(":")+1);
        } else {
            attribValue = attrib;
        }
        String value = "";
        if (attribValue.charAt(0) == '{' || attribValue.charAt(0) == '[') {

            int classOpen = 0, arrayOpen = 0;

            for (int i = attribIndex; i < attribs.size(); i++) {
                attrib = attribs.get(i);
                if (value.equals("")) {
                    value += attribValue + ",";
                } else {
                    value += attrib + ",";
                }
                attribs.remove(i);
                i--;

                boolean stop = false;
                A:
                for (int j = 0; j < attrib.length(); j++) {
                    char character = attrib.charAt(j);
                    switch (character) {
                        case '{' -> {
                            classOpen++;
                        }
                        case '[' -> {
                            arrayOpen++;
                        }
                        case '}' -> {
                            classOpen--;
                            if (classOpen == 0 && arrayOpen == 0) {
                                stop = true;
                                break A;
                            }
                        }
                        case ']' -> {
                            arrayOpen--;
                            if (classOpen == 0 && arrayOpen == 0) {
                                stop = true;
                                break A;
                            }
                        }
                    }
                }

                if (stop) {
                    break;
                }
            }

        } else {
            return attribValue;
        }
        int lastComma = value.lastIndexOf(",");
        if (lastComma != -1) {
            value = value.substring(0, lastComma);
        }
        return value;
    }

    private void setField(Object object, Field field, Object value) throws NoSuchFieldException, IllegalAccessException {

        if (Modifier.isFinal(field.getModifiers())) {
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
        } else {
            try {
                field.set(object, value);
            } catch (IllegalArgumentException e) {
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
    }

}
