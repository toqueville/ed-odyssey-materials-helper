package nl.jixxed.eliteodysseymaterials.parser;

import com.fasterxml.jackson.databind.JsonNode;
import nl.jixxed.eliteodysseymaterials.enums.Asset;
import nl.jixxed.eliteodysseymaterials.enums.ContainerTarget;
import nl.jixxed.eliteodysseymaterials.enums.Material;
import nl.jixxed.eliteodysseymaterials.models.Container;

import java.util.Iterator;
import java.util.Map;

public class ComponentParser extends Parser {
    @Override
    public void parse(final Iterator<JsonNode> components, final ContainerTarget containerTarget, final Map<? extends Material, Container> knownMap, final Map<String, Container> unknownMap) {
        components.forEachRemaining(componentNode ->
        {
            final String name = componentNode.get("Name").asText();
            final Asset asset = Asset.forName(name);
            final int amount = componentNode.get("Count").asInt();
            if (Asset.UNKNOWN.equals(asset)) {
                System.out.println("Unknown Component detected: " + componentNode.toPrettyString());
            }
            final Container container = knownMap.get(asset);
            //stack values as items occur multiple times in the json
            container.setValue(container.getValue(containerTarget) + amount, containerTarget);
        });
    }
}