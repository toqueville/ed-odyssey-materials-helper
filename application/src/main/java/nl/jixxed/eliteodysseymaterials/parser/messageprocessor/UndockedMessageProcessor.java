package nl.jixxed.eliteodysseymaterials.parser.messageprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import nl.jixxed.eliteodysseymaterials.service.event.EventService;
import nl.jixxed.eliteodysseymaterials.service.event.UndockedJournalEvent;

public class UndockedMessageProcessor implements MessageProcessor {
    @Override
    public void process(final JsonNode journalMessage) {
        final String timestamp = journalMessage.get("timestamp").asText();
        final String stationName = asTextOrBlank(journalMessage, "StationName");
        EventService.publish(new UndockedJournalEvent(timestamp, stationName));
    }
}
