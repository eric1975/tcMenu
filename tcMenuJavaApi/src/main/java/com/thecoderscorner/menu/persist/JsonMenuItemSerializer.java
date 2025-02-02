package com.thecoderscorner.menu.persist;

import com.google.gson.*;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

import static com.thecoderscorner.menu.persist.PersistedMenu.*;
import static java.lang.System.Logger.Level.ERROR;

public class JsonMenuItemSerializer {
    private final static System.Logger logger = System.getLogger(JsonMenuItemSerializer.class.getSimpleName());
    private static final String PARENT_ID = "parentId";
    private static final String TYPE_ID = "type";
    private static final String ITEM_ID = "item";

    private final Gson gson;

    public JsonMenuItemSerializer() {
        this(null);
    }
    public JsonMenuItemSerializer(Consumer<GsonBuilder> builder) {
        this.gson = makeGsonProcessor(builder);
    }

    public Gson getGson() {
        return gson;
    }

    public List<PersistedMenu> populateListInOrder(SubMenuItem node, MenuTree menuTree) {
        ArrayList<PersistedMenu> list = new ArrayList<>();
        List<MenuItem> items = menuTree.getMenuItems(node);
        for (MenuItem item : items) {
            list.add(new PersistedMenu(node, item));
            if (item.hasChildren()) {
                list.addAll(populateListInOrder(MenuItemHelper.asSubMenu(item), menuTree));
            }
        }
        return list;
    }

    public String itemsToCopyText(MenuItem startingPoint, MenuTree tree) {
        List<PersistedMenu> items;
        if(startingPoint instanceof SubMenuItem) {
            items = populateListInOrder((SubMenuItem) startingPoint, tree);
        }
        else {
            items = new ArrayList<>(); // has to be an array list.
            items.add(new PersistedMenu(tree.findParent(startingPoint), startingPoint));
        }
        return TCMENU_COPY_PREFIX + gson.toJson(items);
    }

    @SuppressWarnings("unchecked")
    public List<PersistedMenu> copyTextToItems(String items) {
        if(!items.startsWith(TCMENU_COPY_PREFIX)) return Collections.emptyList();
        var jsonStr = items.substring(TCMENU_COPY_PREFIX.length());
        return gson.fromJson(jsonStr, ArrayList.class);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Gson makeGsonProcessor(Consumer<GsonBuilder> builderConsumer) {
        ArrayList<PersistedMenu> example = new ArrayList<>();

        var builder = new GsonBuilder();
        if(builderConsumer!=null) builderConsumer.accept(builder);
        return builder.registerTypeAdapter(example.getClass(), new MenuItemSerialiser())
                .registerTypeAdapter(example.getClass(), new MenuItemDeserialiser())
                .registerTypeAdapter(Instant.class, new CompatibleDateTimePersistor())
                .setPrettyPrinting()
                .create();
    }

    static class MenuItemSerialiser implements JsonSerializer<ArrayList<PersistedMenu>> {

        @Override
        public JsonElement serialize(ArrayList<PersistedMenu> src, Type type, JsonSerializationContext ctx) {
            if (src == null) {
                return null;
            }
            JsonArray arr = new JsonArray();
            src.forEach((itm) -> {
                JsonObject ele = new JsonObject();
                ele.addProperty(PARENT_ID, itm.getParentId());
                ele.addProperty(TYPE_ID, itm.getType());
                ele.add(ITEM_ID, ctx.serialize(itm.getItem()));
                arr.add(ele);
            });
            return arr;
        }
    }

    static class MenuItemDeserialiser implements JsonDeserializer<ArrayList<PersistedMenu>> {

        private final Map<String, Class<? extends MenuItem>> mapOfTypes = new HashMap<>();

        {
            mapOfTypes.put(ENUM_PERSIST_TYPE, EnumMenuItem.class);
            mapOfTypes.put(ANALOG_PERSIST_TYPE, AnalogMenuItem.class);
            mapOfTypes.put(BOOLEAN_PERSIST_TYPE, BooleanMenuItem.class);
            mapOfTypes.put(ACTION_PERSIST_TYPE, ActionMenuItem.class);
            mapOfTypes.put(TEXT_PERSIST_TYPE, EditableTextMenuItem.class);
            mapOfTypes.put(SUB_PERSIST_TYPE, SubMenuItem.class);
            mapOfTypes.put(RUNTIME_LIST_PERSIST_TYPE, RuntimeListMenuItem.class);
            mapOfTypes.put(RUNTIME_LARGE_NUM_PERSIST_TYPE, EditableLargeNumberMenuItem.class);
            mapOfTypes.put(CUSTOM_ITEM_PERSIST_TYPE, CustomBuilderMenuItem.class);
            mapOfTypes.put(SCROLL_CHOICE_PERSIST_TYPE, ScrollChoiceMenuItem.class);
            mapOfTypes.put(RGB32_COLOR_PERSIST_TYPE, Rgb32MenuItem.class);
            mapOfTypes.put(FLOAT_PERSIST_TYPE, FloatMenuItem.class);
        }

        @Override
        public ArrayList<PersistedMenu> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            ArrayList<PersistedMenu> list = new ArrayList<>();
            JsonArray ja = jsonElement.getAsJsonArray();

            ja.forEach(ele -> {
                String ty = ele.getAsJsonObject().get(TYPE_ID).getAsString();
                int parentId = ele.getAsJsonObject().get("parentId").getAsInt();
                Class<? extends MenuItem> c = mapOfTypes.get(ty);
                if (c != null) {
                    MenuItem item = ctx.deserialize(ele.getAsJsonObject().getAsJsonObject(ITEM_ID), c);
                    PersistedMenu m = new PersistedMenu();
                    m.setItem(item);
                    m.setParentId(parentId);
                    m.setType(ty);
                    list.add(m);
                } else {
                    logger.log(ERROR, "Item of type " + ty + " was not reloaded - skipping");
                }
            });

            return list;
        }
    }

    static class CompatibleDateTimePersistor implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        @Override
        public JsonElement serialize(Instant dt, Type type, JsonSerializationContext jsonSerializationContext) {
            var seconds = dt.getEpochSecond();
            var nanos = dt.getNano();
            JsonObject obj = new JsonObject();
            obj.add("seconds", new JsonPrimitive(seconds));
            obj.add("nanos", new JsonPrimitive(nanos));
            return obj;
        }

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var seconds = json.getAsJsonObject().get("seconds").getAsLong();
            var nanos = json.getAsJsonObject().get("nanos").getAsInt();
            return Instant.ofEpochSecond(seconds, nanos);
        }
    }

    public static JsonObject getJsonObjOrThrow(JsonObject object, String child) throws IOException {
        var data = object.get(child);
        if(data == null) throw new IOException("Missing mandatory element " + child);
        return data.getAsJsonObject();
    }

    public static String getJsonStrOrThrow(JsonObject object, String child) throws IOException {
        var data = object.get(child);
        if(data == null) throw new IOException("Missing mandatory element " + child);
        return data.getAsString();
    }

    public static int getJsonIntOrThrow(JsonObject object, String child) throws IOException {
        var data = object.get(child);
        if(data == null) throw new IOException("Missing mandatory element " + child);
        return data.getAsInt();
    }
}
