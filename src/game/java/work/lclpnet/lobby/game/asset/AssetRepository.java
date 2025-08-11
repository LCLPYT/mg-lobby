package work.lclpnet.lobby.game.asset;

import java.io.IOException;
import java.io.InputStream;

public interface AssetRepository {

    InputStream open(AssetPath path) throws IOException;
}
