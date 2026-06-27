package work.lclpnet.game.impl.menu;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.dialog.input.TextInput;
import net.minecraft.server.level.ServerPlayer;
import work.lclpnet.kibu.translate.Translations;

import java.util.List;
import java.util.Optional;

/**
 * A small, separate dialog that captures a search query for a {@link PaginatedOptionMenu}. Kept apart
 * from the menu inventory so that live refreshes can never re-open over it and interrupt the player
 * while typing. On completion it dispatches the given custom click action ids.
 */
final class MenuSearchDialog {

    static final String SEARCH_KEY = "search";

    private MenuSearchDialog() {}

    static void open(ServerPlayer player, Translations translations, String currentSearch,
                     Identifier submitId, Identifier cancelId) {

        Component label = translations.translateText(player, "mg-api.menu.search");
        Component hint = translations.translateText(player, "mg-api.menu.search_hint");

        List<Input> inputs = List.of(
                new Input(SEARCH_KEY, new TextInput(200, label, true, currentSearch, 128, Optional.empty()))
        );

        List<DialogBody> body = List.of(new PlainMessage(hint, 200));

        CommonDialogData common = new CommonDialogData(
                label, Optional.empty(), true, false, DialogAction.NONE, body, inputs
        );

        ActionButton submit = new ActionButton(
                new CommonButtonData(label, 200),
                Optional.of(new CustomAll(submitId, Optional.empty()))
        );

        ActionButton cancel = new ActionButton(
                new CommonButtonData(Component.translatable("gui.cancel"), 200),
                Optional.of(new CustomAll(cancelId, Optional.empty()))
        );

        MultiActionDialog dialog = new MultiActionDialog(common, List.of(submit), Optional.of(cancel), 1);

        player.openDialog(Holder.direct(dialog));
    }
}
