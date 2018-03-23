package es.urjc.ia.bikesurbanfleets.log;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class Debug {

    public static boolean DEBUG_MODE;
    private static FileWriterMap fileWriterMap = new FileWriterMap();
    private static final Path DEBUG_PATH = Paths.get( "debug_logs");

    public static void init() throws IOException {
        if(DEBUG_MODE) {
            FileUtils.deleteDirectory(new File(DEBUG_PATH.toAbsolutePath().toString()));
        }
    }

    public static void log(String message, Entity entity, Event event) throws IOException {
        if(DEBUG_MODE) {
            FileWriter writer = fileWriterMap.getFileWriter(entity);
            if(writer == null) {
                writer = createLog(entity);
            }

            writer.write("----------------------------\n");
            writer.write("Instant: " + event.getInstant() + "\n");
            writer.write("Event: " + event.getClass().getSimpleName() + "\n");
            writer.write(message + "\n");
            writer.write(entity.toString() + "\n");
            writer.write("-----------------------------\n");
        }
    }

    public static void log(Entity entity, Event event) throws IOException {
        if(DEBUG_MODE) {
            FileWriter writer = fileWriterMap.getFileWriter(entity);
            if(writer == null) {
                writer = createLog(entity);
            }

            writer.write("----------------------------\n");
            writer.write("Instant: " + event.getInstant() + "\n");
            writer.write("Event: " + event.getClass().getSimpleName() + "\n");
            writer.write(entity.toString() + "\n");
            writer.write("-----------------------------\n");
        }
    }

    public static void closeAllLogs() throws IOException {
        if(DEBUG_MODE) {
            fileWriterMap.closeAllFileWriters();
        }
    }

    private static FileWriter createLog(Entity entity) throws IOException {

        Class entityClass = entity.getClass();
        while(entityClass.getSuperclass() != Object.class) {
            entityClass = entityClass.getSuperclass();
        }

        String folderNameLogs = entityClass.getSimpleName();
        Path entityLogPath = Paths.get(DEBUG_PATH.toString(), folderNameLogs);
        System.out.println(entityLogPath.toAbsolutePath());
        if(!Files.exists(DEBUG_PATH)) {
            Files.createDirectory(DEBUG_PATH.toAbsolutePath());
        }

        if(!Files.exists(entityLogPath)) {
            Files.createDirectory(entityLogPath.toAbsolutePath());
        }

        Path pathFile = Paths.get(entityLogPath.toString(),
                entityClass.getSimpleName() + entity.getId() + ".txt");

        return fileWriterMap.createFileWriter(entity, pathFile);
    }
}
