package work.lclpnet.lobby.game.map;

import work.lclpnet.lobby.game.asset.AssetRequestOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

public class RepositoryMapLookup implements MapLookup {

    private final MapRepository mapRepository;

    public RepositoryMapLookup(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    @Override
    public Collection<GameMap> getMaps(MapDescriptor descriptor) throws IOException {
        var refs = mapRepository.getMapList(descriptor.getMapPath());

        return refs.stream()
                .map(ref -> GameMap.parse(ref.getProperties(), descriptor))
                .toList();
    }

    @Override
    public Optional<InputStream> openSource(GameMap map) throws IOException {
        MapInfo info = mapRepository.getMapInfo(map.getDescriptor().getMapPath());

        map.putProperties(info.properties());

        String source = info.getSource();

        if (source == null) {
            return Optional.empty();
        }

        // if the map info wasn't cached, fetch the fresh map source to keep it in sync with the info
        boolean infoWasCached = info.properties().optBoolean(AssetMapRepository.CACHED_PROPERTY, false);
        var opts = new AssetRequestOptions(!infoWasCached);

        return Optional.of(mapRepository.open(source, opts));
    }
}
