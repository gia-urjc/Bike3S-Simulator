package es.urjc.ia.bikesurbanfleets.log;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;

public class FileWriterMap {

    private Map<Class<?>, Map<Integer, FileWriter>> fileWriterByClass = new HashMap<>();

    public FileWriter createFileWriter(Entity entity, Path pathFile) throws IOException {
        Class<?> entityClass = getTopHierarchyClass(entity);
        Map<Integer, FileWriter> mapFWriter = this.fileWriterByClass.get(entityClass);
        FileWriter fileWriter = new FileWriter(pathFile.toAbsolutePath().toString(), true);
        if (mapFWriter == null) {
            mapFWriter = new HashMap<>();
            this.fileWriterByClass.put(entityClass, mapFWriter);
        }
        mapFWriter.put(entity.getId(), fileWriter);
        return fileWriter;
    }

    public void closeAllFileWriters() throws IOException {
        if (fileWriterByClass != null) {
            for (Map<Integer, FileWriter> map : fileWriterByClass.values()) {
                if (map != null) {
                    for (FileWriter f : map.values()) {
                        f.close();
                    }
                }
            }
        }
    }

    public FileWriter getFileWriter(Entity entity) {
        Class<?> entityClass = getTopHierarchyClass(entity);
        try {
            return this.fileWriterByClass.get(entityClass).get(entity.getId());
        } catch (NullPointerException e) {
            return null;
        }

    }

    public void closeFileWriter(Entity entity, int id) throws IOException {
        Class<?> entityClass = getTopHierarchyClass(entity);
        FileWriter writer = this.fileWriterByClass.get(entityClass).get(id);
        writer.close();
 //       System.out.println("Closed File in Event");
        this.fileWriterByClass.get(entityClass).remove(id);
    }

    private Class<?> getTopHierarchyClass(Entity entity) {
        Class<?> entityClass = entity.getClass();
        while (entityClass.getSuperclass() != Object.class) {
            entityClass = entityClass.getSuperclass();
        }
        return entityClass;
    }

}
