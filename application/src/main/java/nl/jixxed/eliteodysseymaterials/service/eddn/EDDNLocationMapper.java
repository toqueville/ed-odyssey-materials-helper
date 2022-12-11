package nl.jixxed.eliteodysseymaterials.service.eddn;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.jixxed.eliteodysseymaterials.enums.Expansion;
import nl.jixxed.eliteodysseymaterials.schemas.eddn.carrierjump.*;
import nl.jixxed.eliteodysseymaterials.schemas.eddn.location.Message;
import nl.jixxed.eliteodysseymaterials.schemas.journal.Location.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EDDNLocationMapper extends EDDNMapper {

    public static Message mapToEDDN(final Location location, final Expansion expansion) {
        return new Message.MessageBuilder()
                .withTimestamp(location.getTimestamp())
                .withEvent(location.getEvent())
                .withStarPos(location.getStarPos())
                .withStarSystem(location.getStarSystem())
                .withSystemAddress(location.getSystemAddress())
                .withHorizons(expansion.equals(Expansion.HORIZONS) || expansion.equals(Expansion.ODYSSEY))
                .withOdyssey(expansion.equals(Expansion.ODYSSEY))
                .withBodyID(location.getBodyID())
                .withBody(location.getBody())
                .withBodyType(location.getBodyType())
                .withMarketID(location.getMarketID().orElse(null))
                .withDistFromStarLS(location.getDistFromStarLS().orElse(null))
                .withDocked(location.getDocked())
                .withInSRV(location.getInSRV().orElse(null))
                .withTaxi(location.getTaxi().orElse(null))
                .withMulticrew(location.getMulticrew().orElse(null))
                .withOnFoot(location.getOnFoot().orElse(null))
                .withPopulation(location.getPopulation())
                .withPowerplayState(location.getPowerplayState().orElse(""))
                .withPowers(mapToNullIfEmptyList(location.getPowers()).orElse(null))
                .withStationAllegiance(location.getStationAllegiance().orElse(null))
                .withStationEconomies(mapToNullIfEmptyList(location.getStationEconomies())
                        .map(stationEconomies -> stationEconomies.stream()
                                .map(stationEconomy -> new StationEconomy.StationEconomyBuilder()
                                        .withName(stationEconomy.getName())
                                        .withProportion(stationEconomy.getProportion())
                                        .build())
                                .toList())
                        .orElse(null))
                .withStationEconomy(location.getStationEconomy().orElse(null))
                .withStationFaction(location.getStationFaction()
                        .map(faction -> new SystemFaction.SystemFactionBuilder()
                                .withName(faction.getName())
                                .withFactionState(faction.getFactionState().orElse(null))
                                .build())
                        .orElse(null))
                .withStationGovernment(location.getStationGovernment().orElse(null))
                .withStationName(location.getStationName().orElse(null))
                .withStationServices(mapToNullIfEmptyList(location.getStationServices()).orElse(null))
                .withStationType(location.getStationType().orElse(null))
                .withSystemEconomy(location.getSystemEconomy())
                .withSystemAllegiance(location.getSystemAllegiance())
                .withSystemFaction(location.getSystemFaction()
                        .map(faction -> new SystemFaction.SystemFactionBuilder()
                                .withName(faction.getName())
                                .withFactionState(faction.getFactionState().orElse(null))
                                .build())
                        .orElse(null))
                .withSystemGovernment(location.getSystemGovernment())
                .withSystemSecondEconomy(location.getSystemSecondEconomy())
                .withSystemSecurity(location.getSystemSecurity())
                .withFactions(mapToNullIfEmptyList(location.getFactions()).map(factions -> factions.stream()
                        .map(faction -> new Faction.FactionBuilder()
                                .withName(faction.getName())
                                .withFactionState(faction.getFactionState())
                                .withAllegiance(faction.getAllegiance())
                                .withGovernment(faction.getGovernment())
                                .withHappiness(faction.getHappiness())
                                .withInfluence(faction.getInfluence())
                                .withActiveStates(mapToNullIfEmptyList(faction.getActiveStates())
                                        .map(activeStates -> activeStates.stream()
                                                .map(activeState -> new ActiveState.ActiveStateBuilder().withState(activeState.getState()).build())
                                                .toList())
                                        .orElse(null))
                                .withPendingStates(mapToNullIfEmptyList(faction.getPendingStates())
                                        .map(pendingStates -> pendingStates.stream()
                                                .map(pendingState -> new PendingState.PendingStateBuilder().withState(pendingState.getState()).build())
                                                .toList())
                                        .orElse(null))
                                .withRecoveringStates(mapToNullIfEmptyList(faction.getRecoveringStates())
                                        .map(recoveringStates -> recoveringStates.stream()
                                                .map(recoveringState -> new RecoveringState.RecoveringStateBuilder().withState(recoveringState.getState()).build())
                                                .toList())
                                        .orElse(null))
                                .build())
                        .toList()).orElse(null))
                .withConflicts(mapToNullIfEmptyList(location.getConflicts())
                        .map(conflicts -> conflicts.stream()
                                .map(conflict -> new Conflict.ConflictBuilder()
                                        .withStatus(conflict.getStatus())
                                        .withWarType(conflict.getWarType())
                                        .withFaction1(new ConflictFaction.ConflictFactionBuilder()
                                                .withName(conflict.getFaction1().getName())
                                                .withStake(conflict.getFaction1().getStake())
                                                .withWonDays(conflict.getFaction1().getWonDays())
                                                .build())
                                        .withFaction2(new ConflictFaction.ConflictFactionBuilder()
                                                .withName(conflict.getFaction2().getName())
                                                .withStake(conflict.getFaction2().getStake())
                                                .withWonDays(conflict.getFaction2().getWonDays())
                                                .build())
                                        .build())
                                .toList())
                        .orElse(null))
                .build();
    }
}
