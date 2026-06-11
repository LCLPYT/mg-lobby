package work.lclpnet.game.map;

import work.lclpnet.gaco.asset.AssetPath;
import work.lclpnet.gaco.asset.AssetRequestOptions;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

public class RepositoryMapLookup implements MapLookup {

    private final MapRepository mapRepository;

    public RepositoryMapLookup(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    @Override
    public Collection<GameMap> getMaps(MapDescriptor descriptor) throws IOException {
        var refs = mapRepository.getMapList(AssetPath.of(descriptor.getMapPath()));

        return refs.stream()
                .map(ref -> GameMap.parse(ref.properties(), descriptor))
                .toList();
    }

    @Override
    public Iterable<URI> getSource(GameMap map) throws IOException {
        MapInfo info = loadMapInfo(map.getDescriptor());

        map.putProperties(info.properties());

        String source = info.getSource();

        if (source == null) {
            return Collections::emptyIterator;
        }

        // if the map info wasn't cached, fetch the fresh map source to keep it in sync with the info
        boolean infoWasCached = info.properties().optBoolean(AssetMapRepository.CACHED_PROPERTY, false);
        var opts = new AssetRequestOptions(!infoWasCached);

        AssetPath path = AssetPath.of(info.target(), source);

        return mapRepository.getUris(AssetPath.of(path.toString()), opts);
    }

    @Override
    public MapInfo loadMapInfo(MapDescriptor descriptor) throws IOException {
        return mapRepository.getMapInfo(AssetPath.of(descriptor.getMapPath()));
    }

    public MapRepository getMapRepository() {
        return mapRepository;
    }
}
