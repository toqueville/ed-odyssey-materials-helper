package nl.jixxed.eliteodysseymaterials.trade.message.inbound;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.jixxed.eliteodysseymaterials.trade.message.common.Offer;

@Data
@NoArgsConstructor
public class XBidPushMessage extends InboundMessage {
    private Offer offer;
}