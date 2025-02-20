package ru.bgpu.annotationlk;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;

public class AppConfigWorker {

    private static Logger logger = Logger.getLogger(AppConfigWorker.class.getName());

    public static void configProcessing(String prefix, String filePropName) {

        Reflections reflections = new Reflections(prefix, Scanners.FieldsAnnotated);

        File prop = new File(filePropName);
        if (prop.isFile()) {
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(prop));

                reflections.getFieldsAnnotatedWith(AppConfig.class).forEach(
                        field -> {
                            String value = properties.getProperty(
                                    field.getName(),
                                    field.getAnnotation(AppConfig.class).defValue()
                            );

                            try {
                                field.setAccessible(true);
                                Object targetValue = convertValue(field.getType(), value);
                                field.set(field.getDeclaringClass(), targetValue);
                                field.setAccessible(false);
                            } catch (IllegalAccessException e) {
                                logger.log(
                                        Level.WARNING,
                                        "error set " + field.getDeclaringClass().getName()
                                                + "." + field.getName() + " " + value
                                );
                            } catch (IllegalArgumentException e) {
                                logger.log(
                                        Level.WARNING,
                                        "error converting value for " + field.getDeclaringClass().getName()
                                                + "." + field.getName() + " " + value
                                );
                            }
                        }
                );
            } catch (IOException e) {
                logger.log(Level.WARNING, "error load properties", e);
            }
        } else {
            logger.log(Level.WARNING, "config file not found");
        }
    }

    private static Object convertValue(Class<?> fieldType, String value) {
        if (fieldType == String.class) {
            return value;
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(value);
        } else if (fieldType == float.class || fieldType == Float.class) {
            return Float.parseFloat(value);
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.parseDouble(value);
        } else if (fieldType == int[].class) {
            return Arrays.stream(value.split(","))
                         .mapToInt(Integer::parseInt)
                         .toArray();
        } else if (fieldType == float[].class) {
            return Arrays.stream(value.split(","))
                         .mapToFloat(Float::parseFloat)
                         .toArray();
        } else if (fieldType == double[].class) {
            return Arrays.stream(value.split(","))
                         .mapToDouble(Double::parseDouble)
                         .toArray();
        } else if (fieldType == Integer[].class) {
            return Arrays.stream(value.split(","))
                          .map(Integer::parseInt)
                          .toArray(Integer[]::new);
        } else if (fieldType == Float[].class) {
            return Arrays.stream(value.split(","))
                         .map(Float::parseFloat)
                         .toArray(Float[]::new);
        } else if (fieldType == Double[].class) {
            return Arrays.stream(value.split(","))
                         .map(Double::parseDouble)
                         .toArray(Double[]::new);
        } else if (fieldType == String[].class) {
            return value.split(",");
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + fieldType);
        }
    }
}