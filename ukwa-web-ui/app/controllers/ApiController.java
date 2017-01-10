package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import play.Logger;
import play.libs.F;
import play.libs.F.Option;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import uk.bl.wa.w3act.CollectionTree;
import uk.bl.wa.w3act.Target;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static play.data.Form.form;

public class ApiController extends Controller {
    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        // disable JSON serialisation auto detection - see http://stackoverflow.com/a/25903957/2290217
        objectMapper.disable(MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS);
        // if you want to prevent an exception when classes have no annotated properties
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public Result collections(Option<Long> start, Option<Long> count){
        try {
            return ok(objectMapper.writeValueAsString(
                    Application.collectionsDataSource.getCollections().values()
                        .stream()
                        .skip(start.getOrElse(0L))
                        .limit(count.getOrElse(Long.MAX_VALUE))
                        .collect(Collectors.toList())
            )).as("application/json");
        }
        catch(JsonProcessingException e) {
            Logger.error("Error serialising collections", e);
            return internalServerError();
        }
    }

    public Result collection(Long id){
        CollectionTree collectionTree = null;
        for(CollectionTree top : Application.collectionsDataSource.getCollections().values()) {
            if(collectionTree == null) {
                collectionTree = top.find(id);
            }
        }

        if(collectionTree == null) {
            return notFound("No collection with ID " + id);
        }

        try {
            return ok(objectMapper.writeValueAsString(collectionTree)).as("application/json");
        }
        catch(JsonProcessingException e) {
            Logger.error("Error serialising collection", e);
            return internalServerError();
        }
    }

    public Result target(Long id) {
        Target target = Application.collectionsDataSource.getTarget(id);

        if(target == null) {
            return notFound("No target with ID " + id);
        }

        try {
            return ok(objectMapper.writeValueAsString(target)).as("application/json");
        }
        catch(JsonProcessingException e) {
            Logger.error("Error serialising target", e);
            return internalServerError();
        }
    }
}
