package work.lclpnet.lobby.game.map;

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

        return Optional.of(mapRepository.open(source));
    }
}
