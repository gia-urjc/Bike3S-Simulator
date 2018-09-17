package es.urjc.ia.bikesurbanfleets.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.codec.digest.DigestUtils;

import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;

public class CheckSum {

    private String md5;

    public CheckSum() throws FileNotFoundException {
        File file = new File(GlobalInfo.AUX_DIR + "/mapMd5.txt");
        if(file.exists()) {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(file));
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            this.md5 = jsonObject.get("md5").getAsString();
        }
        else {
            this.md5 = null;
        }
        
    }
    
    public boolean md5CheckSum(File file) throws IOException {
        Gson gson = new Gson();
        String newMd5 = DigestUtils.md5Hex(new FileInputStream(file));
        if(md5 == null || !md5.equals(newMd5)) {
            this.md5 = newMd5;
            FileWriter fileWriter = new FileWriter(new File(GlobalInfo.AUX_DIR + "/mapMd5.txt"));
            gson.toJson(this, fileWriter);
            fileWriter.close();
            return false;
        }
        return true;
    }

}