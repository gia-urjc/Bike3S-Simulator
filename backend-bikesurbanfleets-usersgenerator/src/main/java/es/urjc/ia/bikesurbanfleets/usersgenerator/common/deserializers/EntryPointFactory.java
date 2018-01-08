package es.urjc.ia.bikesurbanfleets.usersgenerator.common.deserializers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoints.EntryPoint;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoints.distributions.Distribution;

/**
 * This class serves to create, in a generic way, entry point instances.
 * @author IAgroup
 *
 */
public class EntryPointFactory {


    private Gson gson;

    public EntryPointFactory() {
        this.gson = new Gson();
    }

    /**
     * It creates an entry point of a specific type.
     * @param json it contains the entry point information.
     * @param distribution It is the distribution type which determines the entry point type to create.
     * @return an instance of specific entry point type.
     */
    public EntryPoint createEntryPoint(JsonObject json, Distribution.DistributionType distribution) {
        switch(distribution) {
            case POISSON: return gson.fromJson(json, EntryPointPoisson.class);
            case NONEDISTRIBUTION: return gson.fromJson(json, EntryPointSingle.class);
            default: throw new JsonParseException("Type of EntryPoint doesn't exists");
        }
    }

}

