package es.urjc.ia.bikesurbanfleets.log;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FileWriterMap {

    private Map<Class, Map<Integer, FileWriter>> fileWriterByClass = new HashMap<>();


    public FileWriter createFileWriter(Entity entity, Path pathFile) throws IOException {
        Class entityClass = getTopHierarchyClass(entity);
        Map<Integer, FileWriter> mapFWriter = this.fileWriterByClass.get(entityClass);
        FileWriter fileWriter = new FileWriter(pathFile.toAbsolutePath().toString(), true);
        if(mapFWriter == null) {
            mapFWriter = new HashMap<>();
            this.fileWriterByClass.put(entityClass, mapFWriter);
        }
        mapFWriter.put(entity.getId(), fileWriter);
        return fileWriter;
    }

    public FileWriter getFileWriter(Entity entity) {
        Class entityClass = getTopHierarchyClass(entity);
        try {
            return this.fileWriterByClass.get(entityClass).get(entity.getId());
        } catch (NullPointerException e) {
            return null;
        }

    }

    public void closeAllFileWriters() throws IOException {
        for(Class eClass: fileWriterByClass.keySet()){
            for(Integer id: fileWriterByClass.get(eClass).keySet()) {
                FileWriter writer = fileWriterByClass.get(eClass).get(id);
                writer.close();
                fileWriterByClass.get(eClass).put(id, null);
            }
        }
    }

    private Class getTopHierarchyClass(Entity entity) {
        Class entityClass =  entity.getClass();
        while(entityClass.getSuperclass() != Object.class) {
            entityClass = entityClass.getSuperclass();
        }
        return entityClass;
    }

}