package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

import java.io.IOException;
import java.util.List;

public class IdReferenceListAdapter extends TypeAdapter<List<Entity>> {

    @Override
    public List<Entity> read(JsonReader in) throws IOException {
        return null;
    }

    @Override
    public void write(JsonWriter out, List<Entity> entities) throws IOException {
        out.beginArray();
        for (Entity entity : entities) {
            if (entity == null) {
                out.nullValue();
            } else {
                out.value(entity.getId());
            }
        }
        out.endArray();
    }
}
