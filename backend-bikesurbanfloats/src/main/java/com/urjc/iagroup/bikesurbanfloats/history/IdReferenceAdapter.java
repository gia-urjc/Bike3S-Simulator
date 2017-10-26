package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

import java.io.IOException;

public class IdReferenceAdapter extends TypeAdapter<Entity> {

    @Override
    public Entity read(JsonReader in) throws IOException {
        return null;
    }

    @Override
    public void write(JsonWriter out, Entity entity) throws IOException {
        if (entity == null) {
            out.nullValue();
            return;
        }

        out.value(entity.getId());
    }
}
