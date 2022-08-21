package io.github.nowipi.JON;

import io.github.nowipi.JON.JONObject;
import sun.misc.Unsafe;

import java.lang.reflect.*;

public class JONUtils {

    public static Object toPrimitive(Object wrapper) {
        if (wrapper instanceof Integer a) {
            return a.intValue();
        } else if (wrapper instanceof Double a) {
            return a.doubleValue();
        } else if (wrapper instanceof Float a) {
            return a.floatValue();
        } else if (wrapper instanceof Short a) {
            return a.shortValue();
        } else if (wrapper instanceof Byte a) {
            return a.byteValue();
        } else if (wrapper instanceof Long a) {
            return a.longValue();
        } else if (wrapper instanceof Character a) {
            return a.charValue();
        }
        return null;
    }

    public static <T> Class<?> getAbsoluteArrayType(Class<T> clazz) {
        String str = clazz.getName().replaceAll("\\[", "");
        switch (str) {
            case "I" -> {
                return int.class;
            }
            default -> {
                return null;
            }
        }
    }

    public static Integer[] toWrapperArray(final int[] array) {
        Integer[] a = new Integer[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }

    public static Double[] toWrapperArray(final double[] array) {
        Double[] a = new Double[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }
    public static Float[] toWrapperArray(final float[] array) {
        Float[] a = new Float[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }
    public static Short[] toWrapperArray(final short[] array) {
        Short[] a = new Short[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }
    public static Byte[] toWrapperArray(final byte[] array) {
        Byte[] a = new Byte[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }
    public static Long[] toWrapperArray(final long[] array) {
        Long[] a = new Long[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }
    public static Boolean[] toWrapperArray(final boolean[] array) {
        Boolean[] a = new Boolean[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }
    public static Character[] toWrapperArray(final char[] array) {
        Character[] a = new Character[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }


    public static String replaceLast(final char last, final String of) {
        String str = null;
        int index = of.lastIndexOf(last);
        if (index != -1) {
            str = of.substring(0, index);
        }
        return str;
    }

    public static boolean isPrimitive(Class<?> clazz) {

        return clazz.equals(Integer.class)  || clazz.equals(int.class) ||
                clazz.equals(Double.class)  || clazz.equals(double.class) ||
                clazz.equals(Float.class)   || clazz.equals(float.class) ||
                clazz.equals(Short.class)   || clazz.equals(short.class) ||
                clazz.equals(Byte.class)    || clazz.equals(byte.class) ||
                clazz.equals(Long.class)    || clazz.equals(long.class) ||
                clazz.equals(Boolean.class) || clazz.equals(boolean.class) ||
                clazz.equals(Character.class) | clazz.equals(char.class);
    }

    private boolean isSerializable(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && !field.isEnumConstant();
    }

    private static void setField(Object object, Field field, Object value) throws NoSuchFieldException, IllegalAccessException {

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
            field.set(object, value);
        }
    }

    private Object getObject(Object object, Field field) throws NoSuchFieldException, IllegalAccessException {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (InaccessibleObjectException e) {
            //get Unsafe object
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            long offset = unsafe.objectFieldOffset(field);
            return unsafe.getObject(object, offset);
        }
    }

    private void getPath(JONObject root, JONObject jonObject) {

    }

}
