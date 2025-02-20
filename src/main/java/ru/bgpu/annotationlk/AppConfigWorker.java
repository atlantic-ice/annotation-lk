package ru.bgpu.annotationlk;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppConfigWorker {

    private static Logger logger = Logger.getLogger(AppConfigWorker.class.getName());

    public static void configProcessing(String prefix, String filePropName) {

        Reflections reflections = new Reflections(prefix, Scanners.FieldsAnnotated);

        File prop = new File(filePropName);
        if(prop.isFile()) {
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(prop));

                reflections.getFieldsAnnotatedWith(AppConfig.class).forEach(
                        field -> {

                            String value = properties.getProperty(
                                    field.getName(),
                                    field.getAnnotation(AppConfig.class).defValue()
                            );
                            Object targetValue = null;

                            if(field.getType().equals(String.class)) {
                                targetValue = value;
                            } else if(field.getType().equals(Integer.class)) {
                                targetValue = Integer.valueOf(value);
                            }

                            try {
                                field.setAccessible(true);
                                field.set(field.getDeclaringClass(), targetValue);
                                field.setAccessible(false);
                            } catch (IllegalAccessException e) {
                                logger.log(
                                        Level.WARNING,
                                        "error set "+field.getDeclaringClass().getName()
                                                +"."+field.getName()+" "+value
                                );
                            }

//                            System.out.println(field.getName());
                        }
                );
            } catch (Exception e) {
                logger.log(Level.WARNING, "error load properties", e);
            }
        } else {
            logger.log(Level.WARNING, "config file not found");
        }
    }

}
